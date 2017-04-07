package utils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import org.biojava.bio.seq.Sequence;
import org.apache.commons.math3.distribution.BinomialDistribution;


public class InsertionSolverTwoSides {
	private String left, right, insertion;
	private int minimumMatch = -1;
	private String matchS = "", posS = "", subS = "", lengthS = "", subS2 = "";
	private int lastMatchPos = -1;
	private String substituteString = "<>";
	private String id;
	private int adjustedPositionLeft;
	private boolean searchFromLeft;
	private int adjustedPositionRight;
	private String stringS = "";
	private ArrayList<String> strings = new ArrayList<String>();
	private String tDNA = "";
	private String tDNArc = "";
	//random tests showed that 12, actually 13 is a very safe number
	private int tDNAHitminium = 12;
	//private int tDNAHitminium = 10;
	private int currentIndex = 0;
	private double AT;
	private double GC;
	private boolean searchRC;
	private String firstHit = "";
	private boolean searchNonRC;
	private boolean firstHitIsTDNA;
	private boolean firstHitIsTDNADirectPos;
	private String tDNAname;
	private Vector<Sequence> tDNAVector;
	private Vector<String> isParts;
	
	public InsertionSolverTwoSides(String left, String right, String insertion, String id){
		this.left = left.toLowerCase();
		this.right = right.toLowerCase();
		this.insertion = insertion.toLowerCase();
		this.id = id;
		checkDNA();
		strings.add(insertion);
		subS = this.insertion;
		subS2 = this.insertion;
		//System.out.println("received DNA and insertion");
		//System.out.println("DNA "+dna);
		//System.out.println("insertion "+insertion);
		isParts = new Vector<String>();
	}
	private void checkDNA() {
		if(!left.matches("[atgc]*")){
			System.err.println("left DNA contains different sequence than [agtc]");
			System.err.println(left);
		}
		if(!right.matches("[atgc]*")){
			System.err.println("right DNA contains different sequence than [agtc]");
			System.err.println(right);
		}
		if(!insertion.matches("[atgc]*")){
			System.err.println("insertion contains different sequence than [agtc]");
			System.err.println(insertion);
		}
	}
	public void setMinimumMatch(int min, boolean safety){
		this.minimumMatch = min;
		//hack to prevent that large insertion get searched extensively
		if(safety && this.insertion.length()>=50 && this.minimumMatch<=6){
			this.minimumMatch = 6;
		}
		if(safety && this.insertion.length()>=100 && this.minimumMatch<=6){
			this.minimumMatch = 8;
		}
	}
	public void solveInsertion(String id){
		substituteString = "<"+id+">";
		solveInsertionInternal();
	}
	public void solveInsertion(){
		solveInsertionInternal();
	}
	private void solveInsertionInternal(){
		if(!this.searchRC && !this.searchNonRC){
			System.out.println("you have to search one of the two sides");
			System.exit(0);
		}
		//System.out.println("start");
		if(minimumMatch<0){
			System.err.println("We cannot solve the insertion, because the minimum is not set");
			return;
		}
		if(minimumMatch>insertion.length()){
			System.err.println("We cannot solve the insertion, because the minimum is larger than the insertion");
			return;
		}
		String lcsLeft = "";
		String lcsRight = "";
		int posLeft = Integer.MAX_VALUE;
		int posRight = Integer.MAX_VALUE;
		int lengthLCSLeft = -1;
		int lengthLCSRight = -1;
		
		if(this.searchNonRC){
			this.searchFromLeft = false;
			lcsLeft = longestCommonSubstring(left, subS);
			posLeft = this.lastMatchPos;
			this.searchFromLeft = true;
			lcsRight = longestCommonSubstring(right, subS);
			posRight = this.lastMatchPos;
			lengthLCSLeft = countLowerCase(lcsLeft);
			lengthLCSRight = countLowerCase(lcsRight);
		}
		String lcsLeftRC = "";
		String lcsRightRC = "";
		int lengthLCSRCLeft = -1;
		int lengthLCSRCRight = -1;
		int posLeftRC = Integer.MAX_VALUE;
		int posRightRC = Integer.MAX_VALUE;
		if(this.searchRC){
			this.searchFromLeft = false;
			lcsLeftRC = longestCommonSubstring(left, Utils.reverseComplement(subS));
			lengthLCSRCLeft = countLowerCase(lcsLeftRC);
			posLeftRC = this.lastMatchPos;
			this.searchFromLeft = true;
			lcsRightRC = longestCommonSubstring(right, Utils.reverseComplement(subS));
			lengthLCSRCRight = countLowerCase(lcsRightRC);
			posRightRC = this.lastMatchPos;
		}
		String tDNAMatch = "";
		int posLeftTDNA = -1;
		String tDNARCMatch = "";
		String tDNAMatchName = "";
		String tDNAMatchNameRC = "";
		//speedup by only doing this if we have a long enough filer
		if(subS.length()>=tDNAHitminium){
			setLargestTDNAMatch(subS, false);
			tDNAMatch = longestCommonSubstring(this.tDNA,subS);
			tDNAMatchName = this.tDNAname;
			posLeftTDNA = this.lastMatchPos;
			//System.out.println("pos: "+this.lastMatchPos);
			//System.out.println(tDNAMatch);
			setLargestTDNAMatch(subS, true);
			tDNARCMatch = longestCommonSubstring(this.tDNArc,subS);
			tDNAMatchNameRC = this.tDNAname;
		}
		
		//System.out.println("posrc: "+this.lastMatchPos);
		//System.out.println("hier!");
		//System.out.println(tDNARCMatch);
		//System.out.println(tDNAMatch);
		int posLeftTDNARC = -1;
		int lengthtDNAMatch = 0;
		int lengthtDNARCMatch = 0;
		
		if(this.tDNA != null){
			posLeftTDNARC = tDNArc.length()-this.lastMatchPos-countLowerCase(tDNARCMatch);
			lengthtDNAMatch = countLowerCase(tDNAMatch);
			lengthtDNARCMatch = countLowerCase(tDNARCMatch);
		}
		while(Math.max(lengthLCSLeft,lengthLCSRCLeft) >= minimumMatch || Math.max(lengthLCSRight, lengthLCSRCRight) >= minimumMatch || Math.max(lengthtDNAMatch, lengthtDNARCMatch) > this.tDNAHitminium){
			//System.out.println(lcs + lengthLCS);
			boolean foundtDNA = false;
			//hack the TDNA hits in here!
			//only if they are larger than then minimum 
			//> what can be found left and right
			//the contains might not be necessary as the largest one will be picked already
			//contains is a bad idea, as we are not sure how is begin searched
			if(Math.max(lengthtDNAMatch, lengthtDNARCMatch) > this.tDNAHitminium  && Math.max(lengthtDNAMatch, lengthtDNARCMatch) > Math.max(Math.max(lengthLCSLeft,lengthLCSRCLeft), Math.max(lengthLCSRight, lengthLCSRCRight))){
				setFirstHitInTDNA(true);
				if(lengthtDNAMatch >= lengthtDNARCMatch){
					this.substituteString = "<"+tDNAMatchName+currentIndex+">";
					addMatchS(tDNAMatch);
					//System.out.println("t:"+posLeftTDNA);
					addPosS(posLeftTDNA, null);
					assLengthS(lengthtDNAMatch);
					subS = setSubS(tDNAMatch);
					foundtDNA = true;
				}
				else{
					this.substituteString = "<"+tDNAMatchNameRC+"rc"+currentIndex+">";
					addMatchS(tDNARCMatch);
					//System.out.println("rc:"+posLeftTDNARC);
					addPosS(posLeftTDNARC, null);
					assLengthS(lengthtDNARCMatch);
					subS = setSubS(tDNARCMatch);
					foundtDNA = true;
				}
			}
			//System.out.println(lengthLCSLeft);
			//System.out.println(lengthLCSRight);
			if(!foundtDNA && Math.max(lengthLCSLeft,lengthLCSRCLeft) == Math.max(lengthLCSRight, lengthLCSRCRight)){
				//hack one of the two!
				//left is closer or equal
				//changed to absolute
				//take into account distance to adjustedPos
				if(Math.min(Math.abs(posLeft+adjustedPositionLeft),Math.abs(posLeftRC+adjustedPositionLeft))<= Math.min(Math.abs(posRight+adjustedPositionRight), Math.abs(posRightRC+adjustedPositionRight))){
					lengthLCSRight = 0;
					lengthLCSRCRight = 0;
				}
				//right is closer
				else{
					lengthLCSLeft = 0;
					lengthLCSRCLeft = 0;
				}
			}
			//left
			if(!foundtDNA && Math.max(lengthLCSLeft,lengthLCSRCLeft) > Math.max(lengthLCSRight, lengthLCSRCRight)){
				setFirstHitInTDNA(false);
				//take the closest one
				if(lengthLCSLeft == lengthLCSRCLeft){
					//left
					if(Math.abs(posLeft+adjustedPositionLeft)< Math.abs(posLeftRC+adjustedPositionLeft)){
						lengthLCSRCLeft = 0;
					}
					//leftRC
					else{
						lengthLCSLeft = 0;
					}
				}
				if(lengthLCSLeft >= lengthLCSRCLeft){
					this.substituteString = "<"+currentIndex+"L>";
					addMatchS(lcsLeft);
					addPosS(posLeft, "LEFT");
					assLengthS(lengthLCSLeft);
					subS = setSubS(lcsLeft);
				}
				else{
					this.substituteString = "<"+currentIndex+"Lrc>";
					addMatchS(lcsLeftRC);
					addPosS(posLeftRC, "LEFT");
					assLengthS(lengthLCSRCLeft);
					subS = setSubS(Utils.reverseComplement(lcsLeftRC));
				}
			}
			//right
			else if(!foundtDNA){
				//take the closest one
				if(lengthLCSRight == lengthLCSRCRight){
					//left
					if(Math.abs(posRight+adjustedPositionRight)< Math.abs(posRightRC+adjustedPositionRight)){
						lengthLCSRCRight = 0;
					}
					//leftRC
					else{
						lengthLCSRight = 0;
					}
				}
				if(lengthLCSRight >= lengthLCSRCRight){
					this.substituteString = "<R"+currentIndex+">";
					addMatchS(lcsRight);
					addPosS(posRight, "RIGHT");
					assLengthS(lengthLCSRight);
					subS = setSubS(lcsRight);
				}
				else{
					this.substituteString = "<rcR"+currentIndex+">";
					addMatchS(lcsRightRC);
					addPosS(posRightRC, "RIGHT");
					assLengthS(lengthLCSRCRight);
					subS = setSubS(Utils.reverseComplement(lcsRightRC));
				}
			}
			//update variables
			if(this.searchNonRC){
				this.searchFromLeft = false;
				//System.out.println(left);
				//System.out.println("subs"+subS);
				lcsLeft = longestCommonSubstring(left, subS);
				//System.out.println("hier!lcsLeft:"+lcsLeft);
				//System.out.println("left:"+left);
				posLeft = this.lastMatchPos;
				this.searchFromLeft = true;
				lcsRight = longestCommonSubstring(right, subS);
				//System.out.println("lcsRight"+lcsRight);
				//System.out.println("right"+right);
				posRight = this.lastMatchPos;
				lengthLCSLeft = countLowerCase(lcsLeft);
				lengthLCSRight = countLowerCase(lcsRight);
			}
			//System.out.println("found:"+lcsLeft);
			//System.out.println("foundR:"+lcsRight);
			//TDNA match
			setLargestTDNAMatch(subS, false);
			tDNAMatch = longestCommonSubstring(this.tDNA,subS);
			tDNAMatchName = this.tDNAname;
			posLeftTDNA = this.lastMatchPos;
			setLargestTDNAMatch(subS, true);
			tDNARCMatch = longestCommonSubstring(this.tDNArc,subS);
			tDNAMatchNameRC = this.tDNAname;
			if(this.tDNA != null){
				posLeftTDNARC = tDNArc.length()-this.lastMatchPos-countLowerCase(tDNARCMatch);
				lengthtDNAMatch = countLowerCase(tDNAMatch);
				lengthtDNARCMatch = countLowerCase(tDNARCMatch);
			}
			//RC match
			if(this.searchRC){
				this.searchFromLeft = false;
				lcsLeftRC = longestCommonSubstring(left, Utils.reverseComplement(subS));
				lengthLCSRCLeft = countLowerCase(lcsLeftRC);
				posLeftRC = this.lastMatchPos;
				this.searchFromLeft = true;
				lcsRightRC = longestCommonSubstring(right, Utils.reverseComplement(subS));
				lengthLCSRCRight = countLowerCase(lcsRightRC);
				posRightRC = this.lastMatchPos;
			}
			currentIndex++;
		}
	}
	private void setLargestTDNAMatch(String subS, boolean rc) {
		if(this.tDNAVector != null && this.tDNAVector.size()>1){
			int maxSize = 0;
			Sequence current = null;
			for(Sequence s: tDNAVector){
				String lcs = null;
				if(!rc){
					lcs = longestCommonSubstring(s.seqString(), subS);
				}
				else{
					lcs = longestCommonSubstring(Utils.reverseComplement(s.seqString()), subS);
				}
				if(lcs.length()>maxSize){
					current = s;
					maxSize = lcs.length();
				}
			}
			//set it to the largest found
			if(current != null){
				this.tDNA = current.seqString();
				this.tDNArc = Utils.reverseComplement(current.seqString());
				this.tDNAname = current.getName();
			}
		}
	}
	private void setFirstHitInTDNA(boolean fromTDNA) {
		if(this.matchS.equals("")){
			this.firstHitIsTDNA = true;
			this.firstHitIsTDNADirectPos = fromTDNA;
		}
		
	}
	private void assLengthS(int lengthLCS) {
		if(this.lengthS.length()>0){
			lengthS += ";";
		}
		lengthS += lengthLCS;
	}
	private String setSubS(String lcs) {
		//only one occurence
		if(subS.indexOf(lcs)<0){
			System.out.println("oeps1");
			System.out.println(this.matchS);
			System.out.println(subS);
			System.out.println(subS2);
			System.out.println(lcs);
			//System.out.println(lcsWithoutUp);
			System.out.println(subS.indexOf(lcs));
			//Integer.parseInt("lo");
			//System.exit(0);
		}
		String lcsWithoutUp = lcs.replaceAll("[AGTC]", "");
		if(subS2.indexOf(lcsWithoutUp)<0){
			/*
			System.out.println("oeps2");
			System.out.println(this.matchS);
			System.out.println(subS);
			System.out.println(subS2);
			System.out.println(lcs);
			System.out.println(lcsWithoutUp);
			System.out.println(subS.indexOf(lcs));
			*/
			//Integer.parseInt("lo");
			//System.exit(0);
		}
		else{
			subS2 = subS2.replaceFirst(lcsWithoutUp, this.substituteString);
		}
			
		int spaces = subS.indexOf(lcs);
		int spacesAfter = subS.length()-spaces-lcs.length()+1;
		if(this.firstHit == ""){
			firstHit = this.substituteString;
		}
		String lastTarget = getLastEvent();
		this.addStringS(lastTarget);
		strings.add(createSpacedString(spaces,lcs,spacesAfter,lastTarget));
		//save some more information for Joost
		///////
		//spaces contains the correct position
		int start = spaces;
		int end = start+lcsWithoutUp.length();
		String[] parts = this.matchS.split(";");
		String pos = this.posS.split(";")[parts.length-1];
		isParts.add(start+"\t"+end+"\t"+substituteString.replaceAll("[0-9]", "")+"|"+pos);
		/////
		return subS.replaceFirst(lcs, lcs.toUpperCase());
	}
	private String getLastEvent() {
		String[] parts = this.matchS.split(";"); 
		//String match = parts[parts.length-1];
		String length = this.lengthS.split(";")[parts.length-1];
		String pos = this.posS.split(";")[parts.length-1];
		return substituteString+":"+pos+":"+length;
	}
	private String createSpacedString(int indexOf, String lcs, int spacesAfter, String lastTarget) {
		String ret = "";
		for(int i=0;i<indexOf;i++){
			ret+= " ";
		}
		ret += lcs;
		for(int i=0;i<spacesAfter;i++){
			ret+= " ";
		}
		ret += lastTarget;
		return ret;
	}
	private void addPosS(int pos, String side) {
		if(this.posS.length()>0){
			posS += ";";
		}
		if(side == null){
			posS += pos;
		}
		else if(side.equals("LEFT")){
			posS += adjustedPositionLeft+pos;
		}
		else if(side.equals("RIGHT")){
			posS += adjustedPositionRight+pos;
		}
		//System.out.println("posS is set to: "+posS);
	}
	private void addMatchS(String lcs) {
		if(this.matchS.length()>0){
			matchS += ";";
		}
		matchS += lcs;
	}
	private void addStringS(String s){
		if(this.stringS.length()>0){
			stringS += ";";
		}
		stringS += s;
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
		int Start = 0;
		int realStart = 0;
	    int Max = 0;
	    int longestLowerCase = 0;
	    for (int i = 0; i < S1.length(); i++)
	    {
	        for (int j = 0; j < S2.length(); j++)
	        {
	            int x = 0;
	            int nrLowercase = 0;
	            while (S1.charAt(i + x) == S2.charAt(j + x) || S1.charAt(i + x) == Character.toLowerCase(S2.charAt(j + x)))
	            {
	                //only lowercase characters count!
	                if(Character.isLowerCase(S2.charAt(j + x))){
	                	nrLowercase++;
	                }
	                x++;
	                if (((i + x) >= S1.length()) || ((j + x) >= S2.length())) break;
	            }
	            if(searchFromLeft && nrLowercase > longestLowerCase)
	            {
	            	Max = x;
	                Start = j;
	                realStart = i;
	                longestLowerCase = nrLowercase;
	                //System.out.println("Setting "+Max);
	                //System.out.println("Setting start "+Start);
	                //System.out.println("Setting start "+hasLowerCase);
	                //System.out.println("Setting start "+S2.substring(j, (j + x)));
	            }
	            //take the last one, but make sure we are not just throwing away the uppercase ones
	            if(!searchFromLeft && nrLowercase >= longestLowerCase && x > Max && nrLowercase > 0)
	            {
	            	Max = x;
	                Start = j;
	                realStart = i;
	                longestLowerCase = nrLowercase;
	                //System.out.println("!Setting "+Max);
	                //System.out.println("!nrLowercase "+nrLowercase);
	                //System.out.println("!longestLowerCase"+longestLowerCase);
		            //System.out.println("!Setting start "+S2.substring(j, (j + x)));
	            }
	         }
	    }
	    if(searchFromLeft){
	    	lastMatchPos = realStart;
	    }
	    else{
	    	lastMatchPos = S1.length()-realStart-Max;
	    }
	    return S2.substring(Start, (Start + Max));
	}
	public String toString(){
		String ret = "";
		String s = "\t";
		ret += id+s+left+s+insertion+s+right+s+insertion.length()+s+subS+s+subS2+s+matchS+s+posS+s+lengthS+s+getType()+s+getLargestMatch()+s+getLargestMatchString()+s+getFirstPos()+s+(getFirstPos()+getLargestMatch());
		ret += insertion;
		return ret;
	}
	public String toStringSolved(){
		String ret = "";
		String s = "\t";
		ret += id+s+left+s+insertion+s+right+s+insertion.length()+s+subS+s+matchS+s+posS+s+lengthS+s+getType()+s+getLargestMatch()+s+getFirstPos()+s+(getFirstPos()+getLargestMatch());
		ret += insertion;
		for(String st: strings){
			if(ret.length()>0){
				ret+="\n";
			}
			ret+=st;
		}
		return ret+"\n";
	}
	public String getMatchS(){
		return matchS;
	}
	public String getStringS(){
		return stringS;
	}
	public String getPosS(){
		return posS;
	}
	public String getSubS(){
		return subS;
	}
	public String getLengthS(){
		return this.lengthS;
	}
	public int getLargestMatch(){
		if(lengthS.length()>0){
			String[] parts = lengthS.split(";");
			return Integer.parseInt(parts[0]);
		}
		return -1;
	}
	public String getLargestMatchString(){
		if(matchS.length()>0){
			String[] parts = matchS.split(";");
			return parts[0];
		}
		return "-1";
	}
	public int getSecondLargestMatch(){
		if(lengthS.length()>0){
			String[] parts = lengthS.split(";");
			if(parts.length>1){
				return Integer.parseInt(parts[1]);
			}
		}
		return -1;
	}
	public int getFirstPos(){
		if(posS.length()>0){
			String[] parts = posS.split(";");
			return Integer.parseInt(parts[0]);
		}
		return Integer.MIN_VALUE;
	}
	public String getFirstHit(){
		return this.firstHit ;
	}
	public String getType(){
		if(this.getSubS().matches("[atgc]*")){
			return "NOT SOLVED";
		}
		//adjusted match
		else if(this.getSubS().replaceAll("[^atgc]", "").length() == 0){
			return "SOLVED";
		}
		//adjusted match
		else if(this.getSubS().replaceAll("[^atgc]", "").length() <= 4){
			return "ALMOST SOLVED";
		}
		else{
			return "PARTIALLY SOLVED";
		}
	}
	public void setAdjustedPositionLeft(int i) {
		this.adjustedPositionLeft = i;
	}
	public boolean hasMultipleMatches(){
		return this.matchS.contains(";");
	}
	public int totalMatch(){
		int total = 0;
		if(lengthS.length()>0){
			for(String l: lengthS.split(";")){
				//System.out.println("["+lengthS+"]");
				//System.out.println(l);
				total+= Integer.parseInt(l);
			}
		}
		return total;
	}
	public double getAverageMatch(){
		return totalMatch()/(double)lengthS.split(";").length;
	}
	public void setAdjustedPositionRight(int i) {
		this.adjustedPositionRight = i;
	}
	public void setTDNA(String tDNA){
		this.setTDNA(tDNA, "tDNA");
	}
	public int countLowerCase(String s){
		int nr = 0;
		for(char c: s.toCharArray()){
			if(Character.isLowerCase(c)){
				nr++;
			}
		}
		return nr;
	}
	public void matchBlastHits(ArrayList<Blast> temp, String chr, int start, boolean revCom) {
		if(temp == null){
			return;
		}
		//System.out.println("I have "+temp.size()+ " genomic blast matches");
		String lastQuery = "";
		for(Blast b: temp){
			//at least 10E-4
			//System.out.println(b.getEValue()+" "+b.getpIdentity() +" "+(b.getqEnd()-b.getqStart())+" origL:"+this.insertion.length());
			//System.out.println(this.insertion);
			if(b.getEValue()>0.001){
				continue;
			}
			/*
			if(b.getChr().equals(chr)){
				System.out.println("SAME chromosome "+b.getEValue());
			}
			else{
				System.out.println("DIFF chromosome "+b.getEValue());
			}
			*/
			//System.out.println(b);
			String query = b.getQuery().toLowerCase();
			//only do it when we don't have it in the flank
			if(left.contains(query) || right.contains(query)){
				continue;
			}
			//only do it when we don't have it in the flank (also reverse complement if needed)
			if(this.searchRC && (Utils.reverseComplement(left).contains(query) || Utils.reverseComplement(right).contains(query))){
				continue;
			}
			if(currentIndex>0 && !subS.contains(b.getQuery())){
				System.out.println("Skipping blast hit because it is not the first... Hopefully the order of blast hits is correct!");
				continue;
			}
			String rc = "";
			int dis = Integer.MIN_VALUE;
			if(b.getChr().equals(chr)){
				//System.out.println("SAME chromosome");
				rc = "sameChr ";
				//distance
				dis = Math.min(b.getsStart()-start,b.getsEnd()-start);
				if(b.isRevComMatch() != revCom){
					rc += "RC ";
				}
				if(revCom){
					dis = -1*dis;
				}
			}
			else{
				//System.out.println("DIFF chromosome");
				rc = "diffChr ";
			}
			String remark = "";
			if(!b.querySubjectIdentical()){
				//System.out.println("Making identical");
				b.makeSubjectQueryIdentical(true, true, -1);
				remark = b.getRemarks();
				//System.out.println(b.getQuery().toLowerCase());
			}
			//additional check if we already removed it, this means that if multiple identical blast
			//hits exist they are mapped to the first found chromosome... 
			//I am expecting this to have little influence on the outcome
			if(lastQuery.contains(b.getQuery())){
				continue;
			}
			this.substituteString = "<genome "+currentIndex+" "+rc+b.getChr()+":"+b.getsStart()+"-"+b.getsEnd()+">"+remark;
			addMatchS(b.getQuery());
			//System.out.println("t:"+posLeftTDNA);
			addPosS(dis, null);
			assLengthS(b.getLength());
			subS = setSubS(b.getQuery().toLowerCase());
			currentIndex++;
			lastQuery = b.getQuery();
		}
		
	}
	public void setProbabilityAT(double d) {
		this.AT = d;
	}
	public void setProbabilityGC(double d) {
		this.GC = d;
	}
	public double getProbability(int genomeSize){
		int totalSpace = getSearchSpace(genomeSize);
		double prob = this.getProbabilityLongestInsertion();
		//remove the size of the longest match;
		String longest = this.matchS.split(";")[0];
		int trials = totalSpace - longest.length();
		//remove a second time
		if(this.searchRC){
			trials -= longest.length();
		}
		//that should not happen, but we'll put a safety anyway
		if(trials<0){
			return -1;
		}
		BinomialDistribution bd = new BinomialDistribution(trials, prob);
		double probRet = (insertion.length()-longest.length()+1)*(1.0-bd.probability(0));
		//disallow >1
		return probRet>1? 1:probRet;
	}
	public int getSearchSpace(int genomeSize){
		int totalSpace = left.length()+right.length(); 
		if(this.searchRC){
			totalSpace *= 2;
		}
		if(this.insertion.length()>=20){
			totalSpace+= genomeSize*2;
		}
		else if(this.insertion.length()>=15){
			totalSpace+= this.tDNA.length()+this.tDNArc.length();
		}
		return totalSpace;
	}
	private double getProbabilityLongestInsertion(){
		double prob = 1.0;
		String longest = this.matchS.split(";")[0];
		for(char c: longest.toCharArray()){
			if(c == 'a' || c == 't' || c == 'A' || c== 'T'){
				prob *= this.AT;
			}
			else if(c == 'g' || c == 'c' || c == 'C' || c == 'G'){
				prob *= this.GC;
			}
		}
		return prob;
	}
	public String getSubS2() {
		return subS2;
	}
	public void search(boolean searchNonRC, boolean searchRC) {
		this.searchRC = searchRC;
		this.searchNonRC = searchNonRC;
	}
	public String getLeft() {
		return left;
	}
	public String getRight() {
		return right;
	}
	public String getLeftGCContent() {
		String s = "\t";
		String ret = "left";
		if(left != null){
			double gc = (left.length()-left.replaceAll("[GCgc]", "").length())/(double)left.length();
			ret+=s+gc;
		}
		return ret;
	}
	public String getRightGCContent() {
		String s = "\t";
		String ret = "right";
		if(right != null){
			double gc = (right.length()-right.replaceAll("[GCgc]", "").length())/(double)right.length();
			ret+=s+gc;
		}
		return ret;
	}
	public boolean getFirstHitInTDNA() {
		return this.firstHitIsTDNA;
	}
	public boolean getFirstHitIsTDNADirectPos(){
		return firstHitIsTDNADirectPos;
	}
	public void setTDNA(String seqString, String name) {
		this.tDNA = seqString.toLowerCase();
		this.tDNArc = Utils.reverseComplement(seqString).toLowerCase();
		this.tDNAname = name;
	}
	public void setTDNA(Vector<Sequence> additionalSearchSequence) {
		this.tDNAVector = additionalSearchSequence;
	}
	public String[] printISParts(HashMap<String, String> colorMap) {
		String[] ret = new String[isParts.size()+1];
		int index = 0;
		ret[index] = 0+"\t"+insertion.length()+"\tgrey";
		index++;
		for(String block: isParts){
			ret[index] = block;
			index++;
		}
		return ret;
	}
}


