package Utils;

public class InsertionSolver {
	private String dna, insertion;
	private int minimumMatch = -1;
	private String matchS = "", posS = "", subS = "", lengthS = "";
	private int lastMatchPos = -1;
	private String substituteString = "<>";
	private String id;
	private int adjustedPosition;
	private boolean searchFromLeft;
	
	public InsertionSolver(String dna, String insertion, String id, boolean searchFromLeft){
		this.dna = dna.toLowerCase();
		this.insertion = insertion.toLowerCase();
		this.id = id;
		this.searchFromLeft = searchFromLeft;
		checkDNA();
		//System.out.println("received DNA and insertion");
		//System.out.println("DNA "+dna);
		//System.out.println("insertion "+insertion);
	}
	public InsertionSolver(String dna, String insertion, String id){
		this.dna = dna.toLowerCase();
		this.insertion = insertion.toLowerCase();
		this.id = id;
		this.searchFromLeft = true;
		checkDNA();
		//System.out.println("received DNA and insertion");
		//System.out.println("DNA "+dna);
		//System.out.println("insertion "+insertion);
	}
	private void checkDNA() {
		if(!dna.matches("[atgc]*")){
			System.err.println("DNA contains different sequence than [agtc]");
		}
		if(!insertion.matches("[atgc]*")){
			System.err.println("insertion contains different sequence than [agtc]");
		}
	}
	public void setMinimumMatch(int min){
		this.minimumMatch = min;
	}
	public void solveInsertion(String id){
		substituteString = "<"+id+">";
		solveInsertionInternal();
		
	}
	public void solveInsertion(){
		solveInsertionInternal();
	}
	private void solveInsertionInternal(){
		if(minimumMatch<0){
			System.err.println("We cannot solve the insertion, because the minimum is not set");
			return;
		}
		if(minimumMatch>insertion.length()){
			System.err.println("We cannot solve the insertion, because the minimum is larger than the insertion");
			return;
		}
		subS = insertion;
		String lcs = longestCommonSubstring(dna, subS);
		int lengthLCS = lcs.length();
		//System.out.println(lcs + lengthLCS);
		while(lengthLCS >= minimumMatch){
			//System.out.println(lcs + lengthLCS);
			addMatchS(lcs); 
			addPosS(lastMatchPos);
			subS = setSubS(lcs);
			assLengthS(lengthLCS);
			lcs = longestCommonSubstring(dna, subS);
			lengthLCS = lcs.length();
		}
		
	}
	private void assLengthS(int lengthLCS) {
		if(this.lengthS.length()>0){
			lengthS += ";";
		}
		lengthS += lengthLCS;
	}
	private String setSubS(String lcs) {
		return subS.replace(lcs, substituteString);
	}
	private void addPosS(int pos) {
		if(this.posS.length()>0){
			posS += ";";
		}
		posS += adjustedPosition+pos;
		
	}
	private void addMatchS(String lcs) {
		if(this.matchS.length()>0){
			matchS += ";";
		}
		matchS += lcs;
	}
	/**get the longest common substring from two strings
	 * 
	 * @param S1
	 * @param S2
	 * @return
	 */
	public String longestCommonSubstring(String S1, String S2)
	{
	    if(S1 == null || S2 == null){
	    	return "";
	    }
		S1 = S1.toLowerCase();
	    S2 = S2.toLowerCase();
		int Start = 0;
	    int Max = 0;
	    for (int i = 0; i < S1.length(); i++)
	    {
	        for (int j = 0; j < S2.length(); j++)
	        {
	            int x = 0;
	            while (S1.charAt(i + x) == S2.charAt(j + x))
	            {
	                x++;
	                if (((i + x) >= S1.length()) || ((j + x) >= S2.length())) break;
	            }
	            if(searchFromLeft && x > Max)
	            {
	                Max = x;
	                Start = i;
	            }
	            //take the last one
	            if(!searchFromLeft && x >= Max)
	            {
	                Max = x;
	                Start = i;
	            }
	         }
	    }
	    lastMatchPos = Start;
	    return S1.substring(Start, (Start + Max));
	}
	public String toString(){
		String ret = "";
		String s = "\t";
		ret += id+s+dna+s+insertion+s+insertion.length()+s+subS+s+matchS+s+posS+s+lengthS+s+getType()+s+getLargestMatch()+s+getFirstPos()+s+(getFirstPos()+getLargestMatch());
		return ret;
	}
	public String getMatchS(){
		return matchS;
	}
	public String getPosS(){
		return posS;
	}
	public String getSubS(){
		return subS;
	}
	public int getLargestMatch(){
		if(lengthS.length()>0){
			String[] parts = lengthS.split(";");
			return Integer.parseInt(parts[0]);
		}
		return -1;
	}
	public int getFirstPos(){
		if(posS.length()>0){
			String[] parts = posS.split(";");
			return Integer.parseInt(parts[0]);
		}
		return -1;
	}
	public String getType(){
		if(this.getSubS().matches("[atgc]*")){
			return "NOT SOLVED";
		}
		else if(this.getSubS().replaceAll(substituteString, "").length() == 0){
			return "SOLVED";
		}
		else if(this.getSubS().replaceAll(substituteString, "").length() <= 4){
			return "ALMOST SOLVED";
		}
		else{
			return "PARTIALLY SOLVED";
		}
	}
	public void setAdjustedPosition(int i) {
		this.adjustedPosition = i;
	}
	public boolean hasMultipleMatches(){
		return this.matchS.contains(";");
	}
}
