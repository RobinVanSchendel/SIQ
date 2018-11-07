package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class FileInfoList {
	private ArrayList<FileInfo> list = new ArrayList<FileInfo>();

	public static FileInfoList parse(String string) {
		FileInfoList fil = new FileInfoList();
		try {
			Scanner s = new Scanner(new File(string));
			boolean first = true;
			while(s.hasNextLine()) {
				String line = s.nextLine();
				String[] parts = line.split("\t");
				if(first) {
					first = false;
				}
				else {
					FileInfo fi = new FileInfo(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]);
					fil.addFIleInfo(fi);
				}
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fil;
	}
	public void addFIleInfo(FileInfo f) {
		list.add(f);
	}
	public ArrayList<FileInfo> getFileInfos() {
		return list;
	}
}
