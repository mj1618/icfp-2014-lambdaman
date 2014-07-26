package compiler;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import asm.Asm;
import compiler.components.*;
import compiler.expressions.Expression;
import util.Utils;

public class Compiler {

	Map<String,Function> functions;
	List<String> machineCode = new ArrayList<String>();
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
			} else if (f.parameterIndex(e.getValue())>=0) {
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
	
	public void printMachineCode(){
		for(String line:machineCode){
			System.out.println(line);
		}
	}
	
	public void printCompiled(){
		System.out.println(compiledToString());
	}
	
	public String compiledToString(){
		String s ="";
		for(Function f: functions.values()){
			s+=f.getName()+":"+"\n";
			for(String asm:f.getAssembly()){
				s+=asm+"\n";
			}
		}
		return s;
	}
	
	public void assemble(){
		
		try {
			BufferedReader br = Asm.Assemble(new StringReader(compiledToString()));
			if(br==null){
				Debug.error("Failed to assemble");
				return;
			}
			String line;
			while((line=br.readLine())!=null){
				machineCode.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Compiler Instance(File file) {
		Parser p = Parser.Instance(file);
		p.preprocess();
		return new Compiler(p.getFunctions());
	}
	
	public static void main(String args[]){
		File f = (args.length > 1) ? new File(args[0]) : new File(new File("hlscripts"), "parsetest.hla");
		Compiler c = Compiler.Instance(f);
		c.compile();
		//c.printCompiled();
		
		c.assemble();
		c.printMachineCode();
	}
}
