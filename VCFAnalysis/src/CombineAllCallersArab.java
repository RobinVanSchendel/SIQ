import java.io.File;
import java.io.FileNotFoundException;

import controller.SVController;
import htsjdk.samtools.reference.ReferenceSequenceFile;
import htsjdk.samtools.reference.ReferenceSequenceFileFactory;

public class CombineAllCallersArab {

	public static void main(String[] args) {
		File genomeFile = new File("E:\\genomes\\arabidopsis\\Arabidopsis_thaliana.TAIR10.28.dna.genome.fa");
		ReferenceSequenceFile rsf = ReferenceSequenceFileFactory.getReferenceSequenceFile(genomeFile);
		
		int maxSupportingSamples = 1;
		int debugLocation = -1;
		
		
		SVController svc = new SVController(rsf, maxSupportingSamples);
		svc.setDebugLocation(debugLocation);
		/*
		try {
			svc.addLookupNames("mappingNames.txt");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		boolean debug = true;
		
		//perform Pindel
		File pindelFile = new File("z:\\Datasets - NGS, UV_TMP, MMP\\NGS\\bams\\arabidopsis\\pindel_Raw\\20200407_teb_tonsoku_atbrca1_atbard1_hits.xlsx");
		PindelCall pindel = new PindelCall(pindelFile);
		if(debug) {
			d("Parsing Pindel");
		}
		pindel.parseFile(svc);
		if(debug) {
			d("Parsed Pindel "+svc.countEvents());
		}
		

		//Perform manta
		File mantaVCF = new File("z:\\Datasets - NGS, UV_TMP, MMP\\NGS\\bams\\arabidopsis\\Manta\\diploidSV.vcf");
		Manta manta = new Manta(mantaVCF);
		if(debug) {
			d("Parsing Manta");
		}
		manta.parseFile(svc);
		if(debug) {
			d("Parsed Manta "+svc.countEvents());
		}
		
		//perform GRIDSS
		//GRIDSS crashed on the files
		File vcf = new File("E:\\temp\\arab\\gridss.vcf");
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
		File GATKvcf = new File("Z:\\\\Datasets - NGS, UV_TMP, MMP\\\\Next Sequence Run\\\\Analysis\\\\20200403_gvcf_LUMC-003-001_arab_filtered.vcf");
		GATKCall gatkCall = new GATKCall(GATKvcf);
		if(debug) {
			d("Parsing GATK");
		}
		gatkCall.parseFile(svc);
		if(debug) {
			d("Parsed GATK "+svc.countEvents());
		}
		
		svc.addMetaData();
		svc.printSVs(maxSupportingSamples);
		//File locs = new File("project_brc-1_correct.txt");
		//svc.printLocations(locs);
		
		System.out.println("the end");

	}

	private static void d(String string) {
		System.out.println(string);
	}

}
