package data;

import java.util.ArrayList;

public class Utils {
	public static String reverseComplement(String dna){
		if(dna==null) {
			return null;
		}
		StringBuffer dnaRev = new StringBuffer(dna).reverse();
		String revCom = "";
		for(char c: dnaRev.toString().toCharArray()){
			switch(c){
				case 'A':
					revCom += 'T';
					break;
				case 'a':
					revCom += 't';
					break;
				case 'T':
					revCom += 'A';
					break;
				case 't':
					revCom += 'a';
					break;
				case 'C':
					revCom += 'G';
					break;
				case 'c':
					revCom += 'g';
					break;
				case 'G':
					revCom += 'C';
					break;
				case 'g':
					revCom += 'c';
					break;
				case 'n':
					revCom += 'n';
					break;
				case 'N':
					revCom += 'N';
					break;
				default:
					System.err.println("cannot do anything with character: "+c);
			}
		}
		return revCom;
	}

	public static String getHomology(String left, String del, String right) {
		if(left == null || right == null){
			return "";
		}
		//make them the same case:
		left = left.toLowerCase();
		del = del.toLowerCase();
		right = right.toLowerCase();
		//check the left part
		left = new StringBuffer(left).reverse().toString();
		del = new StringBuffer(del).reverse().toString();
		//System.out.println("leftrev:"+left);
		//System.out.println("delrev: "+del);
		int index = 0;
		while(left.charAt(index) == del.charAt(index)){
			index++;
			if(index == left.length() || index == del.length()){
				break;
			}
		}
		//System.out.println("index:"+index);
		String leftHom = left.substring(0, index);
		//System.out.println("leftHom:"+leftHom);
		leftHom = new StringBuffer(leftHom).reverse().toString();
		
		//right part
		//System.out.println("right:"+right);
		//turn it again back around
		del = new StringBuffer(del).reverse().toString();
		//System.out.println("del:  "+del);
		index = 0;
		while(right.charAt(index) == del.charAt(index)){
			index++;
			if(index == right.length() || index == del.length()){
				break;
			}
		}
		String rightHom = right.substring(0, index);
		//System.out.println("rightHom:"+rightHom);
		//System.out.println("------------------");
		String ret = leftHom+rightHom;
		if(ret.length()> del.length()){
			ret = del;
		}
		return ret;
	}

	public static String toString(ArrayList<String> support, String delim) {
		String ret = "";
		for(String s: support) {
			if(ret.length()>0) {
				ret+=delim;
			}
			ret+=s;
		}
		return ret;
	}
}
