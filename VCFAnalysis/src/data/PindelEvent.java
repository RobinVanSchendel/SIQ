package data;

public class PindelEvent {
	public String chr;
	public String sample, svtype;
	public String sampleGroup;
	public int start, end;
	public static PindelEvent parsePindelEvent(String s, String header) {
		String[] headerParts = header.split("\t");
		String[] parts = s.split("\t");
		PindelEvent p = new PindelEvent();
		for(int i=0;i<headerParts.length;i++) {
			String head = headerParts[i];
			String part = parts[i];
			//System.out.println(head+"\t"+part);
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
		}
		return p;
		
	}
	public String getSVType() {
		return svtype;
	}
	public void setSVType(String svtype) {
		this.svtype = svtype;
	}
	private void setEnd(String part) {
		this.end = Integer.parseInt(part);
	}
	private void setStart(String part) {
		this.start = Integer.parseInt(part);
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
		String ret = sample+s+chr+s+start+s+end+s+svtype;
		return ret;
	}
	public int getDistance(String location) {
		int pos = PindelEvent.getPosition(location);
		if(start<=pos && pos<=end) {
			int left = start-pos;
			int right = pos-end;
			return Math.max(left,right);
		}
		int left = Math.abs(pos-start);
		int right = Math.abs(pos-end);
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
}
