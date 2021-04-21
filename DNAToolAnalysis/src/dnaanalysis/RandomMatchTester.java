package dnaanalysis;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomMatchTester {
	
	
	public static void main(String[] args) {
		Random r = new Random();		
		int trials = 100000;
		for(int queryLength = 4;queryLength<=20; queryLength++) {
			int subjectLength = 100;
			int freq[] = new int[queryLength+1];
					
			for(int i=0;i<trials;i++) {
				String query = getRandomDNA(r, queryLength);
				String subject = getRandomDNA(r, subjectLength);
				//System.out.println(query);
				//System.out.println(subject);
				int match = findBestMatch(query, subject);
				freq[match]++;
			}
			for(int i=0;i<freq.length;i++) {
				System.out.println(queryLength+"\t"+subjectLength+"\t"+i+"\t"+freq[i]+"\t"+freq[i]/(double)trials);
			}
		}

	}
	public static double getLikelihood(String subject, String query, int matches) {
		Random r = new Random();
		int trials = 10000;
		int freq[] = new int[query.length()+1];
		for(int i=0;i<trials;i++) {
			String subjectShuffle = shuffle(subject);
			//System.out.println(query);
			//System.out.println(subject);
			int match = findBestMatch(query, subjectShuffle);
			freq[match]++;
		}
		int total = 0;
		for(int i = matches;i<=query.length();i++) {
			total+=freq[i];
		}
		double fraction = total/(double)trials;
		return fraction;
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

	private static int findBestMatch(String query, String subject) {
		int maxMatch = 0;
		for(int i=0;i<subject.length()-query.length();i++) {
			String subjectPart = subject.substring(i, i+query.length());
			int match = 0;
			for(int j = 0;j<query.length();j++) {
				if(query.charAt(j) == subjectPart.charAt(j)) {
					match++;
				}
			}
			if(match>maxMatch) {
				maxMatch = match;
			}
		}
		return maxMatch;
	}

	private static String getRandomDNA(Random r, int length) {
		StringBuilder sb = new StringBuilder(length);
		for(int i = 0;i<length;i++) {
			int rand = r.nextInt(4);
			char c = 'X';
			switch(rand) {
			case 0:
				c = 'A';
				break;
			case 1:
				c = 'T';
				break;
			case 2:
				c = 'G';
				break;
			case 3:
				c = 'C';
			}
			sb.append(c);
		}
		return sb.toString();
	}

}
