import java.io.File;
import java.io.FileNotFoundException;

import controller.G4Controller;
import controller.SVController;
import htsjdk.samtools.reference.ReferenceSequenceFile;
import htsjdk.samtools.reference.ReferenceSequenceFileFactory;

public class CombineAllCallers {

	public static void main(String[] args) {
		File genomeFile = new File("E:\\genomes\\caenorhabditis_elegans\\c_elegans.WS235.genomic.fa");
		ReferenceSequenceFile rsf = ReferenceSequenceFileFactory.getReferenceSequenceFile(genomeFile);
		
		boolean searchG4 = true;
		G4Controller g4s = null;
		
		if(searchG4) {
			g4s = new G4Controller();
			g4s.fillHash(rsf);
		}
		
		int maxSupportingSamples = 1;
		int debugLocation = 12048395;
		
		SVController svc = new SVController(rsf, maxSupportingSamples);
		//svc.setDebugLocation(debugLocation);
		try {
			svc.addLookupNames("mappingNames.txt");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		boolean debug = true;
		
		//perform Pindel
		//File pindelFile = new File("E:\\temp\\20200320_Pindel_Brc-1.xlsx");
		File pindelFile = new File("Z:\\Datasets - NGS, UV_TMP, MMP\\Primase_Paper_Robin\\20191014_Project_Primase_For_Paper_TRUE.xlsx");
		PindelCall pindel = new PindelCall(pindelFile);
		if(debug) {
			d("Parsing Pindel");
		}
		pindel.parseFile(svc);
		if(debug) {
			d("Parsed Pindel "+svc.countEvents());
		}
		

		//Perform manta
		//File mantaVCF = new File("E:\\temp\\diploidSV.vcf.gz");
		File mantaVCF = new File("E:\\temp\\Primase\\diploidSV.vcf.gz");
		System.out.println("Manta file exists? "+mantaVCF.exists());
		Manta manta = new Manta(mantaVCF);
		if(debug) {
			d("Parsing Manta");
		}
		manta.parseFile(svc);
		if(debug) {
			d("Parsed Manta "+svc.countEvents());
		}
		
		//perform GRIDSS
		//File vcf = new File("E:\\temp\\gridss.vcf");
		File vcf = new File("E:\\temp\\Primase\\gridss.vcf");;
		GridssCall gc = new GridssCall(vcf);
		//System.out.println("Starting parse");
		if(debug) {
			d("Parsing Gridss");
		}
		gc.parseFile(svc);
		if(debug) {
			d("Parsed Gridss "+svc.countEvents());
		}
		//System.out.println("Ended parse");
		//File GATKvcf = new File("Z:\\Datasets - NGS, UV_TMP, MMP\\NGS\\MA lines - BRC-1 POLQ-1 analysis\\20190313_gvcf_brc-1_project.genotyped.vcf");
		File GATKvcf = new File("Z:\\Datasets - NGS, UV_TMP, MMP\\Next Sequence Run\\Analysis\\createAndCombineGVCF_Project_Primase.vcf");
		//File GATKvcf = null;
		GATKCall gatkCall = new GATKCall(GATKvcf);
		if(debug) {
			d("Parsing GATK");
		}
		gatkCall.parseFile(svc);
		if(debug) {
			d("Parsed GATK "+svc.countEvents());
		}
		
		svc.setG4Controller(g4s);
		svc.addMetaData();
		svc.printSVs(maxSupportingSamples);
		//File locs = new File("project_primase_combined.txt");
		//svc.printLocations(locs);
		
		System.out.println("the end");

	}

	private static void d(String string) {
		System.out.println(string);
	}

}
