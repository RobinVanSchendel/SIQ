package dnaanalysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.stream.Collectors;

import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.symbol.IllegalSymbolException;

public class RandomInsertionSolverTwoSides {
	private InsertionSolverTwoSides is;
	private Random r;
	private boolean searchN = true,searchRC= true;
	private static int ATTEMPTS = 100;
	private static int MINMATCH = 3;
	String left, right, insert;
	private Vector<Sequence> additionalSearchSequence;
	
	public RandomInsertionSolverTwoSides(String left,String right, String insert){
		this.left = left;
		this.right = right;
		this.insert = insert;
		r = new Random();
	}
	
	public int getMinLength(double prob){
		ArrayList<Integer> sizes = new ArrayList<Integer>();
		for(int i=0;i<ATTEMPTS;i++){
			//scramble left and right
			String leftShuffle = shuffle(left);
			String rightShuffle = shuffle(right);
			InsertionSolverTwoSides is = new InsertionSolverTwoSides(leftShuffle, rightShuffle, insert, i+"");
			//if present also shuffle these....
			//probably very slow
			if(additionalSearchSequence!=null) {
				is.setTDNA(shuffle(additionalSearchSequence));
			}
			is.search(searchN, searchRC);
			is.setMinimumMatch(MINMATCH, true);
			is.setMaxTriesSolved(1);
			is.solveInsertion();
			sizes.add(is.getLargestMatch());
		}
		Collections.sort(sizes);
		/*
		for(int size: sizes){
			System.out.println(size);
		}
		*/
		double lookAt = ATTEMPTS*prob;
		int lookAtInt = (int)lookAt;
		//System.out.println("lookint: "+lookAtInt+" "+sizes.get(lookAtInt));
		return sizes.get(lookAtInt);
	}
	private static Vector<Sequence> shuffle(Vector<Sequence> seqs) {
		Vector<Sequence> temp = new Vector<Sequence>();
		for(Sequence s: seqs) {
			String str = shuffle(s.seqString().toString());
			Sequence seq;
			try {
				seq = DNATools.createDNASequence(str, s.getName());
				temp.add(seq);
			} catch (IllegalSymbolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return temp;
	}

	public boolean isNonRandomInsert(double prob, int largestMatch) {
		int min = this.getMinLength(prob);
		return min<largestMatch;
	}
	public static String shuffle(String str){
		 List<Character> chars = str.chars().mapToObj(e->(char)e).collect(Collectors.toList());
		    Collections.shuffle(chars);
		    return chars.stream().map(e->e.toString()).collect(Collectors.joining());
		/*
        List<Character> characters = new ArrayList<Character>();
        for(char c:input.toCharArray()){
            characters.add(c);
        }
        StringBuilder output = new StringBuilder(input.length());
        while(characters.size()!=0){
            int randPicker = (int)(Math.random()*characters.size());
            output.append(characters.remove(randPicker));
        }
        return output.toString();
        */
    }

	public void setTDNA(Vector<Sequence> seqs) {
		this.additionalSearchSequence = seqs;
	}
}
