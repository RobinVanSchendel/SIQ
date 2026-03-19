package utils;

public class CigarAlignmentSpan {
	private CigarSingleAlignment start, end;
	public CigarAlignmentSpan(CigarSingleAlignment start, CigarSingleAlignment end) {
		this.setStart(start);
		this.setEnd(end);
	}
	public CigarSingleAlignment getStart() {
		return start;
	}
	public void setStart(CigarSingleAlignment start) {
		this.start = start;
	}
	public CigarSingleAlignment getEnd() {
		return end;
	}
	public void setEnd(CigarSingleAlignment end) {
		this.end = end;
	}
	public String toString() {
		return this.start+"\t"+this.end;
	}

}
