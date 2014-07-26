package compiler;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import compiler.components.Constant;
import compiler.components.Function;
import compiler.components.Operation;
import compiler.expressions.Expression;
import compiler.types.Assembly;
import compiler.types.OpType;

public class Parser {

	Map<String,Function> functions = new HashMap<String,Function>();
	
	public Map<String, Function> getFunctions() {
		return functions;
	}

	public void setFunctions(Map<String, Function> functions) {
		this.functions = functions;
	}

	public Parser(){}
	
	public void init(List<String> lines){
		trimBlankLines(lines);
		ListIterator<String> i = lines.listIterator();
		while(i.hasNext()){
			Function f = ReadFunctionDeclaration(i);
			ParseLines(i,f);
			functions.put(f.getName(),f);
		}
	}
	
	private static void ParseLines(ListIterator<String> it, Function f) {
		while(it.hasNext()){
			String line = it.next().trim();
			boolean end = ParseLine(line,f);
			if(end)
				return;
		}
	}
	
	private static boolean ParseLine(String line, Function f){
		String[] code = line.split(" ");
		
		if(code[0].equals("end")){
			return true;
			
		} else if(code[0].equals("let")) {
			Constant c = new Constant();
			c.setName(code[1]);
			String value = line.split("=")[1].substring(1);
			Expression exp = Expression.GetExpression(value);
			c.setExpression(exp);
			f.addConstant(c);
			
		} else if(code[0].equals("return")){
			Operation o = new Operation();
			o.setType(OpType.RETURN);
			String value = line.substring("return ".length());
			o.setExpression(Expression.GetExpression(value));
			f.addOperation(o);
			
		} else if(Assembly.IsAssembly(code[0])){
			Operation o = new Operation();
			o.setType(OpType.ASSEMBLY);
			o.setAssembly(line);
			
		} else {
			Operation o = new Operation();
			o.setType(OpType.FUNCTION_CALL);
			o.setExpression(Expression.GetExpression(line));
		}
		return false;
	}

	public void preprocess(){
		for(String fn : functions.keySet()){
			Function f = functions.get(fn);
			for(Operation o:f.getOperations()){
				replaceConstants(o, f.getConstants());
			}
		}
	}

	private void replaceConstants(Operation o, Map<String, Constant> constants) {
		if(o.getType()!=OpType.ASSEMBLY){
			replaceConstants(o.getExpression(), constants);
		}
	}

	private void replaceConstants(Expression exp,
			Map<String, Constant> constants) {
	
		if(exp.isLeaf()==false){
			for(int i = 0; i<exp.getChildren().size(); i++){
				Expression e = exp.getChildren().get(i);
				if(e.isLeaf()){
					if(constants.containsKey(e.getValue())){
						exp.getChildren().set(i, constants.get(e.getValue()).getExpression());
					}
				} else
					replaceConstants(e, constants);
			}
		}
		
	}

	public void print(){
		System.out.println("functions:"+functions.size());
		for(String fn : functions.keySet()){
			Function f = functions.get(fn);
			Debug.test(f.toString());
		}
	}
	
	public static Function ReadFunctionDeclaration(ListIterator<String> it) {
		Function f = new Function();
		
		while(it.hasNext()){
			String line = it.next().trim();
			
			if(line.startsWith("def")){
				String[] decl = line.split(" ");
				f.setName(decl[1]);
				for(int i = 2; i<decl.length; i++){
					f.addParam(decl[i]);
				}
				break;
			} else {
				System.out.println("error on line, expecting a function:"+line);
			}
		}
		
		return f;
	}

	
	private void trimBlankLines(List<String> lines) {
		ListIterator<String> i = lines.listIterator();
		while(i.hasNext()){
			String line = i.next();
			if(line.trim().isEmpty()){
				i.remove();
			}
		}
	}
	
	
	public static Parser Instance(File file){
		try {
			return Instance(Files.readAllLines(Paths.get(file.getPath()), Charset.defaultCharset()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public static Parser Instance(List<String> lines){
		Parser c = new Parser();
		c.init(lines);
		return c;
	}
	
	public static Parser Instance(InputStream is){
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			List<String> lines = new ArrayList<String>();
			String line;
			while( (line=br.readLine())!=null && line.equals("END")!=true){
				lines.add(line);
			}
			return Instance(lines);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public static void main(String args[]){
		Parser c = Parser.Instance(new File("hlscripts\\parsetest.hla"));
		c.preprocess();
		c.print();
	}
}
