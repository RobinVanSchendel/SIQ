import java.io.File;
import java.io.FileNotFoundException;

import controller.SVController;
import htsjdk.samtools.reference.ReferenceSequenceFile;
import htsjdk.samtools.reference.ReferenceSequenceFileFactory;

public class CombineAllCallers {

	public static void main(String[] args) {
		File genomeFile = new File("E:\\genomes\\caenorhabditis_elegans\\c_elegans.WS235.genomic.fa");
		ReferenceSequenceFile rsf = ReferenceSequenceFileFactory.getReferenceSequenceFile(genomeFile);
		SVController svc = new SVController(rsf);
		try {
			svc.addLookupNames("mappingNames.txt");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//perform Pindel
		File pindelFile = new File("E:\\temp\\20200320_Pindel_Brc-1.xlsx");
		PindelCall pindel = new PindelCall(pindelFile);
		//pindel.parseFile(svc);
		

		//Perform manta
		File mantaVCF = new File("E:\\temp\\diploidSV.vcf.gz");
		Manta manta = new Manta(mantaVCF);
		//manta.parseFile(svc);
		
		//perform GRIDSS
		File vcf = new File("E:\\temp\\gridss.vcf");
		GridssCall gc = new GridssCall(vcf);
		//System.out.println("Starting parse");
		//gc.parseFile(svc);
		//System.out.println("Ended parse");
		File GATKvcf = new File("Z:\\Datasets - NGS, UV_TMP, MMP\\NGS\\MA lines - BRC-1 POLQ-1 analysis\\20190313_gvcf_brc-1_project.genotyped.vcf");
		GATKCall gatkCall = new GATKCall(GATKvcf);
		gatkCall.parseFile(svc);
		
		svc.addMetaData();
		int maxSupportingSamples = 1;
		svc.printSVs(maxSupportingSamples);
		
		System.out.println("the end");

	}

}
