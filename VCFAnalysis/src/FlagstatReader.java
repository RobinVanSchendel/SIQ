import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class FlagstatReader {

	public static void main(String[] args) {
		//File f = new File("Z:\\Datasets - NGS, UV_TMP, MMP\\NGS\\bams\\worms\\Pindel_Raw\\LUMC-001-010\\flagstat.txt");
		//File f = new File("flagstat.txt");
		File f = new File("Z:\\Datasets - NGS, UV_TMP, MMP\\NGS\\LUMC-001-101\\Analysis\\Flagstat\\flagstat.txt");
		try {
			Scanner s = new Scanner(f);
			//int count = 0;
			//System.out.println(FlagStat.getHeader());
			System.out.println(FlagStat.getDupHeader());
			while(s.hasNextLine()) {
				File file = new File(s.nextLine());
				FlagStat fl = new FlagStat(file);
				for(int i = 0;i<13;i++) {
					String data = s.nextLine();
					fl.setData(data);
				}
				//System.out.println(fl);
				System.out.println(fl.toStringDuplicates());
			}
			s.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}

}
