package utils;

import java.util.ArrayList;

public class Match {
	private String query, subject;
	private ArrayList<LargestMatchSearcher> lcs = new ArrayList<LargestMatchSearcher>();
	private double mismatchRate = 0.0;
	private boolean isLeft = true;
	private boolean searchF = true, searchRC = true;
	private int preferredPosition;
	public Match(String query, String subject) {
		this.query = query.toLowerCase();
		this.subject = subject.toLowerCase();
	}
	public void setMismatchRate(double d) {
		if(d>=0.0 && d<1.0) {
			this.mismatchRate = d;
		}
		else {
			System.err.println("mismatchRate is not 0.0<=rate<1.0");
		}
	}
	//Next step is to make it iterative
	public LargestMatchSearcher findLCS() {
		LargestMatchSearcher lcs = LargestMatchSearcher.getLCS(this);
		return lcs;
	}
	public void setSearchFromLeft(boolean b) {
		this.isLeft = b;
	}
	public String getQuery() {
		return query;
	}
	public String getSubject() {
		return subject;
	}
	public boolean isLeft() {
		return this.isLeft;
	}
	public int getNrMismatchesAllowed() {
		return (int) (this.mismatchRate*query.length());
	}
	public void setSearchBothDirections() {
		this.searchF = true;
		this.searchRC = true;
	}
	public boolean searchBothDirections() {
		return searchF & searchRC;
	}
	public void setPrefferedSubjectPosition(int i) {
		this.preferredPosition = i;
	}
	public int getPreferredPosition() {
		return this.preferredPosition;
	}
	
}
