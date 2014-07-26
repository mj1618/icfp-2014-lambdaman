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
import java.util.List;
import java.util.Map;

import asm.AsmDoc;
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

	static class Instruction {
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
