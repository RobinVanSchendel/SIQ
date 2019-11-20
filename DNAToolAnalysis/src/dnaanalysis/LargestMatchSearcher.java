package utils;

import java.util.ArrayList;

public class LargestMatchSearcher {
	private String lcsQuery, lcsSubject;
	private int startQ, endQ, startS, endS;
	private boolean rc = false;
	private int preferredPosition;
	private boolean left;
	private String subject;
	private String query;
	public LargestMatchSearcher(String lcsQ, String lcsS) {
		this.lcsQuery = lcsQ;
		this.lcsSubject = lcsS;
	}
	public void setQPos(int start, int end) {
		this.startQ = start;
		this.endQ = end;
	}
	public void setSPos(int start, int end) {
		this.startS = start;
		this.endS = end;
	}
	public static LargestMatchSearcher getLCS(Match match) {
		LargestMatchSearcher lcs = null;
		int allowedMismatch = match.getNrMismatchesAllowed();
		int preferredPosition = match.getPreferredPosition();
		if(match.searchBothDirections()) {
			//System.out.println("F search");
			LargestMatchSearcher lcsF = longestCommonSubstringAllowMismatch(match.getQuery(), match.getSubject(),allowedMismatch, preferredPosition);
			//System.out.println(lcsF);
			//System.out.println("RC search");
			LargestMatchSearcher lcsRC = longestCommonSubstringAllowMismatch(Utils.reverseComplement(match.getQuery()), match.getSubject(),allowedMismatch, preferredPosition);
			lcsRC.setRC(match.getQuery());
			lcsF.setPreferredPosition(preferredPosition);
			lcsRC.setPreferredPosition(preferredPosition);
			lcsF.setLeft(match.isLeft());
			lcsRC.setLeft(match.isLeft());
			//System.out.println(lcsRC);
			
			ArrayList<LargestMatchSearcher> lcss = new ArrayList<LargestMatchSearcher>();
			lcss.add(lcsF);
			lcss.add(lcsRC);
			lcs = getLargestLCS(lcss);
		}
		
		return lcs;
	}
	private void setLeft(boolean left) {
		this.left = left;
	}
	private void setPreferredPosition(int preferredPosition) {
		this.preferredPosition = preferredPosition;
	}
	/**method which adjusts the positions of the rc query correct
	 * rc lcsQuery
	 * rc lcsSubject
	 * adjust startQ and endQ
	 * 
	 * @param query
	 */
	private void setRC(String query) {
		this.rc  = true;
		this.lcsQuery = Utils.reverseComplement(lcsQuery);
		this.lcsSubject = Utils.reverseComplement(lcsSubject);
		startQ = query.indexOf(lcsQuery);
		endQ = startQ+lcsQuery.length();
		
	}
	private static LargestMatchSearcher getLargestLCS(ArrayList<LargestMatchSearcher> lcss) {
		int maxMatch = 0;
		int maxMismatch = Integer.MAX_VALUE;
		LargestMatchSearcher largest = null;
		boolean multipleLargest = false;
		for(LargestMatchSearcher l: lcss) {
			int matches = l.getNrMatches();
			int mismatches = l.getNrMisMatches();
			if(matches>maxMatch || (matches==maxMatch && mismatches<maxMismatch)) {
				maxMatch = matches;
				largest = l;
				multipleLargest = false;
				maxMismatch = mismatches;
			}
			else if(matches==maxMatch && mismatches==maxMismatch) {
				multipleLargest = true;
			}
		}
		//choose the closest one to the preferred position
		//in case of a tie in distance the forward ones are automatically chosen
		//as they are first in the list!
		if(multipleLargest) {
			ArrayList<LargestMatchSearcher> largests = new ArrayList<LargestMatchSearcher>();
			//System.out.println("LARGESTS");
			for(LargestMatchSearcher l: lcss) {
				if(l.getNrMatches()==largest.getNrMatches() && l.getNrMisMatches()==largest.getNrMisMatches()) {
					largests.add(l);
					//System.out.println(l);
				}
			}
			//System.out.println("LARGESTS END");
			int lowestDistance = Integer.MAX_VALUE;
			for(LargestMatchSearcher l: largests) {
				int tempD = Math.min(l.getStartS()-l.getPreferredPosition(), l.getEndS()-l.getPreferredPosition());
				if(tempD<lowestDistance) {
					largest = l;
					lowestDistance = tempD;
					//System.out.println("< tempD "+tempD+" lowest:"+lowestDistance);
				}
				else {
					//System.out.println("tempD "+tempD+" lowest:"+lowestDistance);
				}
			}
			//System.out.println("multipleLargest found "+largests.size());
		}
		else {
			//System.out.println("single largest");
		}
		return largest;
	}
	private int getPreferredPosition() {
		return preferredPosition;
	}
	private int getStartS() {
		return startS;
	}
	private int getEndS() {
		return endS;
	}
	private int getNrMatches() {
		int match = 0;
		for(int i=0;i<lcsQuery.length();i++) {
			if(lcsQuery.charAt(i)==lcsSubject.charAt(i)) {
				match++;
			}
		}
		return match;
	}
	/*
	public static LCS longestCommonSubstring(String query, String subject, boolean searchFromLeft)
	{
	    if(query == null || subject == null){
	    	return null;
	    }
		int Start = 0;
		int realStart = 0;
	    int Max = 0;
	    int longestLowerCase = 0;
	    for (int i = 0; i < query.length(); i++)
	    {
	        for (int j = 0; j < subject.length(); j++)
	        {
	            int x = 0;
	            int nrLowercase = 0;
	            while (query.charAt(i + x) == subject.charAt(j + x) || query.charAt(i + x) == Character.toLowerCase(subject.charAt(j + x)))
	            {
	                //only lowercase characters count!
	                if(Character.isLowerCase(subject.charAt(j + x))){
	                	nrLowercase++;
	                }
	                x++;
	                if (((i + x) >= query.length()) || ((j + x) >= subject.length())) break;
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
	    int lastMatchPos = -1;
	    if(searchFromLeft){
	    	lastMatchPos = realStart;
	    }
	    else{
	    	lastMatchPos = query.length()-realStart-Max;
	    }
	    String lcs = subject.substring(Start, (Start + Max));
	    LCS l = new LCS(lcs, lcs);
	    l.setQPos(lastMatchPos, lastMatchPos+lcs.length());
	    l.setSPos(Start, (Start + Max));
	    return l;
	}
	*/
	private static LargestMatchSearcher longestCommonSubstringAllowMismatch(String query, String subject, int nrMismatches, int preferredPosition)
	{
	    if(subject == null || query == null){
	    	return null;
	    }
		int StartS1 = 0;
		int StartS2 = 0;
	    int Max = 0;
	    int MaxMismatches = 0;
	    int MaxMatches = 0;
	    for (int i = 0; i < subject.length(); i++)
	    {
	        for (int j = 0; j < query.length(); j++)
	        {
	            int x = 0;
	            int mismatches = 0;
	            int matches = 0;
	            while (subject.charAt(i + x) == query.charAt(j + x) || (mismatches<nrMismatches && subject.charAt(i + x) != '|' && query.charAt(j + x) != '|' ))
	            {
	                if(subject.charAt(i + x) != query.charAt(j + x)){
	                	mismatches++;
	                }
	                else{
	                	matches++;
	                }
	            	x++;
	                if (((i + x) >= subject.length()) || ((j + x) >= query.length())) break;
	            }
	            if (matches > MaxMatches || (matches==MaxMatches && mismatches<MaxMismatches))
	            {
	                Max = x;
	                MaxMatches = matches;
	                StartS1 = i;
	                StartS2 = j;
	                MaxMismatches = mismatches;
	                //System.out.println("Setting pos to "+StartS1+" matches "+MaxMatches);
	            }
	            //same event found, take closest to preferredPosition
	            else if(matches == MaxMatches && mismatches==MaxMismatches) {
	            	int disToPreferred = Math.abs(preferredPosition-StartS1);
	            	int disToCurrent = Math.abs(preferredPosition-i);
	            	if(disToCurrent<disToPreferred) {
	            		//System.out.println("Taking the one closer to preferredPosition "+disToCurrent);
	            		Max = x;
		                MaxMatches = matches;
		                StartS1 = i;
		                StartS2 = j;
		                MaxMismatches = mismatches;
	            	}
	            	else {
	            		//System.out.println("Same found at pos "+i+", but keeping old position "+MaxMatches+":"+StartS1);
	            	}
	            }
	         }
	    }
	    String S1temp = subject.substring(StartS1, (StartS1 + Max));
	    String S2temp = query.substring(StartS2, (StartS2 + Max));
	    LargestMatchSearcher l = new LargestMatchSearcher(S2temp,S1temp);
	    l.setSPos(StartS1, (StartS1 + Max));
	    l.setQPos(StartS2, (StartS2 + Max));
	    l.setQuery(query);
	    l.setSubject(subject);
	    return l;
	}
	
