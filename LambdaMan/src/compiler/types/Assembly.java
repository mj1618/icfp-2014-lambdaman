package compiler.types;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Assembly {

	
	public static String[] OpCodes = {"LDC","LD","ADD","SUB","MUL","DIV","CEQ","CGT","CGTE","ATOM","CONS","CAR","CDR","SEL","JOIN","LDF","AP","RTN","DUM","RAP","STOP","TSEL","TAP","TRAP","ST"};

	public static boolean IsAssembly(String s) {
		Set<String> codes = new HashSet<String>(Arrays.asList(OpCodes));
		return codes.contains(s);
	}
	
}
