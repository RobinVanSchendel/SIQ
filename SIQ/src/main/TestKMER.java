package main;

import utils.KMERLocation;
import utils.Left;

public class TestKMER {
	public static void main(String[] args) {
		String ref = "gcttcctcgctcactgactcgctgcgctcggtcgttcggctgcggcgagcggtatcagctcactcaaaggcggtaatacggttatccacagaatcaggggataacgcaggaaagaacatgtgagcaaaaggccagcaaaaggccaggaaccgtaaaaaggccgcgttgctggcgtttttccataggctccgcccccctgacgagcatcacaaaaatcgacgctcaagtcagaggtggcgaaacccgacaggactataaagataccaggcgtttccccctggaagctccctcgtgcgctctcctgttccgaccctgccgcttaccggatacctgtccgcctttctcccttcgggaagcgtggcgctttctcatagctcacgctgtaggtatctcagttcggtgtaggtcgttcgctccaagctgggctgtgtgcacgaaccccccgttcagcccgaccgctgcgccttatccggtaactatcgtcttgagtccaacccggtaagacacgacttatcgccactggcagcagccactggtaacaggattagcagagcgaggtatgtaggcggtgctacagagttcttgaagtggtggcctaactacggctacactagaaggacagtatttggtatctgcgctctgctgaagccagttaccttcggaaaaagagttggtagctcttgatccggcaaacaaaccaccgctggtagcggtggtttttttgtttgcaagcagcagattacgcgcagaaaaaaaggatctcaagaagatcctttgatcttttctacggggtctgacgctcagtggaacgaaaactcacgttaagggattttggtcatgagattatcaaaaaggatcttcacctagatccttttaaattaaaaatgaagttttaaatcaatctaaagtatatatgagtaaacttggtctgacagttaccaatgcttaatcagtgaggcacctatctcagcgatctgtctatttcgttcatccatagttgcctgactccccgtcgtgtagataactacgatacgggagggcttaccatctggccccagtgctgcaatgataccgcgagacccacgctcaccggctccagatttatcagcaataaaccagccagccggaagggccgagcgcagaagtggtcctgcaactttatccgcctccatccagtctattaattgttgccgggaagctagagtaagtagttcgccagttaatagtttgcgcaacgttgttgccattgctacaggcatcgtggtgtcacgctcgtcgtttggtatggcttcattcagctccggttcccaacgatcaaggcgagttacatgatcccccatgttgtgcaaaaaagcggttagctccttcggtcctccgatcgttgtcagaagtaagttggccgcagtgttatcactcatggttatggcagcactgcataattctcttactgtcatgccatccgtaagatgcttttctgtgactggtgagtactcaaccaagtcattctgagaatagtgtatgcggcgaccgagttgctcttgcccggcgtcaatacgggataataccgcgccacatagcagaactttaaaagtgctcatcattggaaaacgttcttcggggcgaaaactctcaaggatcttaccgctgttgagatccagttcgatgtaacccactcgtgcacccaactgatcttcagcatcttttactttcaccagcgtttctgggtgagcaaaaacaggaaggcaaaatgccgcaaaaaagggaataagggcgacacggaaatgttgaatactcatactcttcctttttcaatattattgaagcatttatcagggttattgtctcatgagcggatacatatttgaatgtatttagaaaaataaacaaataggggttccgcgcacatttccccgaaaagtgccacctaaattgtaagcgttaatattttgttaaaattcgcgttaaatttttgttaaatcagctcattttttaaccaataggccgaaatcggcaaaatcccttataaatcaaaagaatagaccgagatagggttgagtgttgttccagtttggaacaagagtccactattaaagaacgtggactccaacgtcaaagggcgaaaaaccgtctatcagggcgatggcccactacgtgaaccatcaccctaatcaagttttttggggtcgaggtgccgtaaagcactaaatcggaaccctaaagggagcccccgatttagagcttgacggggaaagccggcgaacgtggcgagaaaggaagggaagaaagcgaaaggagcgggcgctagggcgctggcaagtgtagcggtcacgctgcgcgtaaccaccacacccgccgcgcttaatgcgccgctacagggcgcgtcccattcgccattcaggctgcgcaactgttgggaagggcgatcggtgcgggcctcttcgctattacgccagctggcgaaagggggatgtgctgcaaggcgattaagttgggtaacgccagggttttcccagtcacgacgttgtaaaacgacggccagtgagcgcgcgtaatacgactcactatagggcgaattgggtaccgggccccccctcgagtttgttgacggcaacgacacggtattcataggtatgacccttttgaacacgagtatccgagaatgcagtaccttgaactggagatgtgttaaccttgatccagcgtccagtctttgcatccttcctctcaatgtcgtaatgatcaactggtgatccaccgttgtcacgtggaggatcccatttgatatcgatatgatcattatcagtatcaacaatttctggtcttcctggctttcctggtgttccaaatggatcctttgcaagaactggctcatcggtgttcaatgaatcagatctaccaaaggtgttctcagccataacacggaactcgtattcgtggccttccgtgagttttggaacagtgattgaggttccagtgacaaatgcagaaactggaacccaggtgttggtctttgtatctctcttctcaacgacatagttgctgatttcagcacctccatcatcctttggtggtttccaattgagaacacaactatccttagtgacatcagaaacttcaagtggtccttctggagcagacggtctatcttgaacaatgacctcgaagatagcttcatcctctcccaactcattcttaaccttcaacttgtagtttccactgtctgcacgtttagcagatgggaagaatattgacgtagtcgagctcttggcatcaacaagaagttctggagcaagagctgctccagaatctccaacagtccaagtggcagttggatcgggagctccaatgaaatcgacttccaagttatgagtgaatccagcctttatcttgatttttctgctagcagttaagatctttggtttcaagtagcgagccttggtgatttgcgggtcagttggatcggatggatcagatggtcctgcagcattgacagcaataacacggaactcgtattcaactcccggtttcaggttatcagcactgaatgttgttccaggagtctttccggcttctgtccagattgcgcttcccttctccttcttctcaacaatatatccagtgactggagctcctccattgtttgcagttgggttccacttaatatcaattctatccttatcccagtcagtaacctctggtttgtcaactggatcaggaacatcaaattgattctttgcaataattggttcttcagcttccaatggttttgattctccttgaagattgactgccttgacacggaatgcgtattcctttccaggaacaagcttattaaccttggctgtacaatctgggaaagttccgacttcctgccatgttccacgagaagtatccatcttctcaacaatgtagtgaagaacatcagttcctccgttatcagttggaggcttccagttcaatgTTATTACCCTGGGCGCCGCATGCGTCGACCCgggaggcctgatttcaCCCCCCCCTCCCCCACCCCCTCCCtcgcAATTCATATGATCCCtacatccttccTTTCCTTCTCCTTTTCCTTCCCCTTTTCCTTCCCCTTTTCCTTCCccttttccttccccttttccttccccttttccttccttttttccttctccttttccttccccttttccttccccttttccttccccttATGaatctcgtcaatcttgagtggtccttctggagttcctggtacatcaagaacagtaacattgcactgagcagtatcttttccatgctcattttcaacaatgattttgtaaactccagtatctccacgaacagcagagaagatgtgaattgctgaggatgttggtgtgttcgtaacatcagctcttgctcctgtatcgattgttgcatcgttggccttccatttagcaactggggctggctctccttcgaatgcgatatcgagcttgatgggtgttccagccttgatacggagatccaaaagtccggcgaggttaagttttggagccattcttcttggtttggcaacaacatttcctgttggatcagatggttttcctggcccagccttattgacagccttcacacggaactgataagtctctcctggagtcaaattatcagcagttgcctttgttgtctttccatcaacacgtgcacactcaacccagtctccaaacttgtccttcttctcaacgatgtaagcatcaattggtgcaccaccgtcgtttgctggtggcttccattcaagatcaacatgatccttatcccaatcaactggggtaacatcagttggtgcatctggttcatcgaatggattcttagcaacaattgcatgatcagaagtcaatggagcagatgttccttgacggtttactgctttcacacggaacttgtattcatgtccttcggatagtttgttaaccttcagagaagtatcagatgtttctccacatggtacccatcgacctccgtcttcttgtttctcaactacgtagttggtaattggagaacctccatcatccaatggttcatcccatttcagcacaacactctccttggtaatatccttgtgacgaagtggcccttctggtgcaccagggatgtcaagaatgttgacatcaacctcatgttcatcttttccactttcattggtagccacaattttgtattttccactatcggcacgagcagtgctctttgttgtcaacttggtgttgttgtcagtgttattgtcgatatgggtatttccaccagatgacaatggagatccgttcaagaaccattcgatctttggagctggctctccttcaacatttacatcaaagttaagagtttgaccagccttaactctttgagcaacaaacatatctctgttgatctttggagcaagatgacgtggcttagcaaccatggttctgctaggatcagatggagtcgattctccagccttgttcaaagccttgacacggaattggtaagtctgacctggtttcaagccatcaactgtagcagtgagctcaccggctccaaccgtgacagctggtacccagtctcctgatggagttctcatttcaacgagatatccttcgattggagcacctccatcatcagctggtggagtccacttgagatctgccggtctccctatagtgagtcgtattaatttcgataagccaggtt";
		ref = ref.toUpperCase();
		KMERLocation kml = new KMERLocation(ref);
		String seq = "XXXXXXXXXXXXXXXXXXXXtagtgagaacatcagttcctccgttatcagttggaggcttccagttcaatgttattaccctgggcgccgcatgcgtcgacccgggaggcctgatttccttttccttccccttttccttccccttttccttccccttttccttccccttttccttccttttttccttctccttttccttccccttttccttccccttttccttccccttatggatctcgtcaatcttgagtggtccttctggagttcctggtacatcaagaacagtaacattgcactgagcagtatcttttccatgctcattttcaacaatgattttgtaaactccagtatctccacgaacagcagagaagatgtgaattgctgaggatgttggtgtgttcgtaacatcagctcttgctcctgtatcgattgttgcatcgttggccttccatttagcaactggggctggctctccttcgaatgcgatatcgagcttgatgggtgttccagccttgatacggagatccaaaagtccggcgaggttaagttttggagccattcttcttggtttggcaacaacatttcctgttggatcagatggttttcctggcccagccttattgacagccttcacacggaactgataagtctctcctggagtcaaattatcagcagttgcctttgttgtctttccatcaacacgtgcacactcaacccagtctccaaacttgccXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";
		System.out.println(seq.length());
		seq = seq.toUpperCase();
		//System.out.println(kml.getLCS(seq));
		String leftFlank = "ggaggcctgatttca";
		leftFlank = leftFlank.toUpperCase();
		int leftPos = ref.indexOf(leftFlank)+leftFlank.length();
		Left l = kml.getMatchLeft(seq, leftPos, true);
		System.out.println("left:"+l);
		int indexL = seq.indexOf(l.getString())+l.getString().length();
		System.out.println("right"+kml.getMatchRight(seq.substring(indexL), l.getSubjectEnd(), 15, true));
	}
}