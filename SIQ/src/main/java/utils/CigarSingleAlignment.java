package utils;

public class CigarSingleAlignment {
	private char ref, query, cigar;
	private int refPos, queryPos;
	private int locInArray;
	public CigarSingleAlignment(char ref, char query, int refPos, int queryPos, char cigar, int loc) {
		this.ref = ref;
		this.query = query;
		this.refPos = refPos;
		this.queryPos = queryPos;
		this.cigar = cigar;
		this.locInArray = loc;
	}
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(ref+":"+query+"|"+refPos+":"+queryPos+"|"+cigar);
		return sb.toString();
	}
	public int getRefPos() {
		return this.refPos;
	}
	public int getLoc() {
		return this.locInArray;
	}
	public boolean match() {
		return ref == query;
	}
	public Character getRef() {
		if(ref == '-') {
			return null;
		}
		return ref;
	}
	public boolean equals(CigarSingleAlignment c) {
		return ref == c.getRef() &&
				query == c.getQuery() &&
				refPos == c.getRefPos() &&
				queryPos == c.getQueryPos();
	}
	private int getQueryPos() {
		return this.queryPos;
	}
	public Character getQuery() {
		if(query == '-') {
			return null;
		}
		return query;
	}
}
