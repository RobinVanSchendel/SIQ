package main;

public class Line {
	private String line;
	private int countEvents;
	private double fraction;
	private String alias;
	public Line(String line) {
		this.line = line;
	}
	public void setCountEvents(int countEvents) {
		this.countEvents = countEvents;
	}
	public int getCountEvents() {
		return countEvents;
	}
	public void addCountevents(int countEvents2) {
		this.countEvents += countEvents2;
	}
	public String toString(int aliasColumn) {
		String[] parts = line.split("\t");
		parts[0] = ""+countEvents;
		parts[1] = ""+fraction;
		parts[aliasColumn] = alias;
		return String.join("\t", parts);
	}
	public void setFraction(double total) {
		this.fraction = countEvents/total;
	}
	public void setAlias(String alias) {
		this.alias = alias;
		
	}
}
