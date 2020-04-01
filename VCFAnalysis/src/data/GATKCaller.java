package data;

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
			int gq = (Integer) gt.getAnyAttribute("GQ");
			int dp = (Integer) gt.getAnyAttribute("DP");
			//int dp = Integer.parseInt((String) gt.getAnyAttribute("DP"));
			if(dp>5) {
				//System.out.println("GQ "+gt.getAnyAttribute("GQ"));
				//System.out.println("DP "+gt.getAnyAttribute("DP"));
				//System.out.println(gt);
				//System.exit(0);
				//ok
				return;
			}
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
