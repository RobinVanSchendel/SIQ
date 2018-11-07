package main;

public class FileInfo {
	private String f, ch0, ch1, ch2,ch3;
	private boolean control;
	
	public FileInfo(String f, String ch0, String ch1, String ch2, String ch3, String control) {
		this.f = f;
		this.ch0 = ch0;
		this.ch1 = ch1;
		this.ch2 = ch2;
		this.ch3 = ch3;
		this.control = Boolean.parseBoolean(control);
	}

	public String getF() {
		return f;
	}

	public String getCh0() {
		return ch0;
	}

	public String getCh1() {
		return ch1;
	}

	public String getCh2() {
		return ch2;
	}

	public String getCh3() {
		return ch3;
	}

	public boolean isControl() {
		return control;
	}
}
