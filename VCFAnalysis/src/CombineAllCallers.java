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

		//Perform manta
		File mantaVCF = new File("E:\\temp\\diploidSV.vcf.gz");
		Manta manta = new Manta(mantaVCF);
		manta.parseFile(svc);
		
		//perform GRIDSS
		File vcf = new File("E:\\temp\\gridss.vcf");
		GridssCall gc = new GridssCall(vcf);
		gc.parseFile(svc);
		
		//svc.addMetaData();
		int maxSupportingSamples = 2;
		svc.printSVs(maxSupportingSamples);
		
		System.out.println("the end");

	}

}
