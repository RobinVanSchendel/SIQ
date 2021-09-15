package utils;

public class LCS extends Left{

	private boolean multipleHits;
	public LCS(String s, int start, int end, int queryStart, int queryEnd) {
		super(s, start, end, queryStart, queryEnd);
	}
	public LCS(String s, int start, int end, int queryStart, int queryEnd, boolean jumped) {
		super(s, start, end, queryStart, queryEnd, jumped);
	}
	public void setMultipleHits(boolean b) {
		this.multipleHits = b;
	}
	public boolean hasMultipleHits() {
		return multipleHits;
	}
	/*
	public int getSubjectDist(LCS lcs) {
		if(this == lcs) {
			return Integer.MAX_VALUE;
		}
		//only if it resides in front
		if(lcs.getSubjectEnd()<this.getSubjectEnd()) {
			
		}
	}
	*/
	private boolean hasOverlap(LCS lcs) {
		if(lcs.getQueryStart()<this.getQueryStart() &&
				lcs.getQueryEnd()>this.getQueryStart()
				) {
			System.out.println(true);
			return true;
		}
			
		return false;
	}
}
