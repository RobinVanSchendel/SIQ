package controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import data.PolyTrendLine;
import data.TrendLine;
import data.Worm;
import data.WormList;
import main.FileInfo;

public class WormSorterController {
	private ArrayList<WormSorterFileController> wsfcs = new ArrayList<WormSorterFileController>();
	private TrendLine t;

	public void addFile(File profDir, FileInfo s) {
		File f = new File(profDir.getAbsolutePath()+File.separator+s.getF());
		File ch0 = new File(profDir.getAbsolutePath()+File.separator+s.getCh0());
		File ch1 = new File(profDir.getAbsolutePath()+File.separator+s.getCh1());
		File ch2 = new File(profDir.getAbsolutePath()+File.separator+s.getCh2());
		File ch3 = new File(profDir.getAbsolutePath()+File.separator+s.getCh3());
		WormSorterFileController wsfc = new WormSorterFileController(f, ch0, ch1, ch2, ch3);
		wsfc.setControl(s.isControl());
		wsfcs.add(wsfc);
	}

	public Map<Double,Double> getControlValues(String column1, String column2, double tofMin, double tofMax) {
		if(tofMin>tofMax) {
			System.err.println("the minus is smaller than the max!");
			System.exit(0);
		}
		Map<Double,Double> max = new TreeMap<Double,Double>();
		for(WormSorterFileController w: wsfcs) {
			if(w.isControl()) {
				double[][] data = w.getData(column1, column2, tofMin, tofMax);
				for(int i = 0;i<data.length;i++) {
					if(max.containsKey(data[i][0])) {
						if(data[i][1]>max.get(data[i][0])){
							max.put(data[i][0], data[i][1]);
						}
					}
					else {
						max.put(data[i][0], data[i][1]);
					}
				}
			}
		}
		return max;
	}

	public void printWormsHighRed(String col1, String col2, double tofMin, double tofMax) {
		if(tofMax<tofMin) {
			System.err.println("tofMax < tofMin");
		}
		for(WormSorterFileController wsfc: wsfcs) {
			WormList wList = wsfc.getWormList();
			int total = 0;
			int countRed = 0;
			for(Worm w: wList.getWorms()) {
				if(w.getTOF()>=tofMin && w.getTOF()<=tofMax) {
					double max = getPredict(w.getData(col1));
					if(w.getData(col2)>max) {
						//System.out.println(wsfc.getFile()+"\t"+w);
						countRed++;
						//System.out.println("Red\t"+wsfc.getFile()+"\t"+w);
					}
					else {
						//System.out.println("nonRed\t"+wsfc.getFile()+"\t"+w);
					}
					total++;
				}
			}
			System.out.println(wsfc.getFile()+"\tTotal: "+total+"\tRed: "+countRed+" Perc:\t"+100*countRed/(double)total+"%");
		}
	}
	public ArrayList<Worm> getWormsHighRed(String col1, String col2, double tofMin, double tofMax) {
		ArrayList<Worm> al = new ArrayList<Worm>();
		if(tofMax<tofMin) {
			System.err.println("tofMax < tofMin");
		}
		for(WormSorterFileController wsfc: wsfcs) {
			WormList wList = wsfc.getWormList();
			for(Worm w: wList.getWorms()) {
				if(w.getTOF()>=tofMin && w.getTOF()<=tofMax) {
					double max = getPredict(w.getData(col1));
					if(w.getData(col2)>max) {
						//System.out.println(wsfc.getFile()+"\t"+w);
						al.add(w);
					}
				}
			}
		}
		return al;
	}
	
	public void setTrendLine(String column1, String column2, double tofMin, double tofMax, int timesSD) {
		Map<Double,Double> test = getControlValues(column1, column2, tofMin, tofMax);
		double[] x = new double[test.size()]; 
		double[] y = new double[test.size()];
		int i =0;
		
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for(double d: test.keySet()) {
			//stats.addValue(test.get(d));
		}
		for(double d: test.keySet()) {
			//System.out.println(d+"\t"+test.get(d));
			x[i] = d;
			y[i] = test.get(d);//+2*stats.getStandardDeviation();
			i++;
		}
		t = new PolyTrendLine(1);
		t.setValues(y, x);
		for(double d: test.keySet()) {
			stats.addValue(test.get(d)-t.predict(d));
		}
		//System.out.println(x);
		t.setDeviation(timesSD*stats.getStandardDeviation());
		for(double d: test.keySet()) {
			System.out.println(column2+"\t"+d+"\t"+test.get(d)+"\t"+this.getPredict(d));
		}
	}
	public double getPredict(double x) {
		if(t!= null) {
			return t.predict(x);
		}
		return Double.NaN;
	}

