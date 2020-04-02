import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import controller.SVController;
import data.GeneralCaller;
import data.Location;
import data.MantaCaller;
import data.PindelCaller;
import data.PindelEvent;
import data.Sample;
import data.StructuralVariation;
import data.StructuralVariation.SVType;
import htsjdk.variant.variantcontext.VariantContext;

public class PindelCall extends GeneralCaller{

	public PindelCall(File pindelFile) {
		this.vcf = pindelFile;
	}

	@Override
	public StructuralVariation parseStructuralVariation(VariantContext vc) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void parseFile(SVController svc) {
		if(vcf.getName().endsWith(".xlsx")) {
			parseFileExcel(svc);
		}
		else {
			System.err.println("Not implemented yet");
		}
	}

	private int parseFileExcel(SVController svc) {
		 Workbook workbook = null;
			try {
				workbook = WorkbookFactory.create(this.vcf);
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
	        		String chr = row.getCell(headerLookup.get("Chr")).toString();
	        		String startPos = row.getCell(headerLookup.get("Start")).toString();
	        		String endPos = row.getCell(headerLookup.get("End")).toString();
	        		double startD = Double.parseDouble(startPos);
	        		double endD = Double.parseDouble(endPos);
	        		Location start = new Location(chr, (int) startD);
	        		Location end = new Location(chr, (int) endD);
	        		String type = row.getCell(headerLookup.get("SVType")).toString();
	        		SVType svType = getSVType(type);
	        		String insert = row.getCell(headerLookup.get("InsertInDeletion")).toString();
	        		StructuralVariation sv = new StructuralVariation(svType, start, end, PindelCaller.nameCaller);
	        		if(!insert.contentEquals("NA")) {
	        			sv.setInsert(insert);
	        		}
	        		String sample = row.getCell(headerLookup.get("Sample")).toString();
	        		Sample s = new Sample(sample);
	        		PindelCaller m = new PindelCaller(null);
	    			m.process();
	    			s.addCall(m);
	    			//s.setGt(vc.getGenotype(name));
	    			sv.addSample(s);
	        		//System.out.println(sv.toOneLineString(null));
	        		//PindelEvent p = PindelEvent.parsePindelEvent(row, headerLookup);
	        		//System.out.println(p);
	    			svc.addSV(sv);
	        	}
	        	counter++;
	        }
	        try {
				workbook.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        return counter;
	}

	private SVType getSVType(String type) {
		switch(type) {
		case "DEL":
			return SVType.DEL;
		case "DELINS":
			return SVType.DELINS;
		case "SINS":
			return SVType.SINS;
		case "TD":
			return SVType.TD;
		case "TDINS":
			return SVType.TDINS;
		case "INV":
			return SVType.INV;
		default:
			System.err.println("I don't have a type for: "+type);
		}
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

}
