package main;

import java.io.File;

public class PairedEnd {
	private File R1, R2;
	public PairedEnd(File R1, File R2) {
		this.R1 = R1;
		this.R2 = R2;
	}
	public String toString() {
		return "R1: "+R1.getAbsolutePath()+" R2: "+R2.getAbsolutePath();
	}
	public File getR1() {
		return R1;
	}
	public File getR2() {
		return R2;
	}
}
