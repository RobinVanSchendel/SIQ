package data;

public class MetaData {
	private String name;
	private String value;
	public MetaData(String string) {
		this.name = string;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public void setValue(int value) {
		this.value = ""+value;
	}
	public void setValue(boolean value) {
		this.value = ""+value;
	}
	public void setValue(double max) {
		this.value = ""+max;
	}
}
