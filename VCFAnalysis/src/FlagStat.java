import java.io.File;

public class FlagStat {
	private File file;
	private int total, mapped;
	private int duplicates;
	
	public FlagStat(File file) {
		this.file = file;
	}
	public void setData(String line) {
		String[] parts = line.split(" \\+ ");
		if(parts[1].contains("in total")) {
			this.total = Integer.parseInt(parts[0]);
		}
		else if(parts[1].contains("0 mapped")) {
			//System.out.println(line);
			this.mapped = Integer.parseInt(parts[0]);
		}
		else if(parts[1].contains("0 duplicates")) {
			//System.out.println(line);
			this.duplicates = Integer.parseInt(parts[0]);
		}
	}
	public String toString() {
		double percMapped = mapped/(double)total;
		String name = file.getName().replace(".sorted.bam", "");
		String s = name+"\t"+total+"\t"+percMapped;
		return s;
	}
	public static String getHeader() {
		return "Sample\tTotal reads\tMapped reads (%)";
	}
	public String toStringDuplicates() {
		double percMapped = mapped/(double)total;
		String name = file.getName().replace(".sorted.bam", "");
		String s = name+"\t"+total+"\t"+mapped+"\t"+duplicates+"\t"+percMapped;
		return s;
	}
	public static String getDupHeader() {
		return "Sample\tTotal reads\tMapped reads\tDuplicate reads\tMapped reads (%)";
	}
}
