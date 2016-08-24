package Utils;

public class TestInsertionSolverTwoSides {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		InsertionSolverTwoSides is = new InsertionSolverTwoSides("CGTCAATTTGTTTACACCACAATATATCCTG"
				,"aacattggcacagtgatagaaaatgaatTATGACTAACAAAACAAATTTACAAACAAGGACAAATAGTATTCTGAAAT"
				, "AACAAAACAAAACAAATGAATTATGACTAACAAAACAAATTTACAAACAAGGACAAATAGTATTCTGAAATGAATTATGACGCTTAGACAACTAG","test");
		//is.setMinimumMatch(5);
		//is.solveInsertion();
		System.out.println(is.toString());
		System.out.println(is.toStringSolved());
		is.setProbabilityGC(0.180588);
		is.setProbabilityAT(0.319412);
		System.out.println("prob:"+is.getProbability(0));
		is = new InsertionSolverTwoSides("tgtcgttttatcaaaatgtactttcattttataataacgctgcggacatctacatttttgaattgaaaaaaaattggtaattactctttctttttctccatattgaccatcatactcattgctgatccatgtagatttcccggacatgaagccatttacaattgaatatatcctgccgccgctgccgctttgcacccggtggagcttgcatgttggtttctacgcagaactgagccggttaggcagataatttccattgagaactgagccatgtgcacct"
				,"tgcaattaagcaataaacccagaaacatattagtacattgagatcgtaaacgatatctagatgatgaggaaaagggataaaactttgatgggagagaataaggcgcaccgttttcttttccgtcaccattttcgccggcgatggagtgtaatcaaaatgtcttggatctgtgcggtggaggagaagaggagcacgatgaccaaaccgaaaaagcgccgcttcgtcacctttttatagtccgtgtcaattttctagggtttttgctcttcaatttattgga"
				, "tgtagatttcccggacatgtagatttcccggagagaataagg","test");
		System.out.println(is.getProbability(89218231));
		is.setMinimumMatch(8, false);
		is.search(true, true);
		is.solveInsertion();
		System.out.println(is.toStringSolved());
		is.setProbabilityGC(0.180588);
		is.setProbabilityAT(0.319412);
		System.out.println("prob:"+is.getProbability(0));
		is = new InsertionSolverTwoSides("tgtcgttttatcaaaatgtactttcattttataataacgctgcggacatctacatttttgaattgaaaaaaaattggtaattactctttctttttctccatattgaccatcatactcattgctgatccatgtagatttcccggacatgaagccatttacaattgaatatatcctgccgccgctgccgctttgcacccggtggagcttgcatgttggtttctacgcagaactgagccggttaggcagataatttccattgagaactgagccatgtgcacct"
				,"tgcaattaagcaataaacccagaaacatattagtacattgagatcgtaaacgatatctagatgatgaggaaaagggataaaactttgatgggagagaataaggcgcaccgttttcttttccgtcaccattttcgccggcgatggagtgtaatcaaaatgtcttggatctgtgcggtggaggagaagaggagcacgatgaccaaaccgaaaaagcgccgcttcgtcacctttttatagtccgtgtcaattttctagggtttttgctcttcaatttattgga"
				, "tgtagatttcccggacatgtagatttcccggagagaataaggcgatgggagagaataagggagagaata","test");
		System.out.println(is.getProbability(89218231));
		is.setMinimumMatch(8, false);
		is.search(true, true);
		is.solveInsertion();
		System.out.println(is.toStringSolved());
		is.setProbabilityGC(0.180588);
		is.setProbabilityAT(0.319412);
		System.out.println("prob:"+is.getProbability(0));
		
		
	}

}
