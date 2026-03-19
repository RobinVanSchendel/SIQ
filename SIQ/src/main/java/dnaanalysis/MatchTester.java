package dnaanalysis;

public class MatchTester {

	public static void main(String[] args) {
		String query = "ctgggagtctcacttctg";
		String subject = "atacgacgtacttcggagaccctgtccctcacctgcactgtctctggtggctccatcagtgataataattactactggggcttgatccgccagcccccagggaaggggctggagtggattggcagtatctattatagtgggagcacctactacaacccgtccctcaagagtcgcgtcaccatatccgtagacacgtccaagaacctgttctccctgaagctgaactctgtgaccgccgcagacacggctgtctattactgtgcgagacatgcgacggggattcacacctggggccagggaaccctggtcaccgtctcctcagcatccccgaccagccccaaggtcttcccgctgagcctctgcagcacccagccagatgggaacgtggtcatcgcctgcctggtccagggcttcttcccccaggagccactcagtgtgacctggagcgaaagtacgtcgtat";
		Match m = new Match(query, subject);
		m.setPrefferedSubjectPosition(80);
		m.setSearchRC(false);
		//LargestMatchSearcher lcs = m.findLCS();
		//System.out.println(lcs);
		m.setMismatchRate(0.8);
		LargestMatchSearcher lcs = m.findLCS();
		System.out.println(lcs);
		System.out.println(query);
		System.out.println(subject);
		/*
		System.out.println("****************RC test********");
		String queryRc = Utils.reverseComplement(query);
		m = new Match(queryRc, subject);
		m.searchBothDirections();
		LargestMatchSearcher lcsRC = m.findLCS();
		//System.out.println(lcsRC);
		m.setMismatchRate(0.2);
		lcsRC = m.findLCS();
		System.out.println(lcsRC);
		//System.out.println(queryRc);
		//System.out.println(subject);
		*/
		int maxMatch = 0;
		int pos = -1;
		for(int i =0;i<subject.length()-query.length();i++) {
			String subjectPart = subject.substring(i, i+query.length());
			int match = 0;
			for(int j =0;j<query.length();j++) {
				if(query.charAt(j)==subjectPart.charAt(j)) {
					match++;
				}
			}
			if(match>maxMatch) {
				maxMatch = match;
				pos = i;
				System.out.println("Setting max to: "+maxMatch+" pos "+pos);
			}
		}
	}

}
