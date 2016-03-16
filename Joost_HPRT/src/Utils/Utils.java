package Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.NoSuchElementException;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.SequenceIterator;
import org.biojavax.bio.seq.RichSequence.IOTools;

public class Utils {
	public static String longestCommonSubstring(String S1, String S2)
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
	public static String[] longestCommonSubstringAllowMismatch(String subject, String query, int nrMismatches)
	{
	    if(subject == null || query == null){
	    	return null;
	    }
		subject = subject.toLowerCase();
	    query = query.toLowerCase();
		int StartS1 = 0;
		int StartS2 = 0;
	    int Max = 0;
	    int MaxMismatches = 0;
	    for (int i = 0; i < subject.length(); i++)
	    {
	        for (int j = 0; j < query.length(); j++)
	        {
	            int x = 0;
	            int mismatches = 0;
	            int distanceLastMismatch = 0;
	            boolean lastIsMismatch = false;
	            while (subject.charAt(i + x) == query.charAt(j + x) || (x >= 10 && mismatches<nrMismatches && subject.charAt(i + x) != '|' && query.charAt(j + x) != '|' ))
	            {
	                if(subject.charAt(i + x) != query.charAt(j + x)){
	                	mismatches++;
	                	lastIsMismatch = true;
	                	distanceLastMismatch = 0;
	                	//System.out.println("allowing mismatch");
	                }
	                else{
	                	lastIsMismatch = false;
	                	distanceLastMismatch++;
	                }
	            	x++;
	                if (((i + x) >= subject.length()) || ((j + x) >= query.length())) break;
	            }
	            if (distanceLastMismatch >= 10 && (x > Max || (x==Max && mismatches<MaxMismatches)))
	            {
	                Max = x;
	                StartS1 = i;
	                StartS2 = j;
	                MaxMismatches = mismatches;
	            }
	         }
	    }
	    String S1temp = subject.substring(StartS1, (StartS1 + Max));
	    String S2temp = query.substring(StartS2, (StartS2 + Max));
	    String[] temp = {S1temp,S2temp};
	    return temp;
	}
	public static String reverseComplement(String dna){
		StringBuffer dnaRev = new StringBuffer(dna).reverse();
		String revCom = "";
		for(char c: dnaRev.toString().toCharArray()){
			switch(c){
				case 'a':
					revCom += 't';
					break;
				case 'A':
					revCom += 'T';
					break;
				case 't':
					revCom += 'a';
					break;
				case 'T':
					revCom += 'A';
					break;
				case 'c':
					revCom += 'g';
					break;
				case 'C':
					revCom += 'G';
					break;
				case 'g':
					revCom += 'c';
					break;
				case 'G':
					revCom += 'C';
					break;
				case 'N':
					revCom += 'N';
					break;
				case 'n':
					revCom += 'n';
					break;
				default:
					System.err.println("Can't complement "+c);
			}
		}
		return revCom;
	}
	public static ArrayList<Sequence> fillArrayListSequences(File subject) {
		ArrayList<Sequence> al = new ArrayList<Sequence>();
		BufferedReader is = null;
		try {
			is = new BufferedReader(new FileReader(subject));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		//get a SequenceDB of all sequences in the file
		SequenceIterator si = IOTools.readFastaDNA(is, null);
		while(si.hasNext()){
			try {
				al.add(si.nextSequence());
			} catch (NoSuchElementException e) {
				e.printStackTrace();
			} catch (BioException e) {
				e.printStackTrace();
			}
		}
		return al;
		
	}
}
