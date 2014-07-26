package compiler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import compiler.components.*;
import compiler.expressions.Expression;
import compiler.utils.Utils;

public class Compiler {

	Map<String,Function> functions;
	public Compiler(Map<String,Function> functions){
		this.functions=functions;
	}
	
	public void compile(){
		for(Function f: functions.values()){
			compileFunction(f);
		}
	}
	
	private void compileFunction(Function f) {
		List<String> asm = new ArrayList<String>();
		
		for(Operation o:f.getOperations()){
			switch(o.getType()){
			case ASSEMBLY:
				asm.add(o.getAssembly());
				break;
			case RETURN:
				asm.addAll(compileExpression(o.getExpression(),f));
				asm.add("RTN");
				break;
			case FUNCTION_CALL:
				asm.addAll(compileExpression(o.getExpression(),f));
				break;
			}
		}
	}

	private List<String> compileExpression(Expression e, Function f) {
		List<String> asm = new ArrayList<String>();
		if(e.isLeaf()){
			if(Utils.IsInteger(e.getValue())){
				asm.add("LDC "+e.getValue());
			} else {
				asm.add("LDF "+e.getValue());
			}
		} else {
			int nargs = functions.get(e.getValue()).getParams().size();
			if(nargs!=e.getChildren().size()){
				Debug.error("Arguments and children mismatch in expression:"+e+" function:"+f.getName());
			}
			asm.add("DUM "+nargs);
			for(Expression exp:e.getChildren()){
				asm.addAll(compileExpression(exp,f));
			}
			asm.add("LDF "+e.getValue());
			asm.add("RAP "+nargs);
		}
		return asm;
	}
	
	public void print(){
		for(Function f: functions.values()){
			System.out.println(f.getName()+":");
			for(String s:f.getAssembly()){
				System.out.println(s);
			}
		}
	}
}
