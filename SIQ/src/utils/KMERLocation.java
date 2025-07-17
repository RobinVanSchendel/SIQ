package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.SortedMap;
import java.util.TreeMap;

import org.jcvi.jillion.core.Ranges;

public class KMERLocation {
	private String ref;
	private static final int KMERLENGTH = 15;
	HashMap<String, KMER> hm = new HashMap<String, KMER>();
	private ArrayList<LCS> lcss = new ArrayList<LCS>();
	private String query;
	private Subject subjectObject = null;
	private static final int MINIMUMSECONDSIZE = 30;
	
	public KMERLocation(Subject so) {
		this.subjectObject  = so;
		this.ref = so.getRefString();
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
		//not sure if the query.length check is speeding up things
		if(query!= null && query.length() == q.length() && query.contentEquals(q)) {
			return true;
		}
		return false;
	}
	public Left getMatchLongestLeft(String seq, boolean allowJump) {
		if(!hasQuery(seq)) {
			//System.out.println("replaced");
			fillLCS(seq);
		}
		//System.out.println(seq);
		if(lcss.size()==0) {
			return null;
		}
		if(lcss.size()==1) {
			return lcss.get(0);
		}
		//get the longest
		LCS max = this.getLCS(seq);
		if(allowJump) {
			//get the second longest left to max
			int longesttwo = -1;
			LCS maxTwo = null;
			for(LCS lcs: lcss) {
				if(lcs.length()>= MINIMUMSECONDSIZE && lcs.length()>longesttwo && !lcs.toString().equals(max.toString())) {
					longesttwo = lcs.length();
					maxTwo = lcs;
				}
			}
			if(maxTwo != null) {
				if(maxTwo.getSubjectStart()<max.getSubjectStart()) {
					return maxTwo;
				}
			}
		}
		return max;
	
	}
	public Left getMatchLeftCheckLongestOnly(LCS l, int rightPos, boolean allowJump, int leftPos, int maxStartPos) {
		int start = l.getSubjectStart();
		if(start > rightPos) {
			return null;
		}
		int maxPos = Math.min(l.getSubjectEnd(), rightPos);
		//it is already fine
		if(maxPos == l.getSubjectEnd()) {
			//System.out.println("shortcut");
			return l;
		}
		String leftS = ref.substring(l.getSubjectStart(), maxPos);
		int queryEndPos = l.getQueryStart()+leftS.length();
		Left left = new Left(leftS,l.getSubjectStart(), maxPos, l.getQueryStart(),queryEndPos);
		return left;
		
	}
	public Left getMatchLeft(String seq, int rightPos, boolean allowJump, int leftPos, int maxStartPos, int nrRanges) {
		if(!hasQuery(seq)) {
			//System.out.println("replacing");
			//long start = System.nanoTime();
			fillLCS(seq);
			//long stop = System.nanoTime();
			//long duration = stop-start;
			//System.out.println("fillLCS "+duration);
			//System.out.println("replaced");
		}
		/*
		else {
			System.out.println("not replacing");
		}
		*/
		//System.out.println(seq);
		if(lcss.size()==0) {
			return null;
		}
		//System.out.println(lcss.size());
		/*
		if(lcss.size()>=24) {
			System.out.println(seq);
			for(LCS lcs: lcss) {
				System.out.println(lcs);
				
			}
			System.exit(0);
		}
		*/
		if(lcss.size()==1) {
			LCS one = lcss.get(0);
			int start = one.getSubjectStart();
			if(start > rightPos) {
				return null;
			}
			int maxPos = Math.min(one.getSubjectEnd(), rightPos);
			//System.out.println("leftPostMax:"+leftPos);
			//System.out.println("leftPostCurrent:"+one.getSubjectEnd());
			//it is already fine
			if(maxPos == one.getSubjectEnd()) {
				//System.out.println("shortcut");
				return one;
			}
			String leftS = ref.substring(one.getSubjectStart(), maxPos);
			int queryEndPos = one.getQueryStart()+leftS.length();
			Left l = new Left(leftS,one.getSubjectStart(), maxPos, one.getQueryStart(),queryEndPos);
			//System.out.println(l);
			return l;
		}
		//check the longest
		//get the longest
		int longest = -1;
		LCS max = null;
		int nextLCS = 0;
		for(LCS lcs: lcss) {
			nextLCS++;
			//code to get the correct left and right now for a single range:
			if(nrRanges == 1) {
				Left left  = getMatchLeftCheckLongestOnly(lcs, subjectObject.getStartOfRightFlank(), true, subjectObject.getEndOfLeftFlank(), -1);
				LCS right = null;
				LCS right2 = null;
				if(left!=null) {
					right = getMatchRightLongestOnly(lcs, left.getSubjectEnd(), CompareSequence.minimumSizeWithLeftRight, true, -1);
					//try looking forward as well
					if(nextLCS < lcss.size()) {
						right2 = getMatchRightLongestOnly(lcss.get(nextLCS), left.getSubjectEnd(), CompareSequence.minimumSizeWithLeftRight, true, -1);
					}
				}
				if(left!=null && (right!=null || right2 != null)) {
					//overwrite the right one here because right2 takes priority
					//question remains if that is indeed true
					if(right2 != null) {
						right = right2;
					}
					int tempLength = left.getString().length()+right.getString().length();
					if(tempLength>longest) {
						max = lcs;
						longest = tempLength;
					}
				}
			}
			else {
				//if(lcs.getSubjectStart()<leftPos) {
				//recalculate the length based on the part that we can actually search for
				//based on leftPos
				int end = Math.min(lcs.getSubjectEnd(), rightPos);
				int length = end - lcs.getSubjectStart();
				if(length>longest && lcs.getSubjectStart()<rightPos) {
					if(maxStartPos>-1) {
						if(lcs.getSubjectStart()<=maxStartPos) {
							longest = lcs.length();
							max = lcs;
						}
					}
					else {
						longest = lcs.length();
						max = lcs;
					}
					//System.out.println("SET:"+max+" "+longest);
				}
			}
		}
		//System.out.println("found max:"+max);
		if(max == null) {
			return null;
		}
		boolean jumped = false;
		if(allowJump) {
			//possible take the next one
			LCS second = null;
			for(LCS lcs: lcss) {
				//System.out.println(lcs);
				int absDist = Math.abs(lcs.getSubjectStart()-max.getSubjectEnd());
				int absDistQuery = Math.abs(lcs.getQueryStart()-max.getQueryEnd());
				//if(lcs != max && lcs.getSubjectStart()<leftPos-30 && absDist<=1 && absDistQuery<=1 && lcs.getSubjectStart()>max.getSubjectStart()) {
				//	second = lcs;
					//System.out.println(absDist);
					//System.out.println(absDistQuery);
				//}
				if(lcs != max && lcs.getSubjectStart()<leftPos-30 && lcs.getSubjectStart()>max.getSubjectStart()) {
					//System.out.println(" "+lcs);
					//System.out.println("   "+second);
					//System.out.println(lcs.getSubjectStart()>=max.getSubjectEnd());
					//System.out.println(lcs.getSubjectStart()>max.getSubjectStart());
					if(second == null) { 
						if(lcs.getSubjectEnd() > max.getSubjectEnd() && lcs.getSubjectStart()>max.getSubjectStart() &&
								lcs.getSubjectStart()<rightPos) {
								//&& lcs.getSubjectStart()>=max.getSubjectEnd()) {
							second = lcs;
							//System.out.println("jumping1 "+second);
						}
					}
					else if(lcs.getSubjectStart()>second.getSubjectStart() && lcs.getSubjectEnd()>second.getSubjectEnd() &&
							lcs.getSubjectStart()<rightPos) {
						second = lcs;
						//System.out.println("jumping "+second);
					}
					//engulfment
					else if(lcs.getSubjectStart()<second.getSubjectStart() && lcs.getSubjectEnd()>second.getSubjectEnd()) {
						second = lcs;
						//System.out.println("jumping engulfment "+second);
					}
					
					//System.out.println(absDist);
					//System.out.println(absDistQuery);
				}
			}
			//System.out.println("max:"+max);
			//System.out.println("second:"+second);
			if(second!= null) {
				max = second;
				jumped = true;
			}
		}
		//System.out.println("maxAgain:"+max);
		//System.out.println(leftPos);
		int maxPos = Math.min(max.getSubjectEnd(), rightPos);
		//System.out.println("Changing position to:"+maxPos);
		String leftS = ref.substring(max.getSubjectStart(), maxPos);
		//queryEnd is modified here
		int queryEndPos = max.getQueryStart()+leftS.length();
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
	public LCS getMatchRightLongestOnly(LCS lcs, int startPos, int minSize, boolean allowJump, int minPosition) {
		int longest = -1;
		LCS max = null;
		//calculate the length on what we can actually use
		int start = Math.max(lcs.getSubjectStart(), startPos);
		int length = lcs.getSubjectEnd()-start;
		if(lcs.getSubjectEnd()>startPos && length>=minSize) {
			if(length>longest) {
				//keep the match close to the designated primer if possible
				if(minPosition>-1) {
					if(lcs.getSubjectEnd()>=minPosition) {
						longest = lcs.getString().length();
						max = lcs;
					}
				}
				//normal situation
				else {
					longest = lcs.getString().length();
					max = lcs;
				}
			}
		}
		return max;
	}
	
	public LCS getMatchRight(String seq, int startPos, int minSize, boolean allowJump, int minPosition) {
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
		int longest = -1;
		LCS max = null;
		for(LCS lcs: lcss) {
			//if(lcs.getSubjectEnd()>startPos) {
				//calculate the length on what we can actually use
				int start = Math.max(lcs.getSubjectStart(), startPos);
				int length = lcs.getSubjectEnd()-start;
				if(lcs.getSubjectEnd()>startPos && length>=minSize) {
					if(length>longest) {
						//keep the match close to the designated primer if possible
						if(minPosition>-1) {
							if(lcs.getSubjectEnd()>=minPosition) {
								longest = lcs.getString().length();
								max = lcs;
							}
						}
						//normal situation
						else {
							longest = lcs.getString().length();
							max = lcs;
						}
					}
				}
		}
		if(max == null) {
			return max;
		}
		boolean jumped = false;
		if(allowJump && max!= null) {
			//can we find one closer by that is also long enough?
			LCS second = null;
			for(LCS lcs: lcss) {
				//int absDist = max.getSubjectDist(lcs, startPos);
				int absDist = Math.abs(lcs.getSubjectEnd()-max.getSubjectStart());
				int absDistQuery = Math.abs(lcs.getQueryEnd()-max.getQueryStart());
				//if(lcs.length()>= MINIMUMSECONDSIZE && lcs != max && lcs.getSubjectEnd()>=startPos && absDist<=1 && absDistQuery<=1 && lcs.getSubjectStart()<max.getSubjectStart()) {
				//	second = lcs;
				//}
				if(lcs.length()>= MINIMUMSECONDSIZE && lcs != max && lcs.getSubjectEnd()>startPos && lcs.getSubjectStart()<max.getSubjectStart()) {
					//this will break again the PacBio
					int distToExpectedCut = lcs.getSubjectEnd()-startPos;
					//query has to start earlier 
					boolean queryStartsEarlier = lcs.getQueryStart()<max.getQueryStart();
					//System.out.println(distToExpectedCut);
					//maybe this should be changed to a configurable number instead of 100
					if(absDist<=1 && absDistQuery<=1 || (distToExpectedCut >=100 && queryStartsEarlier)) {
						if(second == null || lcs.getSubjectStart()<second.getSubjectStart()) {
							second = lcs;
							//System.out.println("Switching");
							//System.out.println(second);
						}
					}
					//System.out.println("Current");
					//System.out.println(max);
					//System.out.println(lcs);
					//System.out.println(absDist);
					//System.out.println(absDistQuery);
				}
			}
			if(second != null) {
				max = second;
				jumped = true;
			}
		}
		int startRefPos = Math.max(startPos, max.getSubjectStart());
		if(startRefPos == max.getSubjectEnd()) {
			return null;
		}
		String s = ref.substring(startRefPos, max.getSubjectEnd());
		int startQuery = query.indexOf(s);
		LCS lcs = new LCS(s,startRefPos, max.getSubjectEnd(), startQuery, startQuery+s.length(), jumped);
		return lcs; 
	}
	public LCS getLCS(String seq) {
		//already have this one
		if(!hasQuery(seq)) {
			this.fillLCS(seq);
		}
		return getLCSInternal();
	}
	private LCS getLCSInternal() {
		int maxLength = -1;
		LCS max = null;
		int hits = 0;
		for(LCS lcs: lcss) {
			//System.out.println(m);
			if(lcs.getString().length()>maxLength) {
				maxLength = lcs.getString().length();
				max = lcs;
				hits = 1;
			}
			else if(lcs.getString().length()==maxLength) {
				//System.out.println(lcs.getString()+" ==maxLength");
				hits++;
			}
		}
		if(hits>1) {
			max.setMultipleHits(true);
		}
		return max;
	}
}