	private void setSubject(String subject) {
		this.subject = subject;
	}
	private void setQuery(String query) {
		this.query = query;
	}
	public String toString() {
		StringBuffer sb = new StringBuffer();
		String s = "\t";
		sb.append(query).append(s);
		sb.append(subject).append(s);
		sb.append(lcsQuery).append(s);
		sb.append(lcsSubject).append(s);
		sb.append(startQ).append(s);
		sb.append(endQ).append(s);
		sb.append(startS).append(s);
		sb.append(endS).append(s);
		sb.append(getNrMatches()).append(s);
		sb.append(getNrMisMatches()).append(s);
		sb.append(getMisMatchString()).append(s);
		sb.append(getOrientation()).append(s);
		sb.append(getAdjustedPositionStart()).append(s);
		sb.append(getAdjustedPositionEnd()).append(s);
		
		return sb.toString();
	}
	public static String getHeader() {
		StringBuffer sb = new StringBuffer();
		String s = "\t";
		sb.append("Query").append(s);
		sb.append("Subject").append(s);
		sb.append("lcsQuery").append(s);
		sb.append("lcsSubject").append(s);
		sb.append("startQ").append(s);
		sb.append("endQ").append(s);
		sb.append("startS").append(s);
		sb.append("endS").append(s);
		sb.append("getNrMatches()").append(s);
		sb.append("getNrMisMatches()").append(s);
		sb.append("getMisMatchString()").append(s);
		sb.append("getOrientation()").append(s);
		sb.append("getAdjustedPositionStart()").append(s);
		sb.append("getAdjustedPositionEnd()").append(s);
		return sb.toString();
	}
	private String getAdjustedPosition() {
		return this.getAdjustedPositionStart()+"-"+this.getAdjustedPositionEnd();
	}
	public int getAdjustedPositionEnd() {
		if(!left) {
			return -1*(endS-this.preferredPosition);
		}
		else {
			return endS-this.preferredPosition;
		}
	}
	public int getAdjustedPositionStart() {
		if(!left) {
			return -1*(startS-this.preferredPosition);
		}
		else {
			return startS-this.preferredPosition;
		}
	}
	private String getOrientation() {
		if(this.rc) {
			return "reverse";
		}
		else {
			return "forward";
		}
	}
	private int getNrMisMatches() {
		int mismatches = 0;
		for(int i=0;i<lcsQuery.length();i++) {
			if(lcsQuery.charAt(i)!=lcsSubject.charAt(i)) {
				mismatches++;
			}
		}
		return mismatches;
	}
	private String getMisMatchString() {
		String ret = "";
		for(int i=0;i<lcsQuery.length();i++) {
			if(lcsQuery.charAt(i)==lcsSubject.charAt(i)) {
				ret+="|";
			}
			else {
				ret+="X";
			}
		}
		return ret;
	}
}