	public ArrayList<Worm> getWormsNotHighRed(String col1, String col2, double tofMin, double tofMax) {
		ArrayList<Worm> al = new ArrayList<Worm>();
		if(tofMax<tofMin) {
			System.err.println("tofMax < tofMin");
		}
		for(WormSorterFileController wsfc: wsfcs) {
			WormList wList = wsfc.getWormList();
			for(Worm w: wList.getWorms()) {
				if(w.getTOF()>=tofMin && w.getTOF()<=tofMax) {
					double max = getPredict(w.getData(col1));
					if(w.getData(col2)<=max) {
						//System.out.println(wsfc.getFile()+"\t"+w);
						al.add(w);
					}
				}
			}
		}
		return al;
	}

	public void printContents() {
		for(WormSorterFileController wsfc: wsfcs) {
			wsfc.printContents();
		}
	}

	public void printWormsHighRedAndGreen(ArrayList<Worm> reds, String col1, String col2, double tofMin, double tofMax) {
		if(tofMax<tofMin) {
			System.err.println("tofMax < tofMin");
		}
		for(WormSorterFileController wsfc: wsfcs) {
			WormList wList = wsfc.getWormList();
			String fileName = wsfc.getFile();
			int total = 0;
			int countRed = 0;
			int countGreenRed = 0;
			int countGreen = 0;
			ArrayList<Worm> redDone = new ArrayList<Worm>();
			for(Worm w: wList.getWorms()) {
				if(w.getTOF()>=tofMin && w.getTOF()<=tofMax) {
					double max = getPredict(w.getData(col1));
					if(w.getData(col2)>max) {
						//System.out.println(wsfc.getFile()+"\t"+w);
						boolean contains = false;
						for(Worm r: reds){
							if(r.getKey().equals(w.getKey())){
								contains = true;
								break;
							}
						}
						if(contains){
							countGreenRed++;
							//System.out.println("redGreen\t"+w);
							redDone.add(w);
						}
						else{
							countGreen++;
						}
						//System.out.println("Red\t"+wsfc.getFile()+"\t"+w);
					}
					else {
						//System.out.println("nonRed\t"+wsfc.getFile()+"\t"+w);
					}
					total++;
				}
			}
			for(Worm w: reds){
				if(w.getFile().equals(fileName)){
					countRed++;
				}
				boolean contains = false;
				for(Worm r: redDone){
					if(r.getKey().equals(w.getKey())){
						contains = true;
						break;
					}
				}
				if(!contains && wsfc.getFile().equals("1588_05_01_pchange.txt") && w.getFile().equals("1588_05_01_pchange.txt") ){
					//System.out.println("redNONGreen\t"+w);	
				}
			}
			
			countRed-=countGreenRed;
			double percRed = countRed/(double)total;
			double percGreenRed = countGreenRed/(double)total;
			double perGreen = countGreen/(double)total;
			double percGreenRedRed = countGreenRed/(double)(countRed+countGreenRed);
			
			String totalStr = wsfc.getFile()+"\tTotal: "+total+"\tRed: "+countRed+"\tGreen: "+countGreen+" \tGreenRed: "+countGreenRed;
			totalStr+= "\tpercRed: \t"+percRed+"\tpercGreen: \t"+perGreen+"\tpercGreenRed: \t"+percGreenRed;
			totalStr+= "\tpercGreenRedRed\t"+percGreenRedRed+"\t"+(1-percGreenRedRed);
			System.out.println(totalStr);
		}
		
	}

	public ArrayList<Worm> getWormsHighRedAndGreen(ArrayList<Worm> reds, String col1, String col2, double tofMin,
			double tofMax) {
		if(tofMax<tofMin) {
			System.err.println("tofMax < tofMin");
		}
		ArrayList<Worm> redAndGreenDone = new ArrayList<Worm>();
		for(WormSorterFileController wsfc: wsfcs) {
			WormList wList = wsfc.getWormList();
			String fileName = wsfc.getFile();
			int total = 0;
			int countRed = 0;
			int countGreenRed = 0;
			int countGreen = 0;
			for(Worm w: wList.getWorms()) {
				if(w.getTOF()>=tofMin && w.getTOF()<=tofMax) {
					double max = getPredict(w.getData(col1));
					if(w.getData(col2)>max) {
						//System.out.println(wsfc.getFile()+"\t"+w);
						boolean contains = false;
						for(Worm r: reds){
							if(r.getKey().equals(w.getKey())){
								contains = true;
								break;
							}
						}
						if(contains){
							countGreenRed++;
							//System.out.println("redGreen\t"+w);
							redAndGreenDone.add(w);
						}
						else{
							countGreen++;
						}
						//System.out.println("Red\t"+wsfc.getFile()+"\t"+w);
					}
					else {
						//System.out.println("nonRed\t"+wsfc.getFile()+"\t"+w);
					}
					total++;
				}
			}
		}
		return redAndGreenDone;
	}
}
