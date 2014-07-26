package compiler.components;

import compiler.expressions.Expression;
import compiler.types.OpType;

public class Operation {

	OpType type;
	Expression expression;
	String assembly;
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

	
}
