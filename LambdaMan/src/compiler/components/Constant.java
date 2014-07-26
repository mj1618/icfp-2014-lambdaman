package compiler.components;

import compiler.expressions.Expression;

public class Constant {

	String name;
	Expression expression;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	public Expression getExpression() {
		return expression;
	}

	public void setExpression(Expression expression) {
		this.expression = expression;
	}
	
	
}
