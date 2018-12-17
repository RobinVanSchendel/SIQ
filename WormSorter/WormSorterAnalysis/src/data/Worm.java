package data;

import java.util.ArrayList;
import java.util.HashMap;

public class Worm {
	private int id;
	private double tof, green, red, violet, extinction;
	private String file;
	private HashMap<String, Channel> channels = new HashMap<String, Channel>();
	private double phgreen;
	private double phred;
	private double phext, phviolet;
	public Worm(int id, String file) {
		this.id = id;
		this.file = file;
	}
	public int getId() {
		return id;
	}
	public void addChannel(String string, int value) {
		if(!channels.containsKey(string)) {
			Channel c = new Channel(string);
			channels.put(string, c);
		}
		getChannel(string).addValue(value);
	}
	public Channel getChannel(String string) {
		return channels.get(string);
	}
	public String getChannelsString() {
		StringBuffer sb = new StringBuffer();
		sb.append(channels.get("ch0")).append("\n");
		sb.append(channels.get("ch1")).append("\n");
		sb.append(channels.get("ch2")).append("\n");
		sb.append(channels.get("ch3")).append("\n");
		//sb.append(channels.get("ch3").getStats()).append("\n");
		return sb.toString();
	}
	public void setTOF(double tof) {
		this.tof = tof;
	}
	public double getTOF() {
		return tof;
	}
	public double getGreen() {
		return green;
	}
	public void setGreen(double green) {
		this.green = green;
	}
	public double getRed() {
		return red;
	}
	public void setRed(double red) {
		this.red = red;
	}
	public double getViolet() {
		return violet;
	}
	public void setViolet(double violet) {
		this.violet = violet;
	}
	public double getData(String name) {
		if(name.equals("Red")) {
			return red;
		}
		else if(name.equals("Violet")) {
			return violet;
		}
		else if(name.equals("Green")) {
			return green;
		}
		else if(name.equals("TOF")) {
			return tof;
		}
		else if(name.equals("Extinction")) {
			return this.extinction;
		}
		else if(name.equals("PH Red")) {
			return this.phred;
		}
		else if(name.equals("PH Green")) {
			return this.phgreen;
		}
		System.err.println("I don't know: "+name);
		System.exit(0);
		return -1.0;
	}
	public static String getHeader() {
		return "file\tid\ttof\textinction\tviolet\tgreen\tred\tphgreen\tphred\tratioPHRedGreen\tOverlap";
	}
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(file).append("\t");
		sb.append(id).append("\t");
		sb.append(tof).append("\t");
		sb.append(extinction).append("\t");
		sb.append(violet).append("\t");
		sb.append(green).append("\t");
		sb.append(red).append("\t");
		sb.append(phgreen).append("\t");
		sb.append(phred).append("\t");
		sb.append(phgreen/(double)phred).append("\t");
		sb.append(this.doPeaksOverlap("ch0","ch2", "ch3", 200)).append("\t");
		return sb.toString();
	}
	public double getExtinction() {
		return extinction;
	}
	public void setExtinction(double extinction) {
		this.extinction = extinction;
	}
	public String getFile() {
		// TODO Auto-generated method stub
		return file;
	}
	public void setPHGreen(double phgreen) {
		this.phgreen = phgreen;
		
	}
	public void cleanChannels() {
		Channel ch0 = channels.get("ch0");
		ArrayList<Integer> list = ch0.getValues();
		int finalIndex = list.size();
		for(int lastIndex = list.size()-1;lastIndex>0;lastIndex-- ){
			if(list.get(lastIndex)>0){
				finalIndex = lastIndex+1;
				break;
			}
		}
		if(finalIndex>list.size()-1){
			finalIndex = list.size()-1;
		}
		for(String key: channels.keySet()){
			channels.get(key).removeFrom(finalIndex);
		}
		// TODO Auto-generated method stub
	}
	public void setPHRed(double phred) {
		this.phred = phred;
	}
	public String getKey(){
		return this.getFile()+"_"+this.getId();
	}
	public void setPHExt(double phext) {
		this.phext = phext;
	}
	public void checkWorm() {
		int maxCCh0 = channels.get("ch0").getHighest();
		int maxCCh1 = channels.get("ch1").getHighest();
		int maxCCh2 = channels.get("ch2").getHighest();
		int maxCCh3 = channels.get("ch3").getHighest();
		if(maxCCh0 != phext){
			System.err.println(file+"\tmaxCCh0 != phext ["+maxCCh0+"]["+phext+"]"+id);
		}
		if(maxCCh1 != phviolet){
			System.err.println(file+"\tmaxCCh1 != phviolet ["+maxCCh1+"]["+phviolet+"]"+id);
		}
		if(maxCCh2 != phgreen){
			System.err.println(file+"\tmaxCCh2 != phgreen ["+maxCCh2+"]["+phgreen+"]"+id);
		}
		if(maxCCh3 != phred){
			System.err.println(file+"\tmaxCCh3 != phred ["+maxCCh3+"]["+phred+"]"+id);
		}
		
	}
	public void setPHViolet(double phv) {
		this.phviolet = phv;
	}
	public String doPeaksOverlap(String ch0, String ch1, String ch2, int minDist){
		if(channels.size()>0){
			ArrayList<XY> peaksCh0 = channels.get(ch0).findPeaks(minDist);
			ArrayList<XY> peaksCh1 = channels.get(ch1).findPeaks(minDist);
			ArrayList<XY> peaksCh2 = channels.get(ch2).findPeaks(minDist);
			//Remove joined peaks of Ch0 and Ch2
			ArrayList<XY> temp = new ArrayList<XY>();
			for(XY xy: peaksCh1){
				for(XY ex: peaksCh0){
					if(xy.hasOverlap(ex, minDist)){
						temp.add(xy);
					}
				}
			}
			peaksCh1.removeAll(temp);
			//now check for overlap with
			String overlap = "";
			for(XY xy: peaksCh1){
				if(overlap.length()>0){
					overlap+="|";
				}
				overlap+=xy.getX()+":";
				for(XY XYCh2: peaksCh2){
					if(xy.hasOverlap(XYCh2, minDist)){
						String tempStr = xy.getOverlap(XYCh2, minDist);
						overlap+=" "+tempStr;
					}
				}
			}
			return overlap;
		}
		return null;
	} 
}
