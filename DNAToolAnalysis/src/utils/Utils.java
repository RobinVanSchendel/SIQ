package utils;

public class Utils {
	public static String reverseComplement(String dna){
		StringBuffer dnaRev = new StringBuffer(dna).reverse();
		StringBuffer revCom = new StringBuffer();
		for(char c: dnaRev.toString().toCharArray()){
			switch(c){
				case 'a':
					revCom.append('t');
					break;
				case 'A':
					revCom.append('T');
					break;
				case 't':
					revCom.append('a');
					break;
				case 'T':
					revCom.append('A');
					break;
				case 'c':
					revCom.append('g');
					break;
				case 'C':
					revCom.append('G');
					break;
				case 'g':
					revCom.append('c');
					break;
				case 'G':
					revCom.append('C');
					break;
				case 'N':
					revCom.append('N');
					break;
				case 'n':
					revCom.append('n');
					break;
				//for second hit only!
				case 'x':
					revCom.append('x');
					break;
				default:
					System.out.println("Can't complement "+dna);
					System.out.println("Can't complement "+c);
					//System.exit(0);
			}
		}
		return revCom.toString();
	}

	public static String longestCommonSubstring(String S1, String S2)
	{
		int Start = 0;
	    int Max = 0;
	    for (int i = 0; i < S1.length(); i++)
	    {
	        for (int j = 0; j < S2.length(); j++)
	        {
	            int x = 0;
	            while (Character.toUpperCase(S1.charAt(i + x)) == Character.toUpperCase(S2.charAt(j + x)))
	            {
	                x++;
	                if (((i + x) >= S1.length()) || ((j + x) >= S2.length())) break;
	            }
	            if (x > Max)
	            {
	                Max = x;
	                Start = i;
	            }
	         }
	    }
	    return S1.substring(Start, (Start + Max));
	}

	public static String getHomologyAtBreak(String left, String del, String right) {
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
}
