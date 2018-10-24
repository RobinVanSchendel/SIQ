package utils;

import java.io.File;

public class SampleInfo {
	private String left, right;
	private File subject;
	private String file;
	public SampleInfo(String file, File subject, String left, String right) {
		this.file = file;
		this.subject = subject;
		this.left = left;
		this.right = right;
	}
	public String getFile() {
		return file;
	}
	public String toString() {
		return subject+"\t"+left+"\t"+right;
	}
	public File getSubject() {
		return subject;
	}
	public String getLeft() {
		return left;
	}
	public String getRight() {
		return right;
	}
}
