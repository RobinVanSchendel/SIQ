import java.io.File;

import controller.SVController;
import data.GATKCaller;
import data.GeneralCaller;
import data.Location;
import data.MantaCaller;
import data.Sample;
import data.StructuralVariation;
import data.StructuralVariation.SVType;
import htsjdk.samtools.util.CloseableIterator;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;

public class GATKCall extends GeneralCaller{

	public GATKCall(File gATKvcf) {
		this.vcf = gATKvcf;
	}

	@Override
	public StructuralVariation parseStructuralVariation(VariantContext vc) {
		if(vc.isSNP()) {
			return null;
		}
		if(vc.getStart()>(2335976-100) && vc.getStart()<(2335976+100)) {
			System.out.println(vc);
			for(Genotype gt: vc.getGenotypes()) {
				System.out.println(gt);
				System.out.println(gt.isHomVar());
				System.out.println(gt.isHet());
				System.out.println(gt.isHomRef());
				System.out.println(gt.getAnyAttribute("AD"));
			}
			//System.exit(0);
		}
		Location start = new Location(vc.getContig(),vc.getStart());
		int alleles = vc.getAlleles().size();
		//for now only 2 alleles are supported
		if(alleles == 2) {
			String del = getDel(vc.getReference());
			String insert = getInsert(vc.getAltAlleleWithHighestAlleleCount());
			//System.out.println("===");
			//System.out.println(vc);
	   		//System.out.println(start);
	   		//System.out.println(vc.getType());
	   		//System.out.println(alleles);
	   		//System.out.println(del);
	   		//System.out.println(insert);
	   		SVType type = getType(del,insert);
	   		//System.out.println(type+"\t"+del+"\t"+insert);
	   		int endPos = -1;
	   		if(del==null) {
	   			endPos = vc.getStart()+1;
	   		}
	   		else {
	   			//+1 otherwise it doesn't work
	   			//seems to be correct as well
	   			endPos = vc.getStart()+del.length()+1; 
	   		}
	   		Location end = new Location(vc.getContig(), endPos);
	   		StructuralVariation sv = new StructuralVariation(type, start, end, getName());
	   		sv.setInsert(insert);
	   		//System.out.println(sv.toOneLineString(null));
	   		//System.out.println("====");
	   		//System.out.println(vc);
	   		//add Sample info
	   		for(String name: vc.getSampleNamesOrderedByName()) {
	    		//System.out.println(name);
				Sample s = new Sample(name);
				GATKCaller m = new GATKCaller(vc.getGenotype(name));
				m.process();
				s.addCall(m);
				//s.setGt(vc.getGenotype(name));
				sv.addSample(s);
				
				//System.out.println(vc.getGenotype(name));
			}
	   		/*
	   		if(vc.getStart()>(4031236-100) && vc.getStart()<(4031236+100)) {
				System.out.println(sv.toOneLineString());
				System.out.println(sv.getNrSampleSupport());
				
				System.exit(0);
			}
			*/
	   		//System.exit(0);
	   		/*
	   		if(sv.getStartEndLocation().contentEquals("CHROMOSOME_III:1330238-1330274")) {
	   			System.out.println(vc);
	   			System.out.println(sv.toOneLineString(null));
	   			for(Genotype gt: vc.getGenotypes()) {
	   				System.out.println(gt);
	   				System.out.println(gt.isHomVar());
	   				System.out.println(gt.isHet());
	   			}
	   			//System.exit(0);
	   		}
	   		*/
	   		
	   		if(sv.getNrSampleSupport()==1) {
	   			//System.out.println(vc);
	   			for(Genotype gt: vc.getGenotypes()) {
	   				//System.out.println(gt);
	   			}
	   		}
	   		return sv;
		}
		return null;
	}

	private SVType getType(String del, String insert) {
		if(del == null && insert == null) {
			System.err.println("euuhhh both del and insert null!!");
		}
		else if(del!= null && insert!=null) {
			return SVType.DELINS;
		}
		else if(del!=null && insert==null) {
			return SVType.DEL;
		}
		else if(del==null && insert!=null) {
			return SVType.SINS;
		}
		return null;
	}

	private String getInsert(Allele al) {
		String base = al.getBaseString();
		if(base!=null&& base.length()>1) {
			return base.substring(1);
		}
		return null;
	}

	private String getDel(Allele reference) {
		String base = reference.getBaseString();
		if(base!=null&& base.length()>1) {
			return base.substring(1);
		}
		return null;
	}

	@Override
	public void parseFile(SVController svc) {
		if(vcf==null) {
			return;
		}
		VCFFileReader reader = new VCFFileReader(vcf, false);
        CloseableIterator<VariantContext> it = reader.iterator();
        int counter = 0;
        int added = 0;
        while(it.hasNext()){
        	VariantContext vc = it.next();
        	StructuralVariation sv = this.parseStructuralVariation(vc);
        	if(sv!=null) {
        		svc.addSV(sv);
        		//add call details here
        		added++;
        	}
        	counter++;
        }
        System.out.println("Added "+added);
        reader.close();
	}

	@Override
	public String getName() {
		return GATKCaller.nameCaller;
	}

}
