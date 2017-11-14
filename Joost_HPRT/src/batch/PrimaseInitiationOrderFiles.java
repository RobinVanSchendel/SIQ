package batch;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;






import utils.CompareSequence;

public class PrimaseInitiationOrderFiles {

	public static void main(String[] args) {
		String fileName = "PrimaseTableOrder.txt";
		//String[] dirs = {"Z:/Robin/Project_Primase","Z:/Tim/","Z:/Evelyne/","C:/Users/rvanschendel/Documents/Project_Primase" };
		//String fileName = "PrimaseTablePart.txt";
		File file = new File(fileName);
		HashMap<String, File> files = new HashMap<String, File>();
		Scanner s = null;
		try {
			s = new Scanner(file);
			boolean first = true;
			int fileColumn = -1;
			int subjectColumn = -1;
			int lFColumn = -1;
			int rFColumn = -1;
			int typeColumn = -1;
			while(s.hasNextLine()){
				String line = s.nextLine();
				String[] parts = line.split("\t");
				if(first){
					int i = 0;
					for(String str: parts){
						if(str.equals("Files")){
							fileColumn = i;
						}
						if(str.equals("Subject")){
							subjectColumn = i;
						}
						if(str.equals("LeftFlank")){
							lFColumn = i;
						}
						if(str.equals("RightFlank")){
							rFColumn = i;
						}
						if(str.equals("Type")){
							typeColumn = i;
						}
						i++;
					}
					first = false;
				}
				else{
					if(parts.length>rFColumn){
						String dirName = parts[fileColumn];
						String subject = parts[subjectColumn];
						String leftFlank = parts[lFColumn];
						String rightFlank = parts[rFColumn];
						String type = "";
						if(typeColumn>=0 && parts.length>typeColumn){
							type = parts[typeColumn];
						}
						//System.out.println(dirName);
						Vector<File> ab1s = SequenceController.getAB1Files(new File(dirName));
						addAbs1toHash(ab1s, files);
						for(File f: ab1s){
							//System.out.println(f);
						}
						//al.addAll(sq.readFilesTryToMatch(files, subject, leftFlank, rightFlank, type, null));
					}
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println("We have now "+files.size()+" correct sequences");
		printHashToTab(files);
	}

	private static void printHashToTab(HashMap<String, File> files) {
		Pattern p = Pattern.compile("^XF\\d\\d\\d\\d");
		System.out.println("Name\tFile\tStrain\tMatch");
		SortedSet<String> keys = new TreeSet<String>(files.keySet());
		for(String key: keys){
			File f = files.get(key);
			Matcher m = p.matcher(f.getName());
			String matchString = f.getName();
			boolean match = false;
			if(m.find()){
				matchString = m.group();
				match = true;
			}
			System.out.println(key+"\t"+f.getAbsolutePath()+"\t"+matchString+"\t"+match);
		}
	}

	private static void addAbs1toHash(Vector<File> ab1s,
			HashMap<String, File> files) {
		for(File f: ab1s){
			if(!files.containsKey(f.getName())){
				files.put(f.getName(), f);
			}
		}
	}

}
