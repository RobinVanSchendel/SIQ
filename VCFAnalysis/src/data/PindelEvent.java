package data;

import java.io.File;
import java.util.HashMap;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

public class PindelEvent {
	public String chr;
	public String sample, svtype;
	public String sampleGroup;
	public String insertion = "";
	public int start, end;
	private boolean isFlankInsert = false;
	private String leftSeq;
	private String rightSeq;
	private int largestMatch;
	private String lms;
	private String leftGenomic;
	private String rightGenomic;
	public static PindelEvent parsePindelEvent(String s) {
		String[] parts = s.split("\t");
		PindelEvent p = new PindelEvent();
		
		//only the TRUE events
		if(!parts[2].startsWith("TRUE")) {
			return null;
		}
		//System.out.println(head+"\t"+part);
		//System.out.println(s);
		p.setSample(parts[1]);
		String location = parts[0].split("_")[0];
		String type = parts[0].split("_")[1];
		p.setSVType(type);
		p.setLocation(location);
		//System.out.println("Created "+p);
		return p;
		
	}
	private void setLocation(String location) {
		String[] parts = location.split(":");
		this.chr = parts[0];
		String[] parts2 = parts[1].split("-");
		this.start = Integer.parseInt(parts2[0]);
		this.end = Integer.parseInt(parts2[1]);
	}
	public String getSVTypeOrig() {
		return svtype;
	}
	public String getSVType() {
		return svtype+" - "+this.isFlankInsert;
	}
	public void setSVType(String svtype) {
		this.svtype = svtype;
	}
	private void setEnd(String part) {
		double end = Double.parseDouble(part);
		this.end = (int) end;
	}
	private void setStart(String part) {
		double start = Double.parseDouble(part);
		this.start = (int) start;
	}
	public String getChr() {
		return chr;
	}
	public void setChr(String chr) {
		this.chr = chr;
	}
	public String getSample() {
		return sample;
	}
	public void setSample(String sample) {
		this.sample = sample;
	}
	public String getSampleGroup() {
		return sampleGroup;
	}
	public void setSampleGroup(String sampleGroup) {
		this.sampleGroup = sampleGroup;
	}
	public String toString() {
		String s = "\t";
		String ret = sample+s+this.leftGenomic+s+this.rightGenomic+s+sampleGroup+s+chr+s+start+s+end+s+getSVType()+s+insertion+s+insertion.length()+s+isFlankInsert+s+leftSeq+s+rightSeq+s+largestMatch+s+lms;
		return ret;
	}
	public int getDistance(String location) {
		int pos = PindelEvent.getPosition(location);
		if(start<=pos && pos<=end) {
			int left = (int) (start-pos);
			int right = (int) (pos-end);
			return Math.max(left,right);
		}
		int left = (int) Math.abs(pos-start);
		int right = (int) Math.abs(pos-end);
		return Math.min(left, right);
	}
	public static String getChromosome(String location) {
		return location.split(":")[0];
	}
	public static int getPosition(String location) {
		String pos = location.split(":")[1];
		String posStart = pos.split("-")[0];
		return Integer.parseInt(posStart);
	}
	public boolean isINV() {
		return svtype.equals("INV");
	}
	public boolean isTD() {
		return svtype.equals("TD") || svtype.equals("TDINS");
	}
	public static PindelEvent parsePindelEvent(Row row, HashMap<String, Integer> headerLookup) {
		PindelEvent p = new PindelEvent();
		for(String key: headerLookup.keySet()) {
			//System.out.println(key);
		}
		p.setSample(row.getCell(headerLookup.get("Sample")).toString());
		p.setSampleGroup(row.getCell(headerLookup.get("SampleGroup")).toString());
		p.setSVType(row.getCell(headerLookup.get("SVType")).toString());
		p.setChr(row.getCell(headerLookup.get("Chr")).toString());
		p.setStart(row.getCell(headerLookup.get("Start")).toString());
		p.setEnd(row.getCell(headerLookup.get("End")).toString());
		Cell insert = row.getCell(headerLookup.get("InsertInDeletion"));
		if(insert!=null) {
			p.setInsert(insert.toString());
		}
		return p;
		/*
		if(head.equals("Sample")) {
			p.setSample(part);
		}
		else if(head.equals("Chr")) {
			p.setChr(part);
		}
		else if(head.equals("Start")) {
			p.setStart(part);
		}
		else if(head.equals("End")) {
			p.setEnd(part);
		}
		else if(head.equals("SVType")) {
			p.setSVType(part);
		}
		*/
	}
	
	private void setInsert(String string) {
		this.insertion = string;
		
	}
	public String getInsertion() {
		return insertion;
	}
	public int getStart() {
		return start;
	}
	public int getEnd() {
		return end;
	}
	public void setFlanksInsert(boolean nonRandomInsert) {
		this.isFlankInsert = nonRandomInsert;
	}
	public void setLeftSeq(String left) {
		leftSeq = left;
	}
	public void setRightSeq(String right) {
		rightSeq = right;
	}
	public void setLargestMatch(int largestMatch) {
		this.largestMatch = largestMatch;
		
	}
	public void setLargestMatchSeq(String string) {
		this.lms = string;
	}
	public boolean isFlankInsert() {
		return this.isFlankInsert;
	}
	public void setLeftGenomic(String left) {
		this.leftGenomic = left;
	}
	public void setRightGenomic(String right) {
		this.rightGenomic = right;
	}
	public String getGenomeLeft() {
		return leftGenomic;
	}
	public String getGenomeRight() {
		return rightGenomic;
	}
}
