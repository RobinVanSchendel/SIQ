package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;


public class CompressLargeSplitFiles {

	public static void main(String[] args) {
		ArrayList<File> files = new ArrayList<File>();
		File tempFile = new File("E:\\Joost_Repair_Seq\\outSlurm\\");
		if(tempFile.isDirectory()) {
			for(File file: tempFile.listFiles()) {
				if(!file.getName().contains("compressed") && !file.getName().contains("stats")) {
					files.add(file);
				}
			}
		}
		else {
			files.add(tempFile);
		}
		for(File f: files) {
			String out = f.getAbsolutePath()+"_compressed.txt";
			File outF = new File(out);
			if(outF.exists()) {
				System.out.println(outF.getName()+" already exists, skipping");
				continue;
			}
			HashMap<String, Line> hm = new HashMap<String, Line>();
			try {
				Scanner s = new Scanner(f);
				String header = s.nextLine();
				int aliasColumn = findColumn("Alias", header);
				int keyColumn = findColumn("ClassName", header);
				int countEventsColumn = findColumn("countEvents", header);
				int count =0;
				String alias = null;
				while(s.hasNextLine()) {
					String line = s.nextLine();
					String[] parts = line.split("\t");
					String key = parts[keyColumn];
					if(alias == null) {
						alias = parts[aliasColumn];
					}
					String totalKey = key;
					//dangerous, better to make the actual key correct
					String newKey = totalKey.substring(totalKey.indexOf(" BC:")+4);
					Line l = new Line(line);
					int countEvents = Integer.parseInt(parts[countEventsColumn]);
					l.setCountEvents(countEvents);
					count++;
					Line tempLine = hm.get(newKey);
					if(tempLine != null) {
						tempLine.addCountevents(l.getCountEvents());
					}
					else {
						hm.put(newKey,l);
					}
					count++;
					if(count%10000==0) {
						System.out.println("Already processed "+count+" lines "+hm.size());
					}
				}
				int total = 0;
				//count total
				for(String key: hm.keySet()) {
					total += hm.get(key).getCountEvents();
				}
				//set fraction and print
				BufferedWriter bw = new BufferedWriter(new FileWriter(outF));
				bw.write(header+"\n");
				for(String key: hm.keySet()) {
					hm.get(key).setFraction(total);
					hm.get(key).setAlias(alias);
					bw.write(hm.get(key).toString(aliasColumn)+"\n");
					
				}
				bw.close();
				Path p = Paths.get(out);
				System.out.println(Files.size(p));
				
				s.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		

	}

	private static int findColumn(String string, String header) {
		String[] heads = header.split("\t");
		for(int i =0;i<heads.length;i++) {
			if(heads[i].contentEquals(string)) {
				System.out.println("found "+string+ ":"+i);
				return i;
			}
		}
		System.err.println("Cannot find "+string);
		System.err.println(header);
		System.exit(0);
		return -1;
	}

}
