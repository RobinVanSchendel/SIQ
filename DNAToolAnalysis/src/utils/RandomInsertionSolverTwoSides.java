package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RandomInsertionSolverTwoSides {
	private InsertionSolverTwoSides is;
	private Random r;
	private boolean searchN = true,searchRC= true;
	private static int ATTEMPTS = 100;
	private static int MINMATCH = 3;
	String left, right, insert;
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
			is.search(searchN, searchRC);
			is.setMinimumMatch(MINMATCH, true);
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
	public static String shuffle(String input){
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
    }
}
