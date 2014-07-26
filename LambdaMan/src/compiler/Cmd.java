package compiler;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;

import asm.AsmDoc;
import compiler.Compiler;

/* frontend for compiler + assembler */
public class Cmd {
	static void usage() {
		System.err.println("usage: java compiler.Cmd FILE.hla");
		System.exit(1);
	}

	public static void main(String[] args) throws IOException {
		if (args.length == 0) {
			usage();
		}
		Compiler c = Compiler.Instance(new File(args[0]));
		c.compile();
		AsmDoc asm = new AsmDoc(c.functions);
		asm.writeTo(new OutputStreamWriter(System.out));
	}
}
