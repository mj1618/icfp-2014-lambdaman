package ghc.interpret;

import java.util.List;

public class Operation {

	Instruction ins;
	List<Argument> args;
	
	public Operation(){
		
	}
	
	public Operation(Instruction ins, List<Argument> args) {
		super();
		this.ins = ins;
		this.args = args;
	}
	
	
	
	public Instruction getIns() {
		return ins;
	}
	public void setIns(Instruction ins) {
		this.ins = ins;
	}
	public List<Argument> getArgs() {
		return args;
	}
	public void setArgs(List<Argument> args) {
		this.args = args;
	}
	
	
	
}
