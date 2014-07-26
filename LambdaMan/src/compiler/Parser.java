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
import java.util.LinkedHashMap;
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

	Map<String,Function> functions = new LinkedHashMap<String,Function>();
	
	public Map<String, Function> getFunctions() {
		return functions;
	}

	public void setFunctions(Map<String, Function> functions) {
		this.functions = functions;
	}

	File f=new File(".");
	public Parser(File f){
		this.f=f;
	}
	public List<String> importLines(List<String> lines) throws IOException{
		List<String> imports = new ArrayList<String>();
		
		for(int i = 0; i<lines.size()&&lines.get(i).startsWith("import"); i++){
			String line = lines.get(i);
			List<String> newImports = readImport(line.split(" ")[1]);
			imports.addAll(newImports);
			imports.addAll(importLines(newImports));
		}
		return imports;
	}
	private List<String> readImport(String s) throws IOException {
		File file = new File(f.getParent(), s);
		try {
			List<String> imports= Files.readAllLines(Paths.get(file.getPath()), Charset.defaultCharset());
			
			return imports;
		} catch (IOException e) {
			
			e.printStackTrace();
			return null;
		}
	}

	public void init(List<String> lines){
		//trimBlankLines(lines);
		
		//read imports
		
		try {
			lines.addAll(importLines(lines));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ListIterator<String> it = lines.listIterator();
		while(it.hasNext()){
			String line = it.next();
			if(line.startsWith("import")==false){
				it.previous();
				break;
			}
		}
		while(it.hasNext()){
			
			Function f = readFunctionDeclaration(it);
			parseLines(it,f);
			functions.put(f.getName(),f);
		}
	}
	
	private void useImport(String s) {
		
		
	}

	private void parseLines(ListIterator<String> it, Function f) {
		while(it.hasNext()){
			String original = it.next();
			String line = original.trim();
			if (line.isEmpty() || line.startsWith(";"))
				continue;
			boolean end = parseLine(it.nextIndex(),line,f,it,original);
			if(end)
				return;
		}
	}
	
	private boolean parseLine(int lineNum, String line, Function f, ListIterator<String> it,String original){
		String[] code = line.split(" ");
		
		if(code[0].equals("end") || code[0].equals("else") || code[0].equals("endif")){
			return true;
			
		} else if(code[0].equals("let")) {
			Constant c = new Constant();
			c.setName(code[1]);
			String value = line.split("=")[1].substring(1);
			Expression exp = Expression.GetExpression(lineNum, value);
			c.setExpression(exp);
			f.addConstant(c);
			
		} else if(code[0].equals("return")){
			Operation o = new Operation();
			o.setType(OpType.RETURN);
			String value = line.substring("return ".length());
			o.setExpression(Expression.GetExpression(lineNum, value));
			f.addOperation(o);
			
		} else if(Assembly.IsAssembly(code[0])){
			Operation o = new Operation();
			o.setType(OpType.ASSEMBLY);
			o.setAssembly(line);
			f.addOperation(o);
			
		}  else if(code[0].equals("if")){
			Operation o = new Operation();
			o.setType(OpType.IF);
			Expression condition= Expression.GetExpression(lineNum,line.substring("if ".length()));
			String ifFunc = createIfFunction(it,f);
			
			String elseFunc = createElseFunction(it,f);
			
			o.setIfElse(condition, ifFunc, elseFunc);
			f.addOperation(o);
			
		} else {
			Operation o = new Operation();
			o.setType(OpType.FUNCTION_CALL);
			o.setExpression(Expression.GetExpression(lineNum, line));
			f.addOperation(o);
		}
		return false;
	}

	private String createElseFunction(ListIterator<String> it,
			Function parent) {
		Function f = new Function();
		f.addAllConstants(parent.getConstants());
		f.addAllParams(parent.getParams());
		f.setName(parent.getName()+"Else");
		f.setIfElse(true);
		parseLines(it,f);
		functions.put(f.getName(), f);
		return f.getName();
	}

	private String createIfFunction(ListIterator<String> it, Function parent) {
		Function f = new Function();
		f.addAllConstants(parent.getConstants());
		f.addAllParams(parent.getParams());
		f.setName(parent.getName()+"If");
		f.setIfElse(true);
		parseLines(it,f);
		functions.put(f.getName(), f);
		return f.getName();
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
		for(Constant c:constants.values()){
			replaceConstants(c.getExpression(), constants);
		}
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
		System.err.println("functions:"+functions.size());
		for(String fn : functions.keySet()){
			Function f = functions.get(fn);
			Debug.test(f.toString());
		}
	}
	
	public Function readFunctionDeclaration(ListIterator<String> it) {
		Function f = new Function();
		
		while(it.hasNext()){
			String line = it.next().trim();
			if (line.isEmpty() || line.startsWith(";"))
				continue;
			
			if(line.startsWith("def")){
				String[] decl = line.split(" ");
				f.setLine(it.nextIndex());
				f.setName(decl[1]);
				for(int i = 2; i<decl.length; i++){
					f.addParam(decl[i]);
				}
				break;
			} else {
				System.err.println(it.nextIndex() + ": error on line, expecting a function:"+line);
			}
		}
		
		return f;
	}

	public static Parser Instance(File file){
		try {
			return Instance(Files.readAllLines(Paths.get(file.getPath()), Charset.defaultCharset()), file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public static Parser Instance(List<String> lines, File f){
		Parser c = new Parser(f);
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
			return Instance(lines, new File("."));
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
