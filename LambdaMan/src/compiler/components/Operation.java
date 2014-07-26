package compiler.components;

import compiler.expressions.Expression;
import compiler.expressions.IfElse;
import compiler.types.OpType;

public class Operation {

	OpType type;
	Expression expression;
	String assembly;
	IfElse ifElse;
	public OpType getType() {
		return type;
	}

	public void setType(OpType type) {
		this.type = type;
	}

	public Expression getExpression() {
		return expression;
	}

	public void setExpression(Expression expression) {
		this.expression = expression;
	}

	public String getAssembly() {
		return assembly;
	}

	public void setAssembly(String assembly) {
		this.assembly = assembly;
	}

	public void setIfElse(Expression condition, String ifFunc, String elseFunc) {
		this.expression=condition;
		this.ifElse=new IfElse(ifFunc,elseFunc);
		
	}

	public IfElse getIfElse() {
		return ifElse;
	}

	public void setIfElse(IfElse ifElse) {
		this.ifElse = ifElse;
	}

	
}
