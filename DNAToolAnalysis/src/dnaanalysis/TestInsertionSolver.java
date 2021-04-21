package dnaanalysis;

public class TestInsertionSolver {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		InsertionSolver is = new InsertionSolver("ttatagatagttggaaattgtttcaaatgacaatattatgac", "tagttggaa","test");
		is.setMinimumMatch(5);
		is.solveInsertion();
		System.out.println(is.toString());
		is = new InsertionSolver("ttatagatagttggaaattgtttcaaatgacaatattatgac", "tagttggaga","test");
		is.setMinimumMatch(5);
		is.solveInsertion();
		System.out.println(is.toString());
		is = new InsertionSolver("ttatagatagttggaaattgtttcaaatgacaatattatgac", "tagttggagacaata","test");
		is.setMinimumMatch(5);
		is.solveInsertion();
		System.out.println(is.toString());
		is = new InsertionSolver("ttatagatagttggaaattgtttcaaatgacaatattatgac", "xng","test");
		is.setMinimumMatch(5);
		is.solveInsertion();
		System.out.println(is.toString());
		is = new InsertionSolver("ttatagatagttggaaattgtttcaaatgacaatattatgac", "xngrw","test");
		is.setMinimumMatch(5);
		is.solveInsertion();
		System.out.println(is.toString());
		is = new InsertionSolver("ttaaactatttttaaaaagacaaaacaataagagggcccatacctgtcca", "ttaaa","test", true);
		is.setMinimumMatch(5);
		is.solveInsertion();
		System.out.println(is.toString());
		is = new InsertionSolver("ttaaactatttttaaaaagacaaaacaataagagggcccatacctgtcca", "ttaaa","test", false);
		is.setMinimumMatch(5);
		is.solveInsertion();
		System.out.println(is.toString());
		
	}

}
