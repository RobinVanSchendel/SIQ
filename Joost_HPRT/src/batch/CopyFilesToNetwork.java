package batch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;

public class CopyFilesToNetwork {

	public static void main(String[] args) {
		File f = new File("Reversion_Copy_List.txt");
		File outputDir = new File("Z:/Robin/Project_Primase/unc-22_reversion_assay/");
		//be sure to switch this as needed!!!!!!
		boolean copy = false;
		try {
			Scanner s = new Scanner(f);
			boolean first = true;
			while(s.hasNextLine()){
				String line = s.nextLine();
				if(first){
					first = false;
					continue;
				}
				String[] parts = line.split("\t");
				File destDir = new File(outputDir.getAbsolutePath()+"/"+parts[2]);
				if(!destDir.exists()){
					destDir.mkdir();
				}
				File newFile = new File(destDir.getAbsolutePath()+"/"+parts[0]);
				File copyFrom = new File(parts[1]);
				try {
					if(!newFile.exists() && copy){
						Files.copy(copyFrom.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
