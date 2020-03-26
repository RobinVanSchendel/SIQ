package controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import data.PindelEvent;

public class PindelController {
	File pindel;
	String header;
	List<PindelEvent> v = new ArrayList<PindelEvent>();
	
	public PindelController(File pindel) {
		this.pindel = pindel;
	}
	public void parsePindel(boolean excludeTDINV) {
		if(pindel== null) {
			return;
		}
		try {
			Scanner s = new Scanner(pindel);
			while(s.hasNextLine()) {
				String line = s.nextLine();
				if(header == null) {
					header = line;
				}
				else {
					PindelEvent p = PindelEvent.parsePindelEvent(line, header);
					if(excludeTDINV && !p.isINV() && !p.isTD()) {
						v.add(p);
					}
					else if(!excludeTDINV) {
						v.add(p);
					}
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public int getDistance(String sampleName, String location) {
		List<PindelEvent> subVector = getSubVector(sampleName, location);
		int minDistance = Integer.MAX_VALUE;
		for(PindelEvent pe: subVector) {
			int distance = pe.getDistance(location);
			if(distance < minDistance) {
				minDistance = distance;
			}
		}
		return minDistance;
	}
	public boolean hasOverlap(String sampleName, String location) {
		return getDistance(sampleName, location) == 0;
	}
	private List<PindelEvent> getSubVector(String sampleName, String location) {
		String chr = PindelEvent.getChromosome(location);
		List<PindelEvent> subset = v.stream()
			.filter(event -> event.getSample().equals(sampleName))
			.filter(event -> event.getChr().equals(chr))
			.collect(Collectors.toList());
		
		return subset;
	}
	
}
