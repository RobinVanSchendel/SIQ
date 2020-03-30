package data;

import java.util.ArrayList;
import java.util.Collection;


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
			if(call.getGt() != null) {
				sb.append(name);
				sb.append("["+call.getCaller()+"]");
				sb.append("\t");
				//sb.append(" ");
				//sb.append(call.getGt()).append("\t");
			}
		}
		return sb.toString();
	}
	public boolean isSupported() {
		for(Caller call: calls) {
			if(call.supports()) {
				return true;
			}
		}
		return false;
	}
	public ArrayList<String> getCallers() {
		ArrayList<String> callers = new ArrayList<String>();
		for(Caller call: calls) {
			callers.add(call.getCaller());
		}
		return callers;
	}
	public boolean isSupported(String caller) {
		for(Caller call: calls) {
			if(call.getCaller().contentEquals(caller) && call.supports()) {
				return true;
			}
		}
		return false;
	}
}
