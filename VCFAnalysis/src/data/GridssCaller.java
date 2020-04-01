package data;

import htsjdk.variant.variantcontext.Genotype;

public class GridssCaller extends Caller {
	
	public static final String nameCaller = "GRIDSS";
	
	public GridssCaller(Genotype genotype) {
		super(genotype);
		// TODO Auto-generated constructor stub
	}
	@Override
	public String getCaller() {
		return nameCaller;
	}
	@Override
	public void process() {
		String rpString = (String)gt.getExtendedAttribute("VF");
		double rp = Double.parseDouble(rpString);
		//remove if nothing is there, or VF=1
		if(rp <=1) {
			this.gt = null;
		}
		// TODO Auto-generated method stub
		
	}
	@Override
	public boolean supports() {
		if(gt!=null) {
			return true;
		}
		return false;
	}
	
}
