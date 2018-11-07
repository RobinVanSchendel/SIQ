package data;

import java.util.ArrayList;

public class Channel {
	private String name;
	private ArrayList<Integer> values = new ArrayList<Integer>();

	public Channel(String string) {
		this.name = string;
	}

	public void addValue(int value) {
		values.add(value);
	}
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for(int v: values) {
			if(sb.length()>0) {
				sb.append("\t");
			}
			sb.append(v);
		}
		return sb.toString();
	}

	public int getHighest() {
		int max = 0;
		for(int i: values) {
			if(i>max) {
				max = i;
			}
		}
		return max;
	}
	public double getAverage() {
		double total = 0;
		double size = 0;
		for(int i: values) {
			if(i>0) {
				total+=i;
				size++;
			}
		}
		return total/size;
	}

	public String getStats() {
		double avg = getAverage();
		double high = getHighest();
		String s = avg+"\t"+high+"\t"+high/avg;
		return s;
	}
	
}
