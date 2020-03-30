import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import controller.PindelController;
import data.PindelEvent;
import htsjdk.samtools.reference.ReferenceSequenceFile;
import htsjdk.samtools.reference.ReferenceSequenceFileFactory;

public class PindelTLSEvents {

	public static void main(String[] args) throws EncryptedDocumentException, IOException {
		// Creating a Workbook from an Excel file (.xls or .xlsx)
		File PindelTLS = new File("C:\\Users\\rvanschendel\\Dropbox\\4TLS_Paper\\NGS data\\20160614_project_allTLS_pindel_MA.xlsx");
		File genomeFile = new File("E:\\genomes\\caenorhabditis_elegans\\c_elegans.WS235.genomic.fa");
		ReferenceSequenceFile rsf = ReferenceSequenceFileFactory.getReferenceSequenceFile(genomeFile);
		PindelController pc = new PindelController(PindelTLS);
		pc.setRSF(rsf);
		System.out.println("Starting parsing Pindel events");
		pc.parsePindel(true);
		System.out.println("Done Parsing "+pc.getAll().size()+" Pindel events");
		pc.setGenomicFlanks(200);
		//now find flankinserts
		boolean searchForward = true;
		boolean searchReverse = true;
		int sizeOutDel = 30;
		int sizeInDel = 10;
		for(int i = 0;i<1;i++) {
			pc.checkAndSetFlankInsert(5, sizeOutDel, sizeInDel, searchForward, searchReverse);
			
			ArrayList<PindelEvent> all = (ArrayList<PindelEvent>) pc.getAll();
			int counter = 0;
			ArrayList<String> groups = pc.getGroups();
			for(String group: groups) {
				File f = new File(group+".txt");
				BufferedWriter writer = new BufferedWriter(new FileWriter(f));
				for(PindelEvent p: all) {
					if(p.getSampleGroup().contentEquals(group) && ( p.getSVTypeOrig().contentEquals("DEL") || p.getSVTypeOrig().contentEquals("DELINS") ) ) {
						writer.write(p.getGenomeLeft()+"\t"+p.getGenomeRight()+"\t"+group+"\n");
						//System.out.println(group);
					}
				}
				writer.close();
				CheckPatternTrio.findAndPrintTrios(f);
			}
			/*
			for(PindelEvent p: all) {
				System.out.println(p);
				if(p.isFlankInsert()) {
					counter++;
				}
			}
			*/
			//System.out.println(counter);
		}
	}

}
