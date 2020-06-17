package data;

public class G4 {
	private String sequence;
	private String chr;
	private int start;
	private int stop;
	private boolean forward;
	
	public G4(String g4, String chr, int start, int end, boolean forward) {
		this.sequence = g4;
		this.chr = chr;
		this.start = start;
		this.stop = end;
		this.forward = forward;
	}
	public String toString() {
		String s = ":";
		return chr+s+start+s+stop+s+sequence+s+forward; 
	}
	public String getLocation() {
		String loc = chr+":"+start+"-"+stop;
		return loc;
	}
	public String getChr() {
		return chr;
	}
	public int getStart() {
		return start;
	}
	public boolean overlaps(StructuralVariation sv) {
		if(sv.getStart().getChr().contentEquals(chr) &&
				sv.getEnd().getChr().contentEquals(chr)) {
					if(sv.getStart().getPosition()<=stop && sv.getEnd().getPosition()>=start) {
						return true;
					}
		}
		return false;
	}
	public boolean isForward() {
		return forward;
	}
	public int getEnd() {
		return stop;
	}
	
}
