package compiler.expressions;

import java.util.ArrayList;
import java.util.List;

public class Expression {

	
	String value=null;
	List<Expression> children = new ArrayList<Expression>();
	
	public boolean isLeaf(){
		return children.size()==0;
	}
	
	public Expression(String s){
		value=s;
	}
	public Expression(){
		
	}
	static int i = 0;
	
	public static Expression GetExpression(String value){
		i=0;
		return from("("+value+")");
	}
	
	private static Expression from(String value) {
		boolean hasChildren = true;
		if(value.charAt(i)=='(')i++;
		else hasChildren = false;
		
		String current="";
		Expression n = new Expression();
		
		//read function name
		for(; i<value.length(); i++){
			if(value.charAt(i)==' '){
				n.value = current;
				break;
			} else if(value.charAt(i)==')'){
				n.value = current;
				return n;
			} else if(value.charAt(i)=='('){
				System.out.println("error open bracket not expected");
			} else {
				current+=value.charAt(i);
			}
		}
		
		if(!hasChildren)return n;
		i++;
		for(; i<value.length(); i++){
			n.children.add(from(value));
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
		System.out.println(Expression.from("(0)").toString());
	}
}
