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
}
