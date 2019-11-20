package utils;

public class MatchTester {

	public static void main(String[] args) {
		String query = "aaagtctgagatctgacgacacggccaaaatggaggtgaacgggg";
		String subject = "agtgtgagatctgacgacacggccgtgtattactgtgcgagaggtccccaccctcgtatgcctgaaaaggcttttgactactggggccagggaaccctggtcaccgtctcctcagcctccaccaagggcccatcggcggccgtgtcgtcagatctcacttccccctggtgagatctgacgacacggccgacctcctccagagcacctctgggggcacagcggccctgggctgcctggtcaaggactacttccccgaaccggtgacggtgtcgtggaactccagggggtgagatctgacgacacggcc";
		Match m = new Match(query, subject);
		m.setPrefferedSubjectPosition(80);
		m.searchBothDirections();
		LargestMatchSearcher lcs = m.findLCS();
		System.out.println(lcs);
		m.setMismatchRate(0.1);
		lcs = m.findLCS();
		System.out.println(lcs);
		System.out.println(query);
		System.out.println(subject);
		System.out.println("****************RC test********");
		String queryRc = Utils.reverseComplement(query);
		m = new Match(queryRc, subject);
		m.searchBothDirections();
		LargestMatchSearcher lcsRC = m.findLCS();
		System.out.println(lcsRC);
		m.setMismatchRate(0.1);
		lcsRC = m.findLCS();
		System.out.println(lcsRC);
		System.out.println(queryRc);
		System.out.println(subject);

	}

}
