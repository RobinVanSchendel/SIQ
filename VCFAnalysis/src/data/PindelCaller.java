package data;

import htsjdk.variant.variantcontext.Genotype;

public class PindelCaller extends Caller{
	public final static String nameCaller = "Pindel";

	public PindelCaller(Genotype genotype) {
		super(genotype);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getCaller() {
		return nameCaller;
	}

	@Override
	public void process() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean supports() {
		return true;
	}

}
