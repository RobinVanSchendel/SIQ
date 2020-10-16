package main;

import org.jcvi.jillion.core.qual.QualitySequenceBuilder;
import org.jcvi.jillion.core.residue.nt.NucleotideSequenceBuilder;
import org.jcvi.jillion.trace.fastq.FastqRecord;
import org.jcvi.jillion.trace.fastq.FastqRecordBuilder;

public class TriplePrimer {
	private String name, r1b, r1p, r2b, r2p;
	public TriplePrimer(String name, String r1Barcode, String r1Primer, String r2Barcode, String r2Primer) {
		this.name= name;
		this.r1b = r1Barcode;
		this.r1p = r1Primer;
		this.r2b = r2Barcode;
		this.r2p = r2Primer;
		
	}
	public String getName() {
		return name;
	}
	public String getR1b() {
		return r1b;
	}
	public String getR1p() {
		return r1p;
	}
	public String getR2b() {
		return r2b;
	}
	public String getR2p() {
		return r2p;
	}
	public String getCombiBarcode() {
		return this.r1b+"_"+this.r2b;
	}
	public boolean matches(FastqRecord fqR1, FastqRecord fqR2) {
		if(fqR1.getNucleotideSequence().toString().startsWith(r1b+r1p)) {
			if(fqR2.getNucleotideSequence().toString().startsWith(r2b+r2p)) {
				return true;
			}
		}
		return false;
	}
	public FastqRecord stripBarcode1(FastqRecord fqR1) {
		String seq = fqR1.getNucleotideSequence().toString();
		int index = seq.indexOf(r1p);
		QualitySequenceBuilder qsb = new QualitySequenceBuilder();
		NucleotideSequenceBuilder nsb = new NucleotideSequenceBuilder();
		for(int i = index;i<seq.length();i++) {
			qsb.append(fqR1.getQualitySequence().get(i));
			nsb.append(fqR1.getNucleotideSequence().get(i));
		}
		FastqRecord fqR1sub = FastqRecordBuilder.create(fqR1.getId(), nsb.build(), qsb.build()).build();
		return fqR1sub;
	}
	public FastqRecord stripBarcode2(FastqRecord fqR2) {
		String seq = fqR2.getNucleotideSequence().toString();
		int index = seq.indexOf(r2p);
		QualitySequenceBuilder qsb = new QualitySequenceBuilder();
		NucleotideSequenceBuilder nsb = new NucleotideSequenceBuilder();
		for(int i = index;i<seq.length();i++) {
			qsb.append(fqR2.getQualitySequence().get(i));
			nsb.append(fqR2.getNucleotideSequence().get(i));
		}
		FastqRecord fqR2sub = FastqRecordBuilder.create(fqR2.getId(), nsb.build(), qsb.build()).build();
		return fqR2sub;
	}

}
