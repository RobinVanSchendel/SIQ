package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.SortedMap;
import java.util.TreeMap;

public class KMERLocation {
	private String ref;
	private static final int KMERLENGTH = 15;
	HashMap<String, KMER> hm = new HashMap<String, KMER>();
	private ArrayList<LCS> lcss = new ArrayList<LCS>();
	private String query;
	public KMERLocation (String s) {
		this.ref = s;
		initKMER();
	}
	private void initKMER() {
		for(int i = 0;i<ref.length();i++) {
			if(i+KMERLENGTH<ref.length()) {
				String s = ref.substring(i, i+KMERLENGTH);
				if(hm.containsKey(s)) {
					hm.get(s).addLocation(i);
				}
				else {
					KMER kml = new KMER(s,i);
					hm.put(s,kml);
				}
			}
		}
	}
	private boolean hasQuery(String q) {
		if(query!= null && query.equals(q)) {
			return true;
		}
		return false;
	}
	public Left getMatchLeft(String seq, int leftPos) {
		if(!hasQuery(seq)) {
			//System.out.println("replaced");
			fillLCS(seq);
		}
		//System.out.println(seq);
		if(lcss.size()==0) {
			return null;
		}
		for(int i = 0;i<lcss.size();i++) {
			//System.out.println(i+" "+lcss.get(i));
		}
		
		if(lcss.size()==1) {
			//System.out.println(lcss.size());
			LCS one = lcss.get(0);
			int start = one.getSubjectStart();
			if(start > leftPos) {
				return null;
			}
			int maxPos = Math.min(one.getSubjectEnd(), leftPos);
			//System.out.println("leftPostMax:"+leftPos);
			//System.out.println("leftPostCurrent:"+one.getSubjectEnd());
			//it is already fine
			if(maxPos == one.getSubjectEnd()) {
				//System.out.println("shortcut");
				return one;
			}
			String leftS = ref.substring(one.getSubjectStart(), maxPos);
			int queryEndPos = seq.indexOf(leftS)+leftS.length();
			Left l = new Left(leftS,one.getSubjectStart(), maxPos, one.getQueryStart(),queryEndPos);
			return l;
		}
		//check the longest
		//get the longest
		int longest = -1;
		LCS max = null;
		for(LCS lcs: lcss) {
			if(lcs.length()>longest && lcs.getSubjectStart()<leftPos) {
				longest = lcs.length();
				max = lcs;
			}
		}
		if(max == null) {
			return null;
		}
		
		//possible take the next one
		LCS second = null;
		for(LCS lcs: lcss) {
			int absDist = Math.abs(lcs.getSubjectStart()-max.getSubjectEnd());
			int absDistQuery = Math.abs(lcs.getQueryStart()-max.getQueryEnd());
			if(lcs != max && lcs.getSubjectStart()<leftPos-30 && absDist<=1 && absDistQuery<=1 && lcs.getSubjectStart()>max.getSubjectStart()) {
				second = lcs;
				//System.out.println(absDist);
				//System.out.println(absDistQuery);
			}
		}
		//System.out.println("max:"+max);
		//System.out.println("second:"+second);
		boolean jumped = false;
		if(second!= null) {
			max = second;
			jumped = true;
		}
		//System.out.println("max:"+max);
		//System.out.println(leftPos);
		int maxPos = Math.min(max.getSubjectEnd(), leftPos);
		String leftS = ref.substring(max.getSubjectStart(), maxPos);
		int queryEndPos = seq.indexOf(leftS)+leftS.length();
		Left l = new Left(leftS,max.getSubjectStart(), maxPos, max.getQueryStart(),queryEndPos, jumped);
		return l;
		/*
		if(lcss.get(0).getSubjectStart()<leftPos){
			//System.out.println(lcss.size());
			LCS one = lcss.get(0);
			LCS two = lcss.get(1);
			LCS takeThis = one;
			boolean jumped = false;
			//do we jump?
			if(two.getSubjectStart()< leftPos 
					&& two.getSubjectStart() == one.getSubjectEnd()+1 
					&& two.getQueryStart() == one.getQueryEnd()+1) {
				//minimum of 30 to jump
				int maxPos = Math.min(two.getSubjectEnd(), leftPos);
				if(maxPos-two.getSubjectStart()>=30) {
					takeThis = two;
					jumped = true;
				}
			}
			int maxPos = Math.min(takeThis.getSubjectEnd(), leftPos);
			//System.out.println(takeThis.getSubjectStart());
			//System.out.println(maxPos);
			//System.out.println(one);
			//System.out.println(two);
			//System.out.println(leftPos);
			String leftS = ref.substring(takeThis.getSubjectStart(), maxPos);
			int queryEndPos = seq.indexOf(leftS)+leftS.length();
			Left l = new Left(leftS,takeThis.getSubjectStart(), maxPos, takeThis.getQueryStart(),queryEndPos, jumped);
			return l;
		}
		return null;	
		*/
	}
		/*
		for(LCS l: lcss) {
			//either take the first or the second
			
			System.out.println(l);
			
		}
		System.out.println("hier!");
		System.exit(0);
		//else {
			int refIndex = -1;
			int seqIndex = -1;
			for(int i = 0;i<seq.length();i++) {
				if(i+KMERLENGTH<seq.length()) {
					String s = seq.substring(i, i+KMERLENGTH);
					if(hm.containsKey(s)) {
						if(hm.get(s).getLocation().size()==1) {
							refIndex = hm.get(s).getLocation().get(0);
							seqIndex = i;
							//System.out.println(s);
							//System.out.println(hm.get(s));
							break;
						}
					}
				}
			}
			if(refIndex>0) {
				int queryStart = seqIndex;
				int subjectStart = refIndex;
				//System.out.println(ref.charAt(subjectStart));
				//System.out.println(seq.charAt(queryStart));
				while(ref.charAt(refIndex) == seq.charAt(seqIndex) && refIndex<leftPos) {
					refIndex++;
					seqIndex++;
					//System.out.println("+1");
				}
				int queryEnd = seqIndex;
				int subjectEnd = refIndex;
				//System.out.println("Query ["+queryStart+":"+queryEnd+"] " +seq.substring(queryStart, queryEnd));
				//System.out.println("Subject ["+subjectStart+":"+subjectEnd+"] " +ref.substring(subjectStart, subjectEnd));
				//return seq.substring(queryStart, queryEnd);
				//jumping is not allowed at the moment
				return new Left(seq.substring(queryStart, queryEnd), subjectStart, subjectEnd, queryStart, queryEnd, false);
			}
			return null;
		//}
		 * */
	private void fillLCS(String seq) {
		lcss.clear();
		this.query = seq;
		SortedMap<Integer, ArrayList<Integer>> locsHash = new TreeMap<Integer, ArrayList<Integer>>();
		for(int i = 0;i<seq.length();i++) {
			if(i+KMERLENGTH<=seq.length()) {
				String s = seq.substring(i, i+KMERLENGTH);
				//ignore these
				if(s.contains("X")) {
					continue;
				}
				if(hm.containsKey(s)) {
					ArrayList<Integer> temp = hm.get(s).getLocation();
					for(int loc: temp) {
						//System.out.println(i+" "+loc+" "+s);
						//if(!locs.contains(loc)) {
							//locs.add(new Location(i,loc));
							if(locsHash.containsKey(i)) {
								locsHash.get(i).add(loc);
							}
							else {
								ArrayList<Integer> ints = new ArrayList<Integer>();
								ints.add(loc);
								locsHash.put(i, ints);
							}
						//}
					}
				}
			}
		}
		if(locsHash.isEmpty()) {
			//System.out.println("no match");
			return;
		}
		//Location realStart = locs.get(0);
		//Location start = realStart;
		//int startLocation = start.getSubject();
		lcss = getLocations(locsHash);
		/*
		for(int i=1;i<locs.size();i++) {
			//System.out.println("loc: "+locs.get(i));
			if(locs.get(i).getQuery() == start.getQuery()+1 && locs.get(i).getSubject() == start.getSubject()+1) {
				startLocation++;
				start = locs.get(i);
			}
			else {
				int end = startLocation+KMERLENGTH;
				
				String lcsString = ref.substring(realStart.getSubject(),end);
				int startQuery = seq.indexOf(lcsString);
				if(startQuery == -1) {
					System.err.println("deep shit");
				}
				int endQuery = startQuery+lcsString.length();
				LCS lcs = new LCS(lcsString,realStart.getSubject(),end, startQuery, endQuery);
				//System.out.println(lcs);
				lcss.add(lcs);
				//reset
				realStart = locs.get(i);
				start = realStart;
				startLocation = start.getSubject();
			}
		}
		//also add the final one
		//System.out.println("hierzo"+lcss.size());
		//System.out.println(lcss.get(0));
		//System.out.println(start.getSubject());
		//System.out.println(realStart.getSubject());
		if(start.getSubject()>=realStart.getSubject() && realStart.getSubject() >0) {
			int end = startLocation+KMERLENGTH;
			String lcsString = ref.substring(realStart.getSubject(),end);
			int startQuery = seq.indexOf(lcsString);
			if(startQuery == -1) {
				System.err.println("deep shit");
			}
			int endQuery = startQuery+lcsString.length();
			LCS lcs = new LCS(lcsString,realStart.getSubject(),end, startQuery, endQuery);
			lcss.add(lcs);
			//set the query
			this.query = seq;
		}
		/*
		if(lcss.size()==1 && ref.indexOf(query)>0) {
			//System.out.println(query);
			//System.out.println(ref);
		}
		*/
	}
	private ArrayList<LCS> getLocations(SortedMap<Integer, ArrayList<Integer>> locsHash) {
		ArrayList<LCS> temp = new ArrayList<LCS>();
		while(!locsHash.isEmpty()) {
			int key = locsHash.firstKey();
			temp.add(findRoute(locsHash, key));
		}
		return temp;
	}
	private LCS findRoute(SortedMap<Integer, ArrayList<Integer>> locsHash, int key) {
		boolean loop = true;
		int start = locsHash.get(key).get(0);
		locsHash.get(key).remove(0);
		if(locsHash.get(key).isEmpty()){
			locsHash.remove(key);
		}
		int end = start;
		int origKey = key;
		while(loop) {
			//System.out.println("["+origKey+":"+key+"]:"+start+":"+end);
			if(locsHash.containsKey(key+1) && locsHash.get(key+1).contains(end+1)) {
				Integer i = end+1;
				locsHash.get(key+1).remove(i);
				if(locsHash.get(key+1).isEmpty()){
					locsHash.remove(key+1);
				}
				loop = true;
				key++;
				end++;
			}
			else {
				loop = false;
			}
		}
		//add KMERLENGTH to values
		key += KMERLENGTH;
		end += KMERLENGTH;
		String substring = query.substring(origKey, key);
		LCS lcs = new LCS(substring,start, end,origKey, key);
		//System.out.println("route:"+lcs);
		return lcs;
		//System.out.println("final["+origKey+":"+key+"]:"+start+":"+end);
	}
	public String getMatchRight(String seq, int startPos, int minSize) {
		//System.out.println("seq"+seq);
		//System.out.println("getMatchRight");
		if(!hasQuery(seq)) {
			//System.out.println("filling "+lcss.size());
			fillLCS(seq);
			//System.out.println("filled "+lcss.size());
		}
		if(lcss.size()==0) {
			return null;
		}
		//System.out.println("lcs");
		//for(LCS l: lcss) {
			//System.out.println(l);
		//}
		int longest = -1;
		LCS max = null;
		for(LCS lcs: lcss) {
			//System.out.println("lcs:"+lcs);
			if(lcs.getSubjectEnd()>startPos && lcs.getString().length()>=minSize) {
				//System.out.println("q:"+seq);
				//System.out.println(lcs);
				//System.out.println(seq.indexOf(lcs.getString()));
				//System.out.println(lcss.size());
				//System.out.println(startPos);
				if(lcs.getString().length()>longest) {
					longest = lcs.getString().length();
					max = lcs;
					//System.out.println("max now: "+max);
					//System.out.println(longest);
				}
				//return lcs.getString();
			}
		}
		if(max!= null) {
			int startRefPos = Math.max(startPos, max.getSubjectStart());
			//System.out.println(startPos);
			//System.out.println(max.getSubjectStart());
			//System.out.println(startRefPos);
			//System.out.println(max.getSubjectEnd());
			if(startRefPos == max.getSubjectEnd()) {
				return null;
			}
			return ref.substring(startRefPos, max.getSubjectEnd());
		}
		return null;
		/*
		int refIndex = -1;
		int seqIndex = -1;
		for(int i = 0;i<seq.length();i++) {
			if(i+KMERLENGTH<seq.length()) {
				String s = seq.substring(i, i+KMERLENGTH);
				if(hm.containsKey(s)) {
					if(hm.get(s).getLocation().size()==1) {
						refIndex = hm.get(s).getLocation().get(0);
						//only break from this position
						if(refIndex >= startPos) {
							seqIndex = i;
							//System.out.println(s);
							//System.out.println(hm.get(s));
							break;
						}
					}
				}
			}
		}
		//both need to be set
		if(refIndex>0 && seqIndex>0) {
			int queryStart = seqIndex;
			int subjectStart = refIndex;
			//System.out.println(ref.charAt(subjectStart));
			//System.out.println(seq.charAt(queryStart));
			while(refIndex<ref.length() && seqIndex<seq.length() && ref.charAt(refIndex) == seq.charAt(seqIndex)) {
				refIndex++;
				seqIndex++;
				//System.out.println("+1");
			}
			int queryEnd = seqIndex;
			int subjectEnd = refIndex;
			//System.out.println("Query ["+queryStart+":"+queryEnd+"] " +seq.substring(queryStart, queryEnd));
			//System.out.println("Subject ["+subjectStart+":"+subjectEnd+"] " +ref.substring(subjectStart, subjectEnd));
			//return seq.substring(queryStart, queryEnd);
			return seq.substring(queryStart, queryEnd);
		}
		return null;
		*/
	}
	public String getLCS(String seq) {
		//already have this one
		if(!hasQuery(seq)) {
			this.fillLCS(seq);
		}
		return getLCSInternal();
	}
	private String getLCSInternal() {
		int maxLength = -1;
		String maxString = "";
		for(LCS lcs: lcss) {
			//System.out.println(m);
			if(lcs.getString().length()>maxLength) {
				maxLength = lcs.getString().length();
				maxString = lcs.getString();
			}
		}
		return maxString;
	}
}
