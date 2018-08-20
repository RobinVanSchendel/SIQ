package main;
import utils.Utils;

public class Test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String left = "ggaagtgcagaaccaatgca";
		String right = "cgcggccgcgatccgctgca";
		String del = "tgcggccgcgaagacagccccggggctagccgtg";
		String hom = Utils.getHomologyAtBreakWithMismatch(left,del,right,0.1);
		System.out.println("Hom: "+hom);
	}

}
