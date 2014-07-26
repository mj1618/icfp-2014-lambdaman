package compiler.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import compiler.types.OpType;

public class Function {

	boolean isIfElse = false;
	int line;
	String name;
	List<Parameter> params = new ArrayList<Parameter>();
	List<Operation> operations = new ArrayList<Operation>();
	Map<String, Constant> constants = new HashMap<String,Constant>();
	List<String> assembly = new ArrayList<String>();
	
	public boolean isIfElse() {
		return isIfElse;
	}

	public void setIfElse(boolean isIfElse) {
		this.isIfElse = isIfElse;
	}

	public List<String> getAssembly() {
		return assembly;
	}

	public void setAssembly(List<String> assembly) {
		this.assembly = assembly;
	}

	public Map<String, Constant> getConstants() {
		return constants;
	}

	public void setConstants(Map<String, Constant> constants) {
		this.constants = constants;
	}

	public void addConstant(Constant c){
		constants.put(c.getName(), c);
	}
	
	public List<Operation> getOperations() {
		return operations;
	}


	public void setOperations(List<Operation> operations) {
		this.operations = operations;
	}


	public Function(){}
	

	public List<Parameter> getParams() {
		return params;
	}

	public void setParams(List<Parameter> params) {
		this.params = params;
	}

	public void addParam(String p) {
		params.add(new Parameter(p));
	}

	public int getLine() {
		return line;
	}

	public void setLine(int line) {
		this.line = line;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	public void addAllOperations(List<Operation> ops) {
		operations.addAll(ops);
	}

	public void addOperation(Operation o) {
		operations.add(o);
	}

	public String toString(){
		String s="";
		
		s="def "+name;
		for(Parameter p:params){
			s+=" "+p.getName();
		}
		s+="\n";

		for(Constant c:getConstants().values()){
			s+="let "+c.getName()+" = "+c.getExpression()+"\n";
			
		}
		for(Operation o:getOperations()){
			if(o.getType()==OpType.RETURN)
				s+="return ";
			s+=o.getExpression()+"\n";
		}
		
		return s;
	}

	public int parameterIndex(String value) {
		
		for(int i = 0; i<this.params.size(); i++){
			if(params.get(i).getName().equals(value))
				return i;
		}
		return -1;
	}

	public void addAllConstants(Map<String, Constant> constants) {
		this.constants.putAll(constants);
	}

	public void addAllParams(List<Parameter> params) {
		this.params.addAll(params);
	}
	
}
