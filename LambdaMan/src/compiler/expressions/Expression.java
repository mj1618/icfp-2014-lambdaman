package compiler.expressions;

import java.util.ArrayList;
import java.util.List;

public class Expression {
	public final int lineNum;
	
	String value=null;
	List<Expression> children = new ArrayList<Expression>();
	
	

	public boolean isLeaf(){
		return children.size()==0;
	}
	
	public Expression(int lineNum, String s){
		this.lineNum = lineNum;
		value=s;
	}
	public Expression(int lineNum){
		this.lineNum = lineNum;
	}
	static int i = 0;
	
	public static Expression GetExpression(int lineNum, String value){
		i=0;
		return from(lineNum, "("+value+")");
	}
	
	private static Expression from(int lineNum, String value) {
		boolean hasChildren = true;
		if(value.charAt(i)=='(')i++;
		else hasChildren = false;
		
		String current="";
		Expression n = new Expression(lineNum);
		
		//read function name
		for(; i<value.length(); i++){
			if(value.charAt(i)==' '){
				n.value = current;
				break;
			} else if(value.charAt(i)==')'){
				n.value = current;
				return n;
			} else if(value.charAt(i)=='('){
				System.err.println(lineNum + ": error open bracket not expected");
			} else {
				current+=value.charAt(i);
			}
		}
		
		if(!hasChildren)return n;
		while(value.charAt(i) == ' ') i++;
		for(; i<value.length(); i++){
			n.children.add(from(lineNum, value));
			if(i<value.length() && value.charAt(i)==')'){
				i++;
				return n;
			}
		}
		
		return n;
	}
	
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public List<Expression> getChildren() {
		return children;
	}
	public void setChildren(List<Expression> children) {
		this.children = children;
	}
	public String toString(){
		String s = "";
		Expression n = this;
		if(n.children.size()>0)s+="(";
		
		s += n.value;
		
		for(Expression nc:n.children){
			s+=" "+nc.toString();
		}
		
		if(n.children.size()>0)s+=")";
		
		return s;
	}
	
	public static void main(String args[]){
		System.out.println(Expression.from(0, "(0)").toString());
	}
}
