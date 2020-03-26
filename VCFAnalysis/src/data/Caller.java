package data;

import htsjdk.variant.variantcontext.Genotype;

public abstract class Caller {
	protected Genotype gt;
	public Caller(Genotype genotype) {
		this.gt = genotype;
	}
	public Genotype getGt() {
		return gt;
	}
	public void setGt(Genotype gt) {
		this.gt = gt;
	}
	public abstract String getCaller();
	
	public abstract void process();
}
