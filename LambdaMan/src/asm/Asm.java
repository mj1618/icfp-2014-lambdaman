package asm;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.StringBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import compiler.components.Function;
import util.Pair;
import util.Utils;

public class Asm {
	public static class AsmException extends RuntimeException {
		public AsmException(String origin, String error) {
			super(String.format("%s: %s", origin, error));
		}

		public AsmException(int lineNum, String error) {
			super(String.format("line %d: %s", lineNum, error));
		}

		public AsmException(int lineNum, String line, String error) {
			super(String.format("line %d: %s: %s", lineNum, error, line));
		}
	}

	private static class Instruction {
		final String origin; /* for error reporting */
		final String name;
		public final List<String> args = new ArrayList<>();
		private final InstructionDef def;

		Instruction(String origin, String name) {
			this.origin = origin;
			this.name = name;
			try {
				this.def = InstructionDef.valueOf(name);
			} catch (IllegalArgumentException e) {
				throw new AsmException(origin, "no such instruction " + name);
			}
		}

		public void checkArgs() throws AsmException {
			if (args.size() != def.args.length) {
				throw new AsmException(origin, "instruction " + name + ": expected " + def.args.length + " arguments but got " + args.size());
			}
		}

		public String toString(Map<String, Integer> labels, Map<String, Integer> constants) {
			StringBuilder sb = new StringBuilder();
			sb.append(String.format("%-5s", name));
			for (int i = 0; i < args.size(); i++) {
				String arg = args.get(i);
				if (Utils.IsInteger(arg)) {
					sb.append(" ").append(arg);
				} else if (def.args[i] == InstructionDef.Arg.ADDR) {
					Integer addr = labels.get(arg);
					if (addr == null) {
						throw new AsmException(origin, "undefined label '" + arg + "'");
					}
					sb.append(" ").append(addr);
				} else {
					Integer val = constants.get(arg);
					if (val == null) {
						throw new AsmException(origin, "undefined constant '" + arg + "'");
					}
					sb.append(" ").append(val);
				}
			}
			return sb.toString().trim();
		}
	}

	private static class ParseResult {
		String label;
		Instruction ins;
		Pair<String, Integer> constant;

		static ParseResult label(String label) {
			ParseResult pr = new ParseResult();
			pr.label = label;
			return pr;
		}
	}

	public static class AsmDoc {
		final List<Instruction> instructions = new ArrayList<>();
		final Map<String, Integer> labels = new HashMap<>();
		final Map<String, Integer> constants = new HashMap<>();

		private class ParseContext {
			String fnName = null;
			int lineNum = 0;
			String curLabel;

			private ParseResult parseLine(int lineNum, String line) {
				ParseResult result = new ParseResult();
				String[] fields = line.split(" +");
				if (fields.length == 0) {
					return result;
				}
				if (fields[0].equals("$const")) {
					if (fields.length < 3) {
						throw new AsmException(lineNum, line, "constant definition must be on one line");
					}
					if (!Utils.IsInteger(fields[2])) {
						throw new AsmException(lineNum, line, "constant value must be literal int");
					}
					result.constant = new Pair<>(fields[1], Integer.valueOf(fields[2]));
					return result;
				}
				for (String field : fields) {
					if (field.length() == 0)
						continue;
					if (field.charAt(0) == ';')
						return result; // ; marks start of comment, skip rest of line
					if (result.ins == null) {
						if (field.endsWith(":")) {
							/* this is a label not an instruction */
							result.label = field.substring(0, field.length() - 1);
						} else {
							String origin = (fnName == null) ? "line " + lineNum : fnName + ":" + lineNum;
							result.ins = new Instruction(origin, field);
						}
					} else {
						result.ins.args.add(field);
					}
				}
				return result;
			}

			void handle(ParseResult result) {
				if (result.constant != null) {
					if (curLabel != null) {
						throw new AsmException(lineNum, "expected instruction to follow label");
					}
					if (constants.containsKey(result.constant.first)) {
						throw new AsmException(lineNum, "duplicate constant " + result.constant.first);
					}
					constants.put(result.constant.first, result.constant.second);
					return;
				}
				if (result.label != null) {
					if (curLabel != null) {
						throw new AsmException(lineNum, "two adjacent labels " + curLabel + " and " + result.label);
					}
					curLabel = result.label;
				}
				if (result.ins != null) {
					result.ins.checkArgs();
					instructions.add(result.ins);
					if (curLabel != null) {
						if (labels.containsKey(curLabel)) {
							throw new AsmException(lineNum, "duplicate label " + curLabel);
						}
						labels.put(curLabel, instructions.size() - 1);
						curLabel = null;
					}
				}
			}
		}

		public AsmDoc(Map<String, Function> functions) {
			boolean first = true;
			for (Map.Entry<String, Function> entry : functions.entrySet()) {
				String fnName = entry.getKey();
				if (first && !fnName.toLowerCase().equals("main")) {
					System.err.println("asm: warning: first function is '" + entry.getKey() + "' instead of 'main'");
				}
				first = false;
				ParseContext ctx = new ParseContext();
				ctx.fnName = fnName;
				ctx.handle(ParseResult.label(fnName));
				try {
					for (String ins : entry.getValue().getAssembly()) {
						ctx.lineNum++;
						ctx.handle(ctx.parseLine(ctx.lineNum, ins));
					}
				} catch (AsmException e) {
					System.err.println("function " + fnName + ": " + e.getMessage());
				}
			}
		}

		/* parses the incoming data to generate a doc */
		AsmDoc(Reader r) throws IOException {
			BufferedReader br = new BufferedReader(r);
			String line;
			ParseContext ctx = new ParseContext();
			while ((line = br.readLine()) != null && line.equals("END!")==false) {
				ctx.lineNum++;
				ctx.handle(ctx.parseLine(ctx.lineNum, line));
			}
		}

		public void writeTo(Writer w) throws IOException {
			for (Instruction ins : instructions) {
				w.write(ins.toString(labels, constants));
				w.write('\n');
			}
		}
	}

	public static BufferedReader Assemble(Reader r) throws IOException{
		StringWriter w = new StringWriter();
		try{
				new AsmDoc(r).writeTo(w);
				return new BufferedReader(new StringReader(w.getBuffer().toString()));
			} catch (AsmException e) {
				System.err.printf("stdin: %s\n", e.getMessage());
				e.printStackTrace(System.err);
				return null;
			}
		
	}

	public static void main(String[] args) throws IOException {
		try (
			Reader reader = new InputStreamReader(System.in);
			Writer writer = new OutputStreamWriter(System.out);
		) {
			new AsmDoc(reader).writeTo(writer);
		} catch (AsmException e) {
			System.err.printf("stdin: %s\n", e.getMessage());
		}
	}
}
