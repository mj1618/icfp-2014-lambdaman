package compiler.expressions;

import java.util.List;

public class IfElse {

	Condition ifcond;
	Condition elsecond;
	
	public IfElse(Condition ifFunction,Condition elseFunction
			) {
		super();
		this.ifcond = ifFunction;
		this.elsecond=elseFunction;
	}

	public Condition getIfcond() {
		return ifcond;
	}

	public void setIfcond(Condition ifcond) {
		this.ifcond = ifcond;
	}

	public Condition getElsecond() {
		return elsecond;
	}

	public void setElsecond(Condition elsecond) {
		this.elsecond = elsecond;
	}

	
}
