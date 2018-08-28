package utils;

public class SetAndPosition {
	public String name;
	public long min, max;
	public SetAndPosition (String name, long min){
		if(name == null) {
			System.err.println("why is the key null??");
		}
		this.name = name;
		this.min = min;
	}
	public SetAndPosition (String name, long min, long max){
		this.name = name;
		this.min = min;
		this.max = max;
	}
	public void setMax(long l) {
		this.max = l;
	}
	public String getName() {
		return name;
	}
	public long getMin() {
		return min;
	}
	public long getMax() {
		return max;
	}
	public boolean withinPosition(int matchStartInt, int matchEndInt, int maxPosDeviation) {
		if(matchStartInt == min && matchEndInt == max) {
			return true;
		}
		if(matchStartInt<min || matchEndInt > max) {
			return false;
		}
		if(matchStartInt>(min+maxPosDeviation)) {
			return false;
		}
		if(matchEndInt<(max-maxPosDeviation)) {
			return false;
		}
		
		return true;
	}
	public static SetAndPosition parse(String nextLine) {
		String[] parts = nextLine.split("\t");
		SetAndPosition sap = new SetAndPosition(parts[0], Integer.parseInt(parts[1]));
		sap.setMax(Integer.parseInt(parts[2]));
		return sap;
	}
	public String toString() {
		return name+" ["+min+":"+max+"]";
	}
	public boolean positionsBounded(int start, int end) {
		return min <= start && max >= end;
	}
}
