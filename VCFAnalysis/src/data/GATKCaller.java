package data;

import java.util.ArrayList;

import htsjdk.variant.variantcontext.Genotype;

public class GATKCaller extends Caller {
	
	public static final String nameCaller = "GATK";
	
	public GATKCaller(Genotype genotype) {
		super(genotype);
		// TODO Auto-generated constructor stub
	}
	@Override
	public String getCaller() {
		return nameCaller;
	}
	@Override
	public void process() {
		if(gt.isHomVar()) {
			int dp = (Integer) gt.getAnyAttribute("DP");
			//int dp = Integer.parseInt((String) gt.getAnyAttribute("DP"));
			if(dp>=8) {
				//System.out.println("GQ "+gt.getAnyAttribute("GQ"));
				//System.out.println("DP "+gt.getAnyAttribute("DP"));
				//System.out.println(gt);
				//System.exit(0);
				//ok
				return;
			}
		}
		else if(gt.isHet()) {
			if(gt.getAnyAttribute("AD") instanceof ArrayList<?>) {
				@SuppressWarnings("unchecked")
				ArrayList<Integer> ad = (ArrayList<Integer>) gt.getAnyAttribute("AD");
				int ref = ad.get(0);
				int alt = ad.get(1);
				//is this reasonable?
				//not to find events in heterozygous samples
				if(alt>8) {
					return;
				}
			}
			//ArrayList<Integer> ad = (ArrayList<Integer>) gt.getAnyAttribute("AD");
			//System.out.println(ad);
		}
		//erase
		this.gt = null;
	}
	@Override
	public boolean supports() {
		if(gt!=null) {
			return true;
		}
		return false;
	}
	
}
