package data;

import java.util.ArrayList;

public class StructuralVariation {
	public enum SVType {DEL,DELINS,SINS,INV,TD,TDINS, TRANS};
	private SVType type;
	private String insert = "";
	private Location start, end;
	private ArrayList<Sample> samples;
	
	public StructuralVariation(SVType type, Location start, Location end) {
		setType(type);
		setStart(start);
		setEnd(end);
		checkLeftRight();
	}
	
	public StructuralVariation(SVType type, Location start, Location end, String insert) {
		this(type, start, end);
		setInsert(insert);
	}

	private void setInsert(String insert) {
		if(insert != null) {
			this.insert = insert.toUpperCase();
		}
	}

	private void checkLeftRight() {
		//also do this for TRANSLOCATIONS
		//if(start.onSameChromosome(end)) {
			if(start.getPosition()>end.getPosition()) {
				Location temp = end;
				this.end = start;
				this.start = temp;
			}
		//}
	}

	public SVType getType() {
		return type;
	}
	public void setType(SVType type) {
		this.type = type;
	}
	public Location getStart() {
		return start;
	}
	public void setStart(Location start) {
		this.start = start;
	}
	public Location getEnd() {
		return end;
	}
	public void setEnd(Location end) {
		this.end = end;
	}
	public String toString() {
		StringBuffer sb = new StringBuffer();
		String sep = "\t";
		sb.append(type).append(sep);
		sb.append(getStartEndLocation()).append(sep);
		sb.append(getStartEndLocation(0.2)).append(sep);
		sb.append(getSize()).append(sep);
		sb.append(getInsert()).append(sep);
		if(samples!=null) {
			for(Sample s: samples) {
				sb.append(s);
			}
		}
		return sb.toString();
	}
	
	private String getStartEndLocation(double d) {
		int distance = start.getDistance(end);
		if(distance <0 ) {
			return getStartEndLocation();
		}
		int startTemp = (int) (start.getPosition()-d*distance);
		int endTemp = (int) (end.getPosition()+d*distance);
		return start.getChr()+":"+startTemp+"-"+endTemp;
			
	}

	private String getInsert() {
		return insert;
	}

	private int getSize() {
		if(start.onSameChromosome(end)) {
			return end.getPosition()-start.getPosition()+1;
		}
		return -1;
	}

	public String getStartEndLocation() {
		if(start.onSameChromosome(end)) {
			return start.getChr()+":"+start.getPosition()+"-"+end.getPosition();
		}
		else {
			return start.getChr()+":"+start.getPosition()+"|"+end.getChr()+":"+end.getPosition();
		}
	}

	public String getKey() {
		//for now this is sufficient
		StringBuffer sb = new StringBuffer();
		String sep = "_";
		sb.append(type).append(sep);
		sb.append(getStartEndLocation()).append(sep);
		sb.append(getSize()).append(sep);
		//for not the insert is also part of the key
		sb.append(getInsert());
		return sb.toString();
	}

	public void addSample(Sample s) {
		if(samples==null) {
			samples = new ArrayList<Sample>();
		}
		samples.add(s);
	}
}
