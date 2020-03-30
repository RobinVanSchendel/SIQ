package data;

import htsjdk.variant.variantcontext.Genotype;

public class MantaCaller extends Caller {

	public final static String nameCaller = "Manta";
	
	public MantaCaller(Genotype genotype) {
		super(genotype);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getCaller() {
		return nameCaller;
	}

	@Override
	public void process() {
		if(gt.isHomVar() || gt.isHet()) {
			//ok
			return;
		}
		//erase
		this.gt = null;
	}

	@Override
	public boolean supports() {
		return gt!= null;
		//return false;
	}

}
