package data;

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
}
