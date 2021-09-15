package controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import data.PindelEvent;
import dnaanalysis.InsertionSolverTwoSides;
import dnaanalysis.RandomInsertionSolverTwoSides;
import htsjdk.samtools.reference.ReferenceSequenceFile;

public class PindelController {
	private File pindel;
	private String header;
	private ReferenceSequenceFile rsf;
	
	private List<PindelEvent> v = new ArrayList<PindelEvent>();
	
	public PindelController(File pindel) {
		this.pindel = pindel;
	}
	public void parsePindel(boolean excludeTDINV) {
		if(pindel== null) {
			return;
		}
		if(pindel.getName().endsWith(".xlsx")) {
			parsePindelExcel(excludeTDINV);
		}
		try {
			Scanner s = new Scanner(pindel);
			while(s.hasNextLine()) {
				String line = s.nextLine();
				if(!line.contains("LocationID")) {
					PindelEvent p = PindelEvent.parsePindelEvent(line);
					if(p!=null) {
						if(excludeTDINV && !p.isINV() && !p.isTD()) {
							v.add(p);
						}
						else if(!excludeTDINV) {
							v.add(p);
						}
					}
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void parsePindelExcel(boolean excludeTDINV) {
		 Workbook workbook = null;
		try {
			workbook = WorkbookFactory.create(pindel);
		} catch (EncryptedDocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        //getRawData
        Sheet rawData = workbook.getSheet("rawData");
        HashMap<String, Integer> headerLookup = new HashMap<String, Integer>();
        int counter = 0;
        for (Row row: rawData) {
        	//header
        	if(counter == 0) {
        		int colCounter = 0;
        		for(Cell cell: row) {
        			headerLookup.put(cell.toString(),colCounter);
        			colCounter++;
        		}
        	}
        	//data
        	else {
        		PindelEvent p = PindelEvent.parsePindelEvent(row, headerLookup);
        		//System.out.println(p);
        		if(excludeTDINV && !p.isINV() && !p.isTD()) {
					v.add(p);
				}
				else if(!excludeTDINV) {
					v.add(p);
				}
        	}
        	counter++;
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
		String chr = PindelEvent.getChromosome(location).replace("CHROMOSOME_", "");
		List<PindelEvent> subset = v.stream()
			.filter(event -> event.getSample().equals(sampleName))
			.filter(event -> event.getChr().equals(chr))
			.collect(Collectors.toList());
		
		return subset;
	}
	public List<PindelEvent> getAll() {
		return v;
	}
	public void checkAndSetFlankInsert(int minSize, int sizeOutDel, int sizeInDel, boolean searchForward, boolean searchReverse) {
		for(PindelEvent p: v) {
			if(p.getInsertion()!= null && p.getInsertion().length()>=minSize){
				int startLeft = p.getStart()-sizeOutDel;
				int endLeft = p.getStart()+sizeInDel;
				String left = getGenomicSequence(p.getChr(),startLeft,endLeft);
				
				int startRight = p.getEnd()-sizeInDel;
				int endRight = p.getEnd()+sizeOutDel;
				String right = getGenomicSequence(p.getChr(),startRight,endRight);
				
				InsertionSolverTwoSides is = new InsertionSolverTwoSides(left, right,p.getInsertion(),p.getSample());
				is.setAdjustedPositionLeft(-sizeInDel);		
				is.setAdjustedPositionRight(-sizeInDel);
				is.search(searchForward, searchReverse);
				//change this?
				is.setMaxTriesSolved(2);
				//set at 5
				is.setMinimumMatch(minSize-1, false);
				is.solveInsertion();
				//this.is = is;
				//now determine if this is random or not
				//one peculiar thing is if the flanks overlap it is not quite fair anymore
				if( endLeft > startRight) {
					//System.out.println(endLeft);
					//System.out.println(startRight);
					int tooLarge = endLeft-startRight;
					//System.out.println(tooLarge);
					//System.out.println(right.length());
					int cut = tooLarge/2;
					right = right.substring(cut);
					left = left.substring(0, left.length()-cut);
				}
				
				RandomInsertionSolverTwoSides ris = new RandomInsertionSolverTwoSides(left,right, p.getInsertion());
				p.setFlanksInsert(ris.isNonRandomInsert(0.9, is.getLargestMatch()));
				p.setLeftSeq(left);
				p.setRightSeq(right);
				p.setLargestMatch(is.getLargestMatch());
				p.setLargestMatchSeq(is.toString());
				//this.isFlankInsert = ris.isNonRandomInsert(0.9, is.getLargestMatch());
			}
		}
	}
	public String getGenomicSequence(String string, int startLeft, int endLeft) {
		return rsf.getSubsequenceAt(string, startLeft, endLeft).getBaseString();
	}
	public void setRSF(ReferenceSequenceFile rsf2) {
		this.rsf = rsf2;
		
	}
	public void setGenomicEvents() {
		
	}
	public void setGenomicFlanks(int size) {
		for(PindelEvent p: v) {
			int startLeft = p.getStart()-size/2;
			//always this off-by-one
			int endLeft = p.getStart()+size/2-1;
			String left = getGenomicSequence(p.getChr(),startLeft,endLeft);
			//System.out.println(left.length());
			p.setLeftGenomic(left);
			
			int startRight = p.getEnd()-size/2;
			//always this off-by-one
			int endRight = p.getEnd()+size/2-1;
			String right = getGenomicSequence(p.getChr(),startRight,endRight);
			p.setRightGenomic(right);
			//System.out.println(right.length());
		}
		
	}
	public ArrayList<String> getGroups() {
		ArrayList<String> groups = new ArrayList<String>();
		for(PindelEvent pe: v) {
			String group = pe.getSampleGroup();
			if(!groups.contains(group)) {
				groups.add(group);
			}
		}
		return groups;
	}
}
