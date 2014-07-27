package compiler.components;

import java.util.List;

import compiler.expressions.Condition;
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
		if(type==OpType.CONDITION)
			return ifElse.getIfcond().getExp();
		else
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
	public void setIfElse(Condition ifFunc, Condition elseFunc) {
		
		this.ifElse=new IfElse(ifFunc,elseFunc);
		
	}

	public IfElse getIfElse() {
		return ifElse;
	}

	public void setIfElse(IfElse ifElse) {
		this.ifElse = ifElse;
	}

	
}
