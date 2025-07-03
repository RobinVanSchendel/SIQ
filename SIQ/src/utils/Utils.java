package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import org.biojava.bio.BioException;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequence.IOTools;
import org.biojavax.bio.seq.RichSequenceIterator;
import org.jcvi.jillion.core.pos.PositionSequence;
import org.jcvi.jillion.trace.chromat.Channel;
import org.jcvi.jillion.trace.chromat.Chromatogram;


public class Utils {
	public static String getMutation(String ref, String alt){
		ref = ref.toUpperCase();
		alt = alt.toUpperCase();
		if((ref.equals("A") && alt.equals("G")) || (ref.equals("T") && alt.equals("C"))){
			return "AT->GC";
		}
		else if((ref.equals("G") && alt.equals("A")) || (ref.equals("C") && alt.equals("T"))){
			return "GC->AT";
		}
		else if((ref.equals("A") && alt.equals("C")) || (ref.equals("T") && alt.equals("G"))){
			return "AT->CG";
		}
		else if((ref.equals("G") && alt.equals("T")) || (ref.equals("C") && alt.equals("A"))){
			return "GC->TA";
		}
		else if((ref.equals("A") && alt.equals("T")) || (ref.equals("T") && alt.equals("A"))){
			return "AT->TA";
		}
		else if((ref.equals("G") && alt.equals("C")) || (ref.equals("C") && alt.equals("G"))){
			return "GC->CG";
		}
		return ref+"->"+alt;
	}
	public static String longestCommonSubstring(String S1, String S2)
	{
	    if(S1 == null || S2 == null || S1.equals("")|| S2.equals("")){
	    	return "";
	    }
	    //System.out.println("======miss");
	    //System.out.println(S1);
	    //System.out.println(S2);
		//S1 = S1.toLowerCase();
	    //S2 = S2.toLowerCase();
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
	            if (x > Max)
	            {
	                Max = x;
	                Start = i;
	            }
	            //possible speedup
	            if(Max == S1.length() || Max == S2.length()) {
	            	break;
	            }
	        }
	        //possible speedup
	        if(Max == S1.length() || Max == S2.length()) {
            	break;
            }
	    }
	    String sub = S1.substring(Start, (Start + Max));
	    //long mem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
	    //System.out.println("Usage "+mem);
	    return sub;
	}
	public static String getHomologyAtBreak(String left, String del, String right) {
		if(left == null || right == null){
			return "";
		}
		//make them the same case:
		//check the left part
		left = new StringBuffer(left).reverse().toString();
		del = new StringBuffer(del).reverse().toString();
		int index = 0;
		while(left.charAt(index) == del.charAt(index)){
			index++;
			if(index == left.length() || index == del.length()){
				break;
			}
		}
		String leftHom = left.substring(0, index);
		leftHom = new StringBuffer(leftHom).reverse().toString();
		
		//right part
		//turn it again back around
		del = new StringBuffer(del).reverse().toString();
		index = 0;
		while(right.charAt(index) == del.charAt(index)){
			index++;
			if(index == right.length() || index == del.length()){
				break;
			}
		}
		String rightHom = right.substring(0, index);
		String ret = leftHom+rightHom;
		if(ret.length()> del.length()){
			ret = del;
		}
		return ret;
	}
	public static String getHomologyAtBreakWithMismatch(String left, String delOrig, String right, double mismatchRate) {
		//now also do it for insertions
		if(left == null || right == null) {
			return "";
		}
		if(delOrig == null){
			return "";
		}
		//check the left part
		left = new StringBuffer(left).reverse().toString();
		String del = new StringBuffer(delOrig).reverse().toString();
		//System.out.println("left"+left);
		//System.out.println("del"+del);
		
		int index = 0;
		int returnIndex = 0;
		int numberCorrect = 0;
		int numberMisses = 0;
		int lastCorrectIndex = 0;
		String homT = "";
		while(index < left.length() && index < del.length()){
			if(left.charAt(index) == del.charAt(index)){
				numberCorrect++;
				homT+=left.charAt(index);
			}
			else{
				numberMisses++;
				homT+='X';
			}
			//System.out.println(left.charAt(index)+" "+del.charAt(index));
			//calculate the mismatch and see if this is a good index
			//System.out.println("correct:"+numberCorrect+ " numbermiss: "+numberMisses);
			//System.out.println(numberMisses/((double)numberCorrect+numberMisses));
			if(numberMisses/((double)numberCorrect+numberMisses) <= mismatchRate){
				returnIndex = index+1;
				if(left.charAt(index) == del.charAt(index)){
					lastCorrectIndex = returnIndex;
					//System.out.println("lastCorrectIndex\t"+lastCorrectIndex);
				}
				//System.out.println("ok: "+homT);
			}
			index++;
		}
		//System.out.println("index:"+returnIndex);
		//System.out.println("indexReturn:"+returnIndex);
		String leftHom = homT.substring(0, lastCorrectIndex);
		//System.out.println("leftHom:"+leftHom);
		leftHom = new StringBuffer(leftHom).reverse().toString();
		
		//right
		//now also do it for insertions
		//System.out.println("right"+right);
		//System.out.println("del"+del);
		del = delOrig;
		//update to leftHom length
		numberCorrect = leftHom.replaceAll("X", "").length();
		numberMisses = leftHom.replaceAll("[ACGT]", "").length();
		index = 0;
		returnIndex = 0;
		homT = "";
		lastCorrectIndex = 0;
		while(index < right.length() && index < del.length()){
			//System.out.println(right.charAt(index));
			//System.out.println("cor:"+numberCorrect);
			//System.out.println("mis:"+numberMisses);
			//System.out.println(index+" right: "+right.charAt(index)+" del: "+del.charAt(index));
			if(right.charAt(index) == del.charAt(index)){
				numberCorrect++;
				homT+=right.charAt(index);
			}
			else{
				numberMisses++;
				homT+='X';
			}
			//calculate the mismatch and see if this is a good index
			//System.out.println("cor:"+numberCorrect);
			//System.out.println("mis:"+numberMisses);
			
			//System.out.println(numberMisses/((double)numberCorrect+numberMisses));
			if(numberMisses/((double)numberCorrect+numberMisses) <= mismatchRate){
				returnIndex = index+1;
				if(right.charAt(index) == del.charAt(index)){
					lastCorrectIndex = returnIndex;
					//System.out.println("updateing" +lastCorrectIndex);
				}
				 
			}
			index++;
		}
		//System.out.println("index:"+index);
		//System.out.println("indexReturn:"+returnIndex);
		//System.out.println("homT:"+homT+ ":"+lastCorrectIndex);
		String rightHom = homT.substring(0, lastCorrectIndex);
		//System.out.println("rightHom:"+rightHom);
		//System.out.println("------------------");
		String ret = leftHom+rightHom;
		if(ret.length()> del.length()){
			ret = del;
		}
		return ret;
	}
	
	public static String[] longestCommonSubstringAllowMismatch(String subject, String query, int nrMismatches, boolean isLeft)
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
	            }
	         }
	    }
	    String S1temp = subject.substring(StartS1, (StartS1 + Max));
	    String S2temp = query.substring(StartS2, (StartS2 + Max));
	    String mismatch = "";
	    String location = StartS1+"-"+(StartS1 + Max);
	    if(isLeft) {
	    	int startLocation = subject.length()-((StartS1 + Max));
	    	int endLocation = subject.length()-StartS1;
	    	location = startLocation+"-"+endLocation; 
	    }
	    int mismatches = 0;
	    int matches = 0;
	    for(int i = 0;i<S1temp.length();i++) {
	    	if(S1temp.charAt(i) == S2temp.charAt(i)) {
	    		mismatch+="-";
	    		matches++;
	    	}
	    	else {
	    		mismatch+="X";
	    		mismatches++;
	    	}
	    }
	    String[] temp = {S1temp,S2temp, mismatch, location, matches+":"+mismatches};
	    return temp;
	}
	public static String reverseComplement(String dna){
		StringBuffer dnaRev = new StringBuffer(dna).reverse();
		StringBuffer revCom = new StringBuffer(dnaRev.length());
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
				default:
					//disable this check for now
					//System.err.println("Can't complement "+c);
					revCom.append(c);
			}
		}
		return revCom.toString();
	}
	public static ArrayList<RichSequence> fillArrayListSequences(File subject) {
		ArrayList<RichSequence> al = new ArrayList<RichSequence>();
		BufferedReader is = null;
		try {
			is = new BufferedReader(new FileReader(subject));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			System.exit(0);
		}
		//get a SequenceDB of all sequences in the file
		RichSequenceIterator si = IOTools.readFastaDNA(is, null);
		while(si.hasNext()){
			try {
				al.add(si.nextRichSequence());
			} catch (NoSuchElementException e) {
				e.printStackTrace();
			} catch (BioException e) {
				e.printStackTrace();
			}
		}
		return al;
		
	}
	public static RichSequence matchNameSequence(ArrayList<RichSequence> sequences,
			String name) {
		for(RichSequence s: sequences){
			if(name.contains(s.getName())){
				return s;
			}
		}
		return null;
	}
	public static HashMap<String, String> fillHashWithAddSequences(ArrayList<RichSequence> sequences) {
		if(sequences.size()<2) {
			return null;
		}
		HashMap<String, String> hmAdditional = new HashMap<String, String>();
		int index = 0;
		for(RichSequence rc: sequences) {
			//skip first!!
			if(index >0) {
				hmAdditional.put(rc.getName(), rc.seqString().toString());
			}
			index++;
		}
		return hmAdditional;
	
	}
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Entry.comparingByValue());

        Map<K, V> result = new LinkedHashMap<>();
        for (Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }
	public static String getSecondarySequence(Chromatogram chromo) {
		StringBuffer secondary = new StringBuffer();
		ArrayList<Integer> peaksA = getPeaks(chromo.getChannelGroup().getAChannel());
		ArrayList<Integer> peaksT = getPeaks(chromo.getChannelGroup().getTChannel());
		ArrayList<Integer> peaksC = getPeaks(chromo.getChannelGroup().getCChannel());
		ArrayList<Integer> peaksG = getPeaks(chromo.getChannelGroup().getGChannel());
		//debug
		
		
		for(int i = 0; i<chromo.getPeakSequence().getLength();i++) {
			int start = 0;
			int end = 0;
			//first starts at 0;
			if(i==0) {
				start = 0;
				int peakLocation = chromo.getPeakSequence().get(i).getValue();
				int peakLocationNext = chromo.getPeakSequence().get(i+1).getValue();
				end = (int) (peakLocation+0.5*(peakLocationNext-peakLocation));
			}
			else if(i==chromo.getPeakSequence().getLength()-1) {
				int peakLocationPrev = chromo.getPeakSequence().get(i-1).getValue();
				int peakLocation = chromo.getPeakSequence().get(i).getValue();
				start = (int) (peakLocation-0.5*(peakLocation-peakLocationPrev));
				end = chromo.getNumberOfTracePositions();
			}
			else {
				int peakLocationPrev = chromo.getPeakSequence().get(i-1).getValue();
				int peakLocation = chromo.getPeakSequence().get(i).getValue();
				int peakLocationNext = chromo.getPeakSequence().get(i+1).getValue();
				start = (int) (peakLocation-0.5*(peakLocation-peakLocationPrev));
				end = (int) (peakLocation+0.5*(peakLocationNext-peakLocation));
				
			}
			//System.out.println(start+":"+end);
			//System.out.println((i+1)+":"+chromo.getNucleotideSequence().get(i));
			String seqTemp = getMaxPeaks(chromo,start,end,peaksA,peaksT,peaksC,peaksG, chromo.getNucleotideSequence().get(i).toString());
			//System.out.println(seqTemp);
			secondary.append(seqTemp);
			if(i==249) {
				//System.out.println(peaksC);
				//System.exit(0);
			}
			//System.out.println((i+1)+":"+chromo.getNucleotideSequence().get(i)+":"+peaks);
			//String print = (i+1)+"\t"+a.get(peakLocation)+"\t"+t.get(peakLocation)+"\t"+c.get(peakLocation)+"\t"+g.get(peakLocation)+"\t"+chromo.getNucleotideSequence().get(i)+"\t"+chromo.getPeakSequence().get(i);
			//System.out.println(print);
		}
		//System.out.println(a.getLength());
		//System.out.println(t);
		//System.out.println(c);
		//System.out.println(g);
		System.out.println(chromo.getNucleotideSequence().toString().length());
		System.out.println(chromo.getNucleotideSequence().toString());
		System.out.println(secondary.toString());
		System.out.println(secondary.length());
		return secondary.toString();
		//System.exit(0);
	}
	private static ArrayList<Integer> getPeaks(Channel channel) {
		ArrayList<Integer> numbers = new ArrayList<Integer>();
		ArrayList<Integer> length = new ArrayList<Integer>();
		int lastPosition = Integer.MIN_VALUE;
		int nrTimes = 1;
		for(int i=0;i<channel.getPositionSequence().getLength();i++) {
			int value = channel.getPositionSequence().get(i).getValue();
			if(lastPosition!=value) {
				numbers.add(lastPosition);
				length.add(nrTimes);
				nrTimes = 0;
			}
			lastPosition = value;
			nrTimes++;
			//System.out.println(i+" "+value);
		}
		//add last one
		numbers.add(lastPosition);
		length.add(nrTimes);
		
		
		ArrayList<Integer> indexes = extrema(numbers);
		ArrayList<Integer> indexesReal = new ArrayList<Integer>();
		
		/*
		System.out.println(channel.getPositionSequence());
		System.out.println(numbers);
		System.out.println(length);
		System.out.println(indexes);
		*/
		
		int index = indexes.remove(0);
		//since infinitive was added
		int indexReal = -1;
		for(int i=0;i<length.size();i++) {
			int times = length.get(i);
			if(i==index) {
				for(int j=0;j<times;j++) {
					indexesReal.add(indexReal+j);
				}
				if(indexes.size()>0) {
					index = indexes.remove(0);
				}
			}
			indexReal+=times;
		}
		return indexesReal;
	}
	private static ArrayList<Integer> extrema(ArrayList<Integer> a) 
    { 
        ArrayList<Integer> indexes = new ArrayList<Integer>();
        for (int i = 1; i < a.size() - 1; i++)  
        { 
            if(a.get(i) > a.get(i-1) && a.get(i) > a.get(i+1)) {
            	indexes.add(i);
            } 
        } 
        return indexes; 
    }
	private static String getMaxPeaks(Chromatogram chromo, int start, int end, ArrayList<Integer> peaksA,
			ArrayList<Integer> peaksT, ArrayList<Integer> peaksC, ArrayList<Integer> peaksG, String nucleotide) {
		double ratio = 0.33;
		//A
		PositionSequence aPos = chromo.getChannelGroup().getAChannel().getPositionSequence();
		PositionSequence tPos = chromo.getChannelGroup().getTChannel().getPositionSequence();
		PositionSequence cPos = chromo.getChannelGroup().getCChannel().getPositionSequence();
		PositionSequence gPos = chromo.getChannelGroup().getGChannel().getPositionSequence();
		int aMax = -1;
		int tMax = -1;
		int cMax = -1;
		int gMax = -1;
		for(int i = start+1;i<end;i++) {
			if(peaksA.contains(i)) {
				aMax=i;
			}
			if(peaksT.contains(i)) {
				tMax = i;
			}
			if(peaksC.contains(i)) {
				cMax = i;
			}
			if(peaksG.contains(i)) {
				gMax = i;
			}
		}
		//System.out.println("=");
		ArrayList<Integer> list = new ArrayList<Integer>();
		if(aMax>0) {
			list.add(aPos.get(aMax).getValue());
		}
		if(tMax>0) {
			list.add(tPos.get(tMax).getValue());
		}
		if(cMax>0) {
			list.add(cPos.get(cMax).getValue());
		}
		if(gMax>0) {
			list.add(gPos.get(gMax).getValue());
		}
		//System.out.println("Size: "+list.size()+" "+list);
		if(list.size()==0) {
			return "N";
		}
		double max = Collections.max(list);
		//System.out.println(max);
		ArrayList<Double> ratios = new ArrayList<Double>();
		double aRatio = 0.0;
		double tRatio = 0.0;
		double cRatio = 0.0;
		double gRatio = 0.0;
		
		if(aMax>0) {
			aRatio = aPos.get(aMax).getValue()/max;
		}
		if(tMax>0) {
			tRatio = tPos.get(tMax).getValue()/max;
		}
		if(cMax>0) {
			cRatio = cPos.get(cMax).getValue()/max;
		}
		if(gMax>0) {
			gRatio = gPos.get(gMax).getValue()/max;
		}
		
		String ret = "";
		if(aRatio>ratio) {
			ratios.add(aRatio);
		}
		if(tRatio>ratio) {
			ratios.add(tRatio);
		}
		if(cRatio>ratio) {
			ratios.add(cRatio);
		}
		if(gRatio>ratio) {
			//System.out.println("G:"+gRatio);
			ratios.add(gRatio);
		}
		Collections.sort(ratios, Collections.reverseOrder());
		//System.out.println("ratios "+ratios);
		for(int i=0;i<ratios.size();i++) {
			double ratioAmount = ratios.get(i);
			if(ratioAmount == aRatio && !ret.contains("A")) {
				ret+="A";
			}
			else if(ratioAmount == tRatio && !ret.contains("T")) {
				ret+="T";
			}
			else if(ratioAmount == cRatio && !ret.contains("C")) {
				ret+="C";
			}
			else if(ratioAmount == gRatio && !ret.contains("G")) {
				ret+="G";
			}
		}
		//System.out.println(ret);
		//test
		/*
		if(ret.length()>0) {
			if(ret.charAt(0)!=nucleotide.charAt(0)) {
				System.out.println("The highest peak is not the value taken");
				System.out.println(aRatio+":"+tRatio+":"+cRatio+":"+gRatio);
				System.out.println((index+1)+":" +ret.charAt(0)+": "+nucleotide.charAt(0)+" :"+ret);
				System.out.println(tRatio);
			}
		}
		*/
		//some weird hack
		//System.exit(0);
		if(ret.length()==0 || ret.length()==4) {
			//clueless
			return "N";
		}
		else {
			if(ret.length()==1) {
				return ret;
			}
			//take out the nucleotide taken by the basecaller
			//that is not alway the top ranked nucleotide...
			else {
				ret = ret.replace(nucleotide, "");
				return translateToIUPAC(ret);
			}
		}
	}
	private static String translateToIUPAC(String substring) {
		if(substring.length()==1) {
			return substring;
		}
		else {
			char[] chars = substring.toCharArray();
			Arrays.sort(chars);
			String sorted = new String(chars);
			//System.out.println("Sorted:"+sorted);
			switch(sorted) {
			case "AT":
				return "W";
			case "AG":
				return "R";
			case "CT":
				return "Y";
			case "CG":
				return "S";
			case "GT":
				return "K";
			case "AC":
				return "M";
			case "ACG":
				return "V";
			case "ACT":
				return "H";
			default:
				System.err.println("nucleotide combination:"+sorted+" unknown");
				return "N";
			}
		}
	}
}
