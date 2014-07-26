package asm;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.StringBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Asm {
	public static class AsmException extends RuntimeException {
		public AsmException(int lineNum, String error) {
			super(String.format("line %d: %s", lineNum, error));
		}

		public AsmException(int lineNum, String line, String error) {
			super(String.format("line %d: %s: %s", lineNum, error, line));
		}
	}

	static boolean isLiteralInt(String token) {
		return token.matches("[0-9]+");
	}

	private static class Instruction {
		final int lineNum; /* for error reporting */
		final String name;
		public final List<String> args = new ArrayList<>();
		private final InstructionDef def;

		Instruction(int lineNum, String name) {
			this.lineNum = lineNum;
			this.name = name;
			try {
				this.def = InstructionDef.valueOf(name);
			} catch (IllegalArgumentException e) {
				throw new AsmException(lineNum, "no such instruction " + name);
			}
		}

		public void checkArgs() throws AsmException {
			if (args.size() != def.args.length) {
				throw new AsmException(lineNum, "instruction " + name + ": expected " + def.args.length + " arguments but got " + args.size());
			}
			for (int i = 0; i < def.args.length; i++) {
				if (!isLiteralInt(args.get(i)) && def.args[i] != InstructionDef.Arg.ADDR) {
					throw new AsmException(lineNum, "can't use label " + args.get(i) + " where " + name + " expects literal int");
				}
			}
		}

		public String toString(Map<String, Integer> labels) {
			StringBuilder sb = new StringBuilder();
			sb.append(String.format("%-5s", name));
			for (String arg : args) {
				if (isLiteralInt(arg)) {
					sb.append(" ").append(arg);
				} else {
					Integer addr = labels.get(arg);
					if (addr == null) {
						throw new AsmException(lineNum, "undefined label " + arg);
					}
					sb.append(" ").append(addr);
				}
			}
			return sb.toString().trim();
		}
	}

	private static class AsmDoc {
		final List<Instruction> instructions = new ArrayList<>();
		final Map<String, Integer> labels = new HashMap<>();

		/* parses the incoming data to generate a doc */
		AsmDoc(Reader r) throws IOException {
			BufferedReader br = new BufferedReader(r);
			String line;
			int lineNum = 0;
			String curLabel = null;
			while ((line = br.readLine()) != null) {
				lineNum++;
				String[] fields = line.split(" +");
				Instruction ins = null;
				for (String field : fields) {
					if (field.length() == 0)
						continue;
					if (field.charAt(0) == ';')
						break; // ; marks start of comment, skip rest of line
					if (ins == null) {
						if (field.endsWith(":")) {
							/* this is a label not an instruction */
							if (curLabel != null) {
								throw new AsmException(lineNum, line, "two adjacent labels");
							}
							curLabel = field.substring(0, field.length() - 1);
						} else {
							ins = new Instruction(lineNum, field);
						}
					} else {
						ins.args.add(field);
					}
				}
				if (ins != null) {
					ins.checkArgs();
					instructions.add(ins);
					if (curLabel != null) {
						labels.put(curLabel, instructions.size() - 1);
						curLabel = null;
					}
				}
			}
		}

		public void writeTo(Writer w) throws IOException {
			for (Instruction ins : instructions) {
				w.write(ins.toString(labels));
				w.write('\n');
			}
		}
	}

	public static String replaceExtension(String file, String newExt) {
		int dot = file.lastIndexOf(".");
		if (dot == -1) {
			return file + "." + newExt;
		}
		return file.substring(0, dot) + "." + newExt;
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
