package data;

public class Location {
	private static final int NEIGHBOURHOOD = 50;
	public String chr;
	public int position;
	
	public Location(String contig, int start) {
		setChr(contig);
		setPosition(start);
	}
	public String getChr() {
		return chr;
	}
	public void setChr(String chr) {
		//not sure if this is always correct, but keep it for now
		if(chr.endsWith(".0")) {
			chr = chr.replace(".0", "");
		}
		this.chr = chr;
	}
	public int getPosition() {
		return position;
	}
	public void setPosition(int position) {
		this.position = position;
	}

	public boolean onSameChromosome(Location end) {
		return this.getChr().contentEquals(end.getChr());
	}
	//parses a string in the format <chr>:<integer>
	public static Location parse(String substring) {
		String[] parts = substring.split(":");
		return new Location(parts[0],Integer.parseInt(parts[1]));
	}
	public String toString() {
		return chr+":"+position;
	}
	public int getDistance(Location end) {
		if(onSameChromosome(end)) {
			return end.position-this.position;
		}
		return -1;
	}
	public boolean isNeighbourHooud(Location start) {
		if(this.onSameChromosome(start)) {
			int dis = Math.abs(this.getPosition()-start.getPosition());
			if(dis<NEIGHBOURHOOD) {
				return true;
			}
		}
		return false;
	}
}
