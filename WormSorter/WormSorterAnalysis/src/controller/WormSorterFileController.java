package controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import data.Worm;
import data.WormList;

public class WormSorterFileController {
	private File f, ch0, ch1, ch2, ch3;
	private int[][] ch0A, ch1A, ch2A, ch3A;
	private WormList wl = new WormList();
	private final static int MAX = 0;
	private boolean control;
	private String pmt;
	public WormSorterFileController(File f, File ch0, File ch1, File ch2, File ch3) {
		this.f = f;
		this.ch0 = ch0;
		this.ch1 = ch1;
		this.ch2 = ch2;
		this.ch3 = ch3;
		readFile(MAX);
		//readChannels(MAX);
	}
	public WormSorterFileController(File f) {
		this.f = f;
		readFile(MAX);
	}
	private void readChannels(int nr) {
		int lines = getNrLines(ch0);
		ch0A = readChannelArray("ch0", ch0, lines);
		ch1A = readChannelArray("ch1", ch1, lines);
		ch2A = readChannelArray("ch2", ch2, lines);
		ch3A = readChannelArray("ch3", ch3, lines);
		/*
		printWormChannel(5);
		printWormChannel(1);
		readChannel("ch0", ch0, nr);
		readChannel("ch1", ch1, nr);
		readChannel("ch2", ch2, nr);
		readChannel("ch3", ch3, nr);
		*/
	}
	private int getNrLines(File ch02) {
		Scanner s;
		int lines = 0;
		try {
			s = new Scanner(ch02);
			while(s.hasNextLine()) {
				s.nextLine();
				lines++;
			}
			s.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return lines;
		
	}
	private void printWormChannel(int column) {
		StringBuffer ch0s = new StringBuffer();
		for(int j = 0;j<ch0A.length;j++) {
			ch0s.append(ch0A[j][column]).append("\t");
		}
		StringBuffer ch1s = new StringBuffer();
		for(int j = 0;j<ch1A.length;j++) {
			ch1s.append(ch1A[j][column]).append("\t");
		}
		StringBuffer ch2s = new StringBuffer();
		for(int j = 0;j<ch2A.length;j++) {
			ch2s.append(ch2A[j][column]).append("\t");
		}
		StringBuffer ch3s = new StringBuffer();
		for(int j = 0;j<ch3A.length;j++) {
			ch3s.append(ch3A[j][column]).append("\t");
		}
		StringBuffer total = new StringBuffer();
		total.append(ch0s).append("\n");
		total.append(ch1s).append("\n");
		total.append(ch2s).append("\n");
		total.append(ch3s).append("\n");
		System.out.println(total.toString());
		//return total.toString();
	}
	private int[][] readChannelArray(String string, File f, int lines) {
		int[][] matrix = null;
		System.out.println("Reading readChannelArray "+string+" "+f.getName());
		try {
			BufferedReader br = new BufferedReader( 
                    new FileReader(f)); 
			boolean first = true;
			String line;
			int row = 0;
			while((line = br.readLine()) != null) {
				lines++;
			}
			br.close();
			br = new BufferedReader( 
                    new FileReader(f));
			while((line = br.readLine()) != null) {
				//System.out.println(lines);
				String[] parts = line.split("\t");
				if(first) {
					matrix = new int[lines][parts.length];
					first = false;
				}
				else {
					int index = 0;
					for(String part: parts) {
						matrix[row][index] = Integer.parseInt(part);
						index++;
					}
				}
				row++;
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//System.out.println("Done readChannelArray "+string);
		return matrix;
	}
	private void readChannel(String string, File f, int max) {
		System.out.println("Reading channel "+string);
		try {
			BufferedReader br = new BufferedReader( 
                    new FileReader(f)); 
			ArrayList<Integer> ids = new ArrayList<Integer>();
			boolean first = true;
			int lines = 0;
			boolean stop = false;
			String line;
			while((line = br.readLine()) != null) {
				//System.out.println(lines);
				String[] parts = line.split("\t");
				if(first) {
					for(String part: parts) {
						int id = Integer.parseInt(part);
						ids.add(id);
					}
					first = false;
				}
				else {
					int index = 0;
					//System.out.println(parts.length);
					for(String part: parts) {
						int value = Integer.parseInt(part);
						int id = ids.get(index);
						wl.addChannelValue(string, id, value);
						index++;
						if(max>0 && index == max) {
							//System.out.println("break");
							break;
						}
					}
					//System.out.println(lines);
				}
				lines++;
				//System.out.println(s.nextInt());
			}
			br.close();
			//System.exit(0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void readFile(int max) {
		System.out.println("Reading file "+f.getName());
		try {
			Scanner s = new Scanner(f);
			boolean first = true;
			int index = 0;
			HashMap<String, Integer> columns = new HashMap<String, Integer>();
			while(s.hasNextLine()) {
				String line = s.nextLine();
				String[] parts = line.split("\t");
				if(first) {
					first = false;
					for(int i=0;i<parts.length;i++) {
						columns.put(parts[i], i);
						//System.out.println(i+"\t"+parts[i]);
					}
					
				}
				else {
					if(parts.length>3) {
						int id = Integer.parseInt(parts[columns.get("Id")]);
						Worm w = wl.createWorm(id,f.getName());
						int tof = Integer.parseInt(parts[columns.get("TOF")]);
						double extinction = Double.parseDouble(parts[columns.get("Extinction")]);
						double red = Double.parseDouble(parts[columns.get("Red")]);
						double green = Double.parseDouble(parts[columns.get("Green")]);
						double violet = Double.parseDouble(parts[columns.get("Violet")]);
						double phgreen = Double.parseDouble(parts[columns.get("PH Green")]);
						w.setTOF(tof);
						w.setRed(red);
						w.setGreen(green);
						w.setViolet(violet);
						w.setExtinction(extinction);
						w.setPHGreen(phgreen);
						index++;
					}
					else if(line.contains("PMT voltage")) {
						String violet = s.nextLine();
						String green = s.nextLine();
						String red = s.nextLine();
						this.setPMT(violet,"Violet:");
						this.setPMT(green,"Green:");
						this.setPMT(red,"Red:");
					}
					/*
					else if(line.contains("Lasers")) {
						System.out.println(s.nextLine());
						System.out.println(s.nextLine());
						System.out.println(s.nextLine());
					}
					*/
				}
				if(max>0 && index>=max) {
					break;
				}
			}
			s.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	private void setPMT(String line, String color) {
		if(this.pmt == null) {
			pmt = line;
		}
		else {
			this.pmt+= "\t"+line;
		}
		
	}
	public void printWorms(int minTOF, int minCh4) {
		wl.printWorms(minTOF, minCh4);
	}
	public void setControl(boolean b) {
		this.control = b;
	}
	public String getWorms() {
		return wl.toString();
	}
	public boolean isControl() {
		return control;
	}
	public double[][] getData(String column1, String column2, double tofMin, double tofMax) {
		return wl.getData(column1, column2, tofMin, tofMax);
	}
	public WormList getWormList() {
		return wl;
	}
	public String getFile() {
		return f.getName();
	}
	public boolean containsWorm(Worm w) {
		return wl.contains(w);
	}
	public void printContents() {
		String content = f.getName();
		content += "\t"+wl.getWorms().size()+"\t"+pmt;
		System.out.println(content);
	}
}
