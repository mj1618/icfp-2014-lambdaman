package compiler.expressions;

public class IfElse {

	String ifFunction;
	String elseFunction;
	
	public IfElse(String ifFunction, String elseFunction) {
		super();
		this.ifFunction = ifFunction;
		this.elseFunction = elseFunction;
	}
	public String getIfFunction() {
		return ifFunction;
	}
	public void setIfFunction(String ifFunction) {
		this.ifFunction = ifFunction;
	}
	public String getElseFunction() {
		return elseFunction;
	}
	public void setElseFunction(String elseFunction) {
		this.elseFunction = elseFunction;
	}
	
}
