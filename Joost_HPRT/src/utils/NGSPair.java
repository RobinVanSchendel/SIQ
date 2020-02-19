package utils;

import java.io.File;
import java.util.Vector;

public class NGSPair {
	private File R1, R2;
	
	public NGSPair(File R1, File R2) {
		this.R1 = R1;
		this.R2 = R2;
	}

	public static Vector<NGSPair> obtainPairs(Vector<File> v) {
		Vector<File> done = new Vector<File>();
		Vector<NGSPair> pairs = new Vector<NGSPair>();
		for(File f: v) {
			if(done.contains(f)) {
				continue;
			}
			if(f.getName().contains("R1.fastq")) {
				File R1 = f;
				String R2name = R1.getName().replace("R1.fastq", "R2.fastq");
				File R2 = new File(R1.getAbsolutePath().replace(R1.getName(), R2name));
				//search for R2
				for(File tempFile: v) {
					if(done.contains(tempFile)) {
						continue;
					}
					if(tempFile.getAbsolutePath().equals(R2.getAbsolutePath())) {
						NGSPair ngspair = new NGSPair(R1, tempFile);
						pairs.add(ngspair);
						done.add(R1);
						done.add(tempFile);
						break;
					}
				}
			}
		}
		return pairs;
	}

	public File getR1() {
		return R1;
	}

	public File getR2() {
		return R2;
	}

}
