package compiler;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.StringBuilder;
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
	String path;
	public Compiler(Map<String,Function> functions){
		this.functions=functions;
	}
	
	public Compiler(Map<String, Function> functions, String path) {
		this.functions=functions;
		this.path=path;
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
				if(f.isIfElse())asm.add("JOIN");
				else asm.add("RTN");
				break;
			case FUNCTION_CALL:
				asm.addAll(compileExpression(o.getExpression(),f));
				break;
			case IF:
				asm.addAll(compileExpression(o.getExpression(),f));
				asm.add("SEL "+o.getIfElse().getIfFunction()+" "+o.getIfElse().getElseFunction());				
			}
		}
		if(f.isIfElse())asm.add("JOIN");
		else asm.add("RTN");
		f.setAssembly(asm);
	}

	private List<String> compileExpression(Expression e, Function f) {
		List<String> asm = new ArrayList<String>();
		if(IsFunction(e,f)==false){
			if(Utils.IsInteger(e.getValue())){
				asm.add("LDC "+e.getValue());
			} else if (f.parameterIndex(e.getValue())>=0) {
				asm.add("LD 0 "+f.parameterIndex(e.getValue()) + "   ; " + e.getValue());
			} else {
				asm.add("LDF "+e.getValue());
			}
		} else if(IsPassedFunction(e,f)){
			for(Expression exp:e.getChildren()){
				asm.add("LD 0 "+f.parameterIndex(exp.getValue()));
			}
			asm.add("LD 0 "+f.parameterIndex(e.getValue()));
			
			asm.add("AP "+e.getChildren().size());
		} else {
			int nargs = functions.get(e.getValue()).getParams().size();
			if(nargs!=e.getChildren().size()){
				if(e.getChildren().size()==0){
					//closure
					asm.add("LDF "+e.getValue());
				} else {
					Debug.error("Arguments and children mismatch in expression:"+e+" function:"+f.getName()+" args needed:"+nargs+" args got:"+e.getChildren().size());
				}
			} else {
				for(Expression exp:e.getChildren()){
					asm.addAll(compileExpression(exp,f));
				}
				asm.add("LDF "+e.getValue());
	
				asm.add("AP "+nargs);
			}
		}
		return asm;
	}
	
	private boolean IsFunction(Expression e, Function f) {
		return IsPureFunction(e) || IsPassedFunction(e,f);
	}

	private boolean IsPureFunction(Expression e) {
		
		return functions.containsKey(e.getValue());
	}
	private boolean IsPassedFunction(Expression e, Function f) {
		
		return e.getChildren().size()>0 && f.parameterIndex(e.getValue())>=0;
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
		StringBuilder s = new StringBuilder();
		
		Function main = functions.get("Main");
		s.append(String.format("%-10s ; %s\n", main.getName() + ":", main.getSignature()));
		for(String asm:main.getAssembly()){
			s.append("  ").append(asm).append("\n");
		}
		
		for(Function f: functions.values()){
			if(f.getName().equals("Main"))continue;
			s.append(String.format("%-10s ; %s\n", f.getName() + ":", f.getSignature()));
			for(String asm:f.getAssembly()){
				s.append("  ").append(asm).append("\n");
			}
		}
		return s.toString();
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

	static Compiler Instance(File file) throws IOException {
		Parser p = Parser.Instance(file);
		p.preprocess();
		return new Compiler(p.getFunctions(), file.getCanonicalPath());
	}
	
	public static void main(String args[]){
		File f = (args.length > 1) ? new File(args[0]) : new File(new File("hlscripts"), "game_test.hla");
		Compiler c;
		try {
			c = Compiler.Instance(f);
			c.compile();
//			c.printCompiled();
			
			c.assemble();
			c.printMachineCode();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
