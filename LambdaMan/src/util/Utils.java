package util;

public class Utils {
	public static boolean IsInteger(String s){
		try{
			Integer.parseInt(s);
			return true;
		} catch(Exception e){
			return false;
		}
	}
}
