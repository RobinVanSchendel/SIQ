package controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import data.Sample;
import data.StructuralVariation;
import htsjdk.samtools.reference.ReferenceSequenceFile;

public class SVController {
	private HashMap<String, StructuralVariation> svs = new HashMap<String, StructuralVariation>();
	private ReferenceSequenceFile rsf;
	private ArrayList<String> callers = new ArrayList<String>();
	private boolean checkForLocations = false;
	private HashMap<String, String> names = new HashMap<String, String>();
	
	public SVController(ReferenceSequenceFile rsf) {
		this.rsf = rsf;
	}
	//only add SVs if they are not yet in
	public void addSV(StructuralVariation sv, String caller) {
		//change names if needed
		for(Sample s: sv.getSamples()) {
			s.setName(lookupName(s.getName()));
		}
		if(sv.getStart().getPosition()<16454679 && sv.getStart().getPosition()>16454879) {
			System.out.println(sv);
			System.exit(0);
		}
		
		
		if(!callers.contains(caller)) {
			callers.add(caller);
		}
		if(callers.size()>1) {
			checkForLocations = true;
		}
		//can also be a different caller
		if(svs.containsKey(sv.getKey())) {
			StructuralVariation svTemp = svs.get(sv.getKey());
			//only stop if the callers match
			if(svTemp.getCallers().contentEquals(caller)) {
				return;
			}
		}
		boolean inserted = insertSV(sv, caller);
		//if(sv.getStartEndLocation().contentEquals("CHROMOSOME_II:967255-1050920")) {
			//System.out.println("hier "+inserted);
			//System.exit(0);
		//}
	}
	private String getHeader() {
		String header = StructuralVariation.getHeader(callers);
		return header;
	}
	private boolean insertSV(StructuralVariation sv, String caller) {
		if(!checkForLocations) {
			//System.out.println("Adding");
			svs.put(sv.getKey(), sv);
			return true;
		}
		else {
			//improve this search later on
			//System.out.println("Checking");
			boolean added = false;
			ArrayList<StructuralVariation> closest  = new ArrayList<StructuralVariation>();
			for(String key: svs.keySet()) {
				StructuralVariation svTemp = svs.get(key);
				if(svTemp.inNeighbourhood(sv) && !svTemp.getCallers().contains(caller)) {
					closest.add(svTemp);
				}
			}
			if(closest.size()>0) {
				if(closest.size()==1) {
					closest.get(0).merge(sv);
					added = true;
					return false;
				}
				else {
					for(StructuralVariation close : closest) {
						//System.out.println(close);
						
					}
					//System.out.println("====");
					//System.out.println(sv);
					//System.out.println("======");
					//System.exit(0);
				}
			}
			if(!added) {
				svs.put(sv.getKey(), sv);
				return true;
			}
		}
		return false;
	}
	public int countEvents() {
		return svs.size();
	}
	public void addMetaData() {
		for(String key: svs.keySet()) {
			svs.get(key).addMetaData(rsf);
		}
	}
	
	public void printSVs(int maxSupportingFiles) {
		System.out.println(getHeader());
		int counter = 0;
		for(String key: svs.keySet()) {
			StructuralVariation sv = svs.get(key);
			
			if(sv.getStartEndLocation().contentEquals("CHROMOSOME_V:11138746-11138835")) {
				//System.out.println("Filter");
				//System.out.println("Filter "+sv.getNrSampleSupport());
			}
			
			int total = 0;
			int max = 0;
			String totalString = "";
			for(String caller: callers) {
				int nrFileSupports = sv.getNrSampleSupport(caller);
				if(nrFileSupports>max) {
					max = nrFileSupports;
				}
				total += nrFileSupports;
				if(totalString.length()>0) {
					totalString+="\t";
				}
				totalString+=nrFileSupports;
			}
			
			if(total>0 && max<=maxSupportingFiles) {
				sv.setCallerString(totalString);
				System.out.println(sv.toOneLineString(callers));
				counter++;
				//System.out.println(sv.getNrFileSupport());
			}
		}
		System.out.println("Printed "+counter+" events");
	}
	public void printSVs() {
		System.out.println(getHeader());
		for(String key: svs.keySet()) {
			StructuralVariation sv = svs.get(key);
			System.out.println(sv);
		}
	}
	public void addLookupNames(String string) throws FileNotFoundException {
		Scanner s = new Scanner(new File(string));
		while(s.hasNextLine()) {
			String line = s.nextLine();
			String[] parts = line.split("\t");
			names.put(parts[0],parts[1]);
		}
	}
	public String lookupName(String name) {
		if(names.containsKey(name)) {
			return names.get(name);
		}
		return name;
	}
}
