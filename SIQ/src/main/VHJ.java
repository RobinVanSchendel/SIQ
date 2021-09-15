package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import dnaanalysis.InsertionSolverTwoSides;
import dnaanalysis.LargestMatchSearcher;
import dnaanalysis.Match;

public class VHJ {

	public static void main(String[] args) {
		File f = new File("Z:\\Datasets - NGS, UV_TMP, MMP\\Temp\\Marvyn_Insertions collected copy 190310.txt");
		try {
			
			int seqColumn = -1;
			int MotifColumn = -1;
			boolean first = true;
			System.out.println(LargestMatchSearcher.getHeader());
			double mmRate = 0.2;
			//for(double mmRate =0;mmRate<1.0;mmRate+=0.01) {
				Scanner s = new Scanner(f);
				int total = 0;
				int totalAlongZero = 0;
				while(s.hasNextLine()) {
					String line = s.nextLine();
					String[] parts = line.split("\t");
					if(first) {
						for(int i=0;i<parts.length;i++) {
							//System.out.println(i+":"+parts[i]);
							if(parts[i].equals("Motif") && MotifColumn==-1) {
								MotifColumn = i;
							}
							else if(parts[i].equals("Sequence")) {
								seqColumn = i;
							}
						}
						first = false;
					}
					else {
						String seq = parts[seqColumn];
						String motif = parts[MotifColumn];
						//System.out.println(line);
						motif = motif.replace("(","");
						motif = motif.replace(")","");
						
						//I already know that some are not
						int index = seq.indexOf(motif); 
						if(index>=0 && motif.length()>=6) {
							//System.out.println(motif+":"+seq);
							String left = seq.substring(0,index);
							String right = seq.substring(index+motif.length());
							//System.out.println(left);
							//System.out.println(motif);
							//System.out.println(right);
							//System.out.println(solveInsertion(motif, left, right, 0, 100, 5));
							String motifLower = motif.toLowerCase();
							String rightLower = right.toLowerCase();
							String leftLower = left.toLowerCase();
							String combined = leftLower+rightLower;
							Match m = new Match(motifLower, combined);
							m.setMismatchRate(mmRate);
							m.setPrefferedSubjectPosition(leftLower.length());
							m.setId(parts[0]);
							LargestMatchSearcher lcs = m.findLCS();
							//String[] matchR = Utils.longestCommonSubstringAllowMismatch(rightLower, motifLower, mismatches, false);
							//String[] matchL = Utils.longestCommonSubstringAllowMismatch(leftLower, motifLower, mismatches, true);
							//String[] combinedMatch = Utils.longestCommonSubstringAllowMismatch(combined, motifLower, mismatches, false);
							//System.out.println(motifLower);
							//System.out.println(rightLower);
							//System.out.println(leftLower);
							//System.out.println("right "+right.length()+" "+String.join(" ", matchR));
							//System.out.println("left "+left.length()+" "+String.join(" ", matchL));
							//System.out.println("combined "+combined.length()+" "+String.join(" ", combinedMatch));
							//System.out.println(motif);
							//System.out.println(combined);
							//System.out.println(leftLower.length()+":L"+leftLower);
							//System.out.println(rightLower.length()+":R"+rightLower);
							System.out.println(lcs);
							if(lcs.getAdjustedPositionStart()<=0 && lcs.getAdjustedPositionEnd()>0) {
								totalAlongZero++;
							}
							total++;
							//System.out.println("****");
							
						}
					}
				}
				double perc = totalAlongZero/(double)total;
				System.out.println(mmRate+"\t"+perc+"\t"+totalAlongZero+"\t"+total);
				s.close();
			//}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	private static InsertionSolverTwoSides solveInsertion(String insert, String left, String right, int start, int end, int maxTries) {
		//disabled for translocation
		InsertionSolverTwoSides is = new InsertionSolverTwoSides(left, right,insert,"name");
		is.setAdjustedPositionLeft(start);		
		is.setAdjustedPositionRight(start);
		is.search(true, true);
		is.setMaxTriesSolved(maxTries);
		is.setMinimumMatch(3, false);
		is.solveInsertion();
		return is;
		/*
		//now determine if this is random or not
		//one peculiar thing is if the flanks overlap it is now quite fair anymore
		int leftStart = subject.seqString().indexOf(left);
		int leftEnd = start+left.length();
		int rightStart = subject.seqString().indexOf(right);
		if( (leftEnd) > rightStart) {
			int tooLarge = leftEnd-rightStart;
			int cut = tooLarge/2;
			right = right.substring(cut);
			left = left.substring(0, left.length()-cut);
		}
		
		RandomInsertionSolverTwoSides ris = new RandomInsertionSolverTwoSides(left,right, insert);
		*/
	}

}
