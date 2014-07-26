package asm;

public enum InstructionDef {
	LDC(Arg.INT),
	LD(Arg.INT, Arg.INT),
	ADD(),
	SUB(),
	MUL(),
	DIV(),
	CEQ(),
	CGT(),
	CGTE(),
	ATOM(),
	CONS(),
	CAR(),
	CDR(),
	SEL(Arg.ADDR,Arg.ADDR),
	JOIN(),
	LDF(Arg.ADDR),
	AP(Arg.INT),
	RTN(),
	DUM(Arg.INT),
	RAP(Arg.INT),
	STOP(),
	TSEL(Arg.ADDR, Arg.ADDR),
	TAP(Arg.INT),
	TRAP(Arg.INT),
	ST(Arg.INT, Arg.INT),
	DBUG(),
	BRK(),
	;

	final Arg[] args;

	private InstructionDef(Arg... args) {
		this.args = args;
	}

	enum Arg {
		INT, /* literal integer */
		ADDR, /* absolute instruction address */
	}
}
