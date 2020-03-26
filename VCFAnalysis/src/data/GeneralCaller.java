package data;

import htsjdk.variant.variantcontext.VariantContext;

public abstract class GeneralCaller {
	public abstract StructuralVariation parseStructuralVariation(VariantContext vc);
}
