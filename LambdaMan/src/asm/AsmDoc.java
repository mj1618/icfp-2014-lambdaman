package asm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import asm.Asm.AsmException;
import asm.Asm.Instruction;
import compiler.components.Function;
import util.Pair;
import util.Utils;

public class AsmDoc {
	final List<Instruction> instructions = new ArrayList<>();
	final Map<String, Integer> labels = new HashMap<>();
	final Map<String, Integer> constants = new HashMap<>();

	private static class ParseResult {
		String label;
		Instruction ins;
		Pair<String, Integer> constant;
		String comment;

		static ParseResult label(String label) {
			ParseResult pr = new ParseResult();
			pr.label = label;
			return pr;
		}
	}

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
				if (result.comment != null) {
					result.comment += " " + field;
					continue;
				}
				if (field.charAt(0) == ';') {
					result.comment = field; // ; marks start of comment
					continue;
				}
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
				if (result.comment != null) {
					result.ins.comment = result.comment;
				}
				instructions.add(result.ins);
				if (curLabel != null) {
					if (labels.containsKey(curLabel)) {
						throw new AsmException(lineNum, "duplicate label " + curLabel);
					}
					labels.put(curLabel, instructions.size() - 1);
					if (result.ins.comment == null) {
						result.ins.comment = "; " + curLabel + ":";
					} else {
						result.ins.comment = result.ins.comment.replaceFirst("; ", "; " + curLabel + ": ");
					}
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
