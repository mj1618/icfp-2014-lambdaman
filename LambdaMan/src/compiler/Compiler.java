package compiler;

import java.io.File;
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
		asm.add("RTN");
		f.setAssembly(asm);
	}

	private List<String> compileExpression(Expression e, Function f) {
		List<String> asm = new ArrayList<String>();
		if(e.isLeaf()){
			if(Utils.IsInteger(e.getValue())){
				asm.add("LDC "+e.getValue());
			} else if (f.parameterIndex(e.getValue())>0) {
				asm.add("LD 0 "+f.parameterIndex(e.getValue()));
			} else {
				asm.add("LDF "+e.getValue());
			}
		} else {
			if(functions.get(e.getValue())==null){
				Debug.error("Error, "+e.getValue()+" is not a function but is being called as one");
			} else {

				int nargs = functions.get(e.getValue()).getParams().size();
				if(nargs!=e.getChildren().size()){
					Debug.error("Arguments and children mismatch in expression:"+e+" function:"+f.getName()+" args needed:"+nargs+" args got:"+e.getChildren().size());
				}
				for(Expression exp:e.getChildren()){
					asm.addAll(compileExpression(exp,f));
				}
				asm.add("LDF "+e.getValue());

				asm.add("AP "+nargs);
			}
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

	private static Compiler Instance(File file) {
		Parser p = Parser.Instance(new File("hlscripts\\parsetest.hla"));
		p.preprocess();
		return new Compiler(p.getFunctions());
	}
	
	public static void main(String args[]){
		Compiler c = Compiler.Instance(new File("hlscripts\\parsetest.hla"));
		c.compile();
		c.print();
	}
}
