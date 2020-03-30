package data;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import controller.SVController;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;

public abstract class GeneralCaller {
	protected File vcf;
	
	public abstract StructuralVariation parseStructuralVariation(VariantContext vc);
	public abstract void parseFile(SVController svc);
	public abstract String getName();
	
	protected Location parseEnd(Allele altAlleleWithHighestAlleleCount) {
		String alleleString = altAlleleWithHighestAlleleCount.toString();
		//System.out.println(altAlleleWithHighestAlleleCount);
		Pattern p = Pattern.compile("[\\[\\]]");
		Matcher m = p.matcher(alleleString);
		if(m.find()) {
			String start = m.group();
			int startIndex = m.start()+1;
			if(m.find()) {
				String end = m.group();
				int endIndex = m.start();
				Location loc = Location.parse(alleleString.substring(startIndex, endIndex));
				return loc;
			}
		}
		return null;
	}
	protected String obtainTypeSigns(Allele high) {
		String alleleString = high.toString();
		//System.out.println(alleleString);
		Pattern p = Pattern.compile("[\\[\\]]");
		Matcher m = p.matcher(alleleString);
		if(m.find()) {
			String start = m.group();
			if(m.find()) {
				String end = m.group();
				return start+end;
			}
		}
		return null;
	}
}
