package data;

import java.util.ArrayList;
import java.util.HashMap;

public class WormList {
	private HashMap<Integer, Worm> worms = new HashMap<Integer, Worm>();
	public Worm createWorm(int id, String file) {
		Worm w = new Worm(id, file);
		worms.put(id, w);
		return w;
	}
	public String toString() {
		return "I contain "+worms.size()+" worms";
	}
	public void addChannelValue(String string, int id, int value) {
		Worm w = getWorm(id);
		if(w != null) {
			w.addChannel(string, value);
		}
	}
	public Worm getWorm(int id) {
		return worms.get(id);
	}
	public void printWorms(int minTOF, int minCh4) {
		for(Integer key: worms.keySet()) {
			Worm w = worms.get(key);
			if(w.getTOF()>= minTOF && w.getChannel("ch3").getHighest()>minCh4) {
				System.out.println(w.getId());
				System.out.println(w.getChannelsString());
			}
		}
	}
	public void setTOF(int tof) {
		// TODO Auto-generated method stub
	}
	public double[][] getData(String column1, String column2, double tofMin, double tofMax) {
		double[][] data = new double[worms.size()][2];
		int counter = 0;
		for(int key: worms.keySet()) {
			Worm w = worms.get(key);
			if(w.getTOF()>=tofMin && w.getTOF()<=tofMax) {
				data[counter][0] = w.getData(column1);
				data[counter][1] = w.getData(column2);
			}
			counter++;
		}
		return data;
	}
	public ArrayList<Worm> getWorms() {
		ArrayList<Worm> al = new ArrayList<Worm>();
		for(Integer key: worms.keySet()) {
			al.add(worms.get(key));
		}
		return al;
	}
	public boolean contains(Worm w) {
		// TODO Auto-generated method stub
		return false;
	}
}
