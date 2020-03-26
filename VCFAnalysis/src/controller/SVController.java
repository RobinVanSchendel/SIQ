package controller;

import java.util.ArrayList;
import java.util.HashMap;

import data.StructuralVariation;

public class SVController {
	private HashMap<String, StructuralVariation> svs = new HashMap<String, StructuralVariation>();
	
	//only add SVs if they are not yet in
	public void addSV(StructuralVariation sv) {
		if(svs.containsKey(sv.getKey())) {
			return;
		}
		svs.put(sv.getKey(), sv);
	}
	public int countEvents() {
		return svs.size();
	}
	
	public void printSVs() {
		for(String key: svs.keySet()) {
			System.out.println(svs.get(key));
		}
	}
}
