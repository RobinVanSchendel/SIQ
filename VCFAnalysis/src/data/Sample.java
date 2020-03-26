package data;

import java.util.ArrayList;


public class Sample {
	private String name;
	private ArrayList<Caller> calls = new ArrayList<Caller>();
	
	public Sample(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public ArrayList<Caller> getCall() {
		return calls;
	}
	public void addCall(Caller call) {
		calls.add(call);
	}
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for(Caller call: calls) {
			//sb.append(call.getCaller());
			if(call.getGt() != null) {
				sb.append(name);
				sb.append(" ");
				sb.append(call.getGt()).append("\t");
			}
		}
		return sb.toString();
	}
}
