package utils;

import java.util.ArrayList;

public class KMER {
	private String s;
	private ArrayList<Integer> locs = new ArrayList<Integer>();
	public KMER(String s, int loc) {
		this.s = s;
		locs.add(loc);
	}
	public void addLocation (int loc) {
		locs.add(loc);
	}
	public ArrayList<Integer> getLocation() {
		return locs;
	}
	public String toString(){
		return s +" "+ locs;
	}

}
