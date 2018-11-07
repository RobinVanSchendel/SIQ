package data;

import java.util.HashMap;

public class Worm {
	private int id;
	private double tof, green, red, violet, extinction;
	private String file;
	private HashMap<String, Channel> channels = new HashMap<String, Channel>();
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
		//sb.append(channels.get("ch0")).append("\n");
		//sb.append(channels.get("ch1")).append("\n");
		//sb.append(channels.get("ch2")).append("\n");
		//sb.append(channels.get("ch3")).append("\n");
		sb.append(channels.get("ch3").getStats()).append("\n");
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
		System.err.println("I don't know: "+name);
		return -1.0;
	}
	public static String getHeader() {
		return "id\ttof\textinction\tviolet\tgreen\tred";
	}
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("file:"+file).append("\t");
		sb.append("id:"+id).append("\t");
		sb.append("tof:"+tof).append("\t");
		sb.append("e:"+extinction).append("\t");
		sb.append("v:"+violet).append("\t");
		sb.append("g:"+green).append("\t");
		sb.append("r"+red).append("\t");
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
}
