package compiler.expressions;

import compiler.components.Function;
import compiler.types.ConditionType;

public class Condition {
	
	ConditionType type;
	Expression exp;
	String f;
	
	
	public Condition(ConditionType type, Expression exp, String string) {
		super();
		this.type = type;
		this.exp = exp;
		this.f = string;
	}
	
	public Condition(ConditionType type, String f) {
		super();
		this.type = type;
		this.f = f;
	}

	public ConditionType getType() {
		return type;
	}
	public void setType(ConditionType type) {
		this.type = type;
	}
	public Expression getExp() {
		return exp;
	}
	public void setExp(Expression exp) {
		this.exp = exp;
	}
	public String getF() {
		return f;
	}
	public void setF(String f) {
		this.f = f;
	}
	
	

}
