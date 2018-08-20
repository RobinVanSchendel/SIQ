package utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class UtilsReduce {
	public static boolean positionIsOk(String[] parts, HashMap<String, Integer> index, ArrayList<SetAndPosition> pos,
			String matchStart, String matchEnd, String file, int maxPosDeviation, HashMap<String, String> countMap) {
		String matchS = parts[index.get(matchStart)];
		String matchE = parts[index.get(matchEnd)];
		int matchStartInt = Integer.parseInt(matchS);
		int matchEndInt = Integer.parseInt(matchE);
		String key = parts[index.get(file)];
		if(countMap != null) {
			key = countMap.get(parts[index.get(file)]);
		}
		for(SetAndPosition sap: pos) {
			//this might not be the best match ever...
			if(key.equals(sap.getName())) {
				return sap.withinPosition(matchStartInt, matchEndInt, maxPosDeviation);
			}
		}
		return false;
	}
	public static ArrayList<String> ReadInFileNames(File file) {
		ArrayList<String> samples = new ArrayList<String>();
		Scanner s;
		try {
			s = new Scanner(file);
			while(s.hasNextLine()) {
				String line = s.nextLine();
				samples.add(line);
			}
			s.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return samples;
	}
	public static HashMap<String, String> ReadInFileNamesChanges(File names) throws FileNotFoundException {
		HashMap<String, String> hm = new HashMap<String, String>();
		Scanner s = new Scanner(names);
		while(s.hasNextLine()) {
			String line = s.nextLine();
			String[] parts = line.split("\t");
			if(parts.length>1) {
				hm.put(parts[0], parts[1]);
			}
		}
		s.close();
		return hm;
	}
	public static ArrayList<SetAndPosition> countPositions(File f, HashMap<String, String> namesMap, HashMap<String, Integer> index, String file,
			String columnStart, String columnEnd, String countColumn) {
		HashMap<String, HashMap<Integer, Integer>> countOne = new HashMap<String, HashMap<Integer, Integer>>();
		HashMap<String, HashMap<Integer, Integer>> countTwo = new HashMap<String, HashMap<Integer, Integer>>();
		Scanner s;
		try {
			s = new Scanner(f);
			while(s.hasNextLine()) {
				String line = s.nextLine();
				String[] parts = line.split("\t");
				String key = parts[index.get(file)];
				//skip those
				if(key.equals("File")) {
					continue;
				}
				String realKey = namesMap.get(key);
				if(realKey== null) {
					System.err.println("I cannot find the key "+key+" in this hash");
				}
				String one = parts[index.get(columnStart)];
				String two = parts[index.get(columnEnd)];
				String count = parts[index.get(countColumn)];
				int oneInt = Integer.parseInt(one);
				int twoInt = Integer.parseInt(two);
				int countInt = Integer.parseInt(count);
				if(!countOne.containsKey(realKey)) {
					HashMap<Integer, Integer> hm = new HashMap<Integer, Integer>();
					countOne.put(realKey, hm);
					hm = new HashMap<Integer, Integer>();
					countTwo.put(realKey, hm);
				}
				//first counter
				HashMap<Integer, Integer> counterOne = countOne.get(realKey);
				if(!counterOne.containsKey(oneInt)) {
					counterOne.put(oneInt, 0);
				}
				counterOne.put(oneInt, counterOne.get(oneInt)+countInt);				
				//second counter
				HashMap<Integer, Integer> counterTwo = countTwo.get(realKey);
				if(!counterTwo.containsKey(twoInt)) {
					counterTwo.put(twoInt, 0);
				}
				counterTwo.put(twoInt, counterTwo.get(twoInt)+countInt);
			}
			s.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ArrayList<SetAndPosition> pos = new ArrayList<SetAndPosition>();
		for(String key: countOne.keySet()) {
			HashMap<Integer, Integer> hm = countOne.get(key);
			int max = 0;
			int maxKey = 0;
			for(int keyInt: hm.keySet()) {
				if(hm.get(keyInt)>max) {
					max = hm.get(keyInt);
					maxKey = keyInt;
				}
			}
			SetAndPosition sap = new SetAndPosition(key, maxKey);
			pos.add(sap);
			System.out.println(key+"\t"+maxKey+"\t"+hm.get(maxKey));
		}
		for(String key: countTwo.keySet()) {
			HashMap<Integer, Integer> hm = countTwo.get(key);
			int max = 0;
			int maxKey = 0;
			for(int keyInt: hm.keySet()) {
				if(hm.get(keyInt)>max) {
					max = hm.get(keyInt);
					maxKey = keyInt;
				}
			}
			for(SetAndPosition sap : pos) {
				if(sap!= null && sap.getName().equals(key)) {
					sap.setMax(maxKey);
				}
			}
			System.out.println(key+"\t"+maxKey+"\t"+hm.get(maxKey));
		}
		return pos;
	}
	public static ArrayList<String> retrieveColumn(File f, String string, boolean unique) {
		ArrayList<String> ret = new ArrayList<String>();
		int columnString = -1;
		try {
			Scanner s = new Scanner(f);
			boolean first = true;
			while(s.hasNextLine()) {
				String line = s.nextLine();
				String[] parts = line.split("\t");
				if(first) {
					first = false;
					for(int i = 0;i<parts.length;i++) {
						if(parts[i].equals(string)) {
							columnString = i;
						}
					}
					if(columnString == -1) {
						System.err.println("Unable to find column "+string);
						return null;
					}
					
				}
				//remove unwanted lines
				else if(!parts[columnString].equals(string)) {
					if(unique) {
						if(!ret.contains(parts[columnString])) {
							ret.add(parts[columnString]);
						}
					}
					else {
						ret.add(parts[columnString]);
					}
				}
			}
			s.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}


	public static String extractKey(String[] parts, int fileColumn, int leftFlankColumn, int subjectColumn,
			int rightFlankColumn, int insertColumn) {
		String s = "_";
		return parts[fileColumn]+s+parts[leftFlankColumn]+s+parts[subjectColumn]+s+parts[rightFlankColumn]+s+parts[insertColumn];
	}
	
	public static HashMap<String, Integer> getHeader(File file) {
		HashMap<String, Integer> hm = new HashMap<String, Integer> ();
		try {
			Scanner s = new Scanner(file);
			if(s.hasNextLine()) {
				String line = s.nextLine();
				String[] parts = line.split("\t");
				for(int i=0;i<parts.length;i++) {
					hm.put(parts[i], i);
				}
			}
			s.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return hm;
	}
	
	public static ArrayList<SetAndPosition> readInFilePositions(File file) {
		ArrayList<SetAndPosition> pos = new ArrayList<SetAndPosition>();
		try {
			Scanner s = new Scanner(file);
			while(s.hasNextLine()) {
				pos.add(SetAndPosition.parse(s.nextLine()));
			}
			s.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return pos;
	}

	public static HashMap<String, String> assignReplacementNames(ArrayList<String> fileNames) {
		HashMap<String, String> hm = new HashMap<String, String>();
		for(String s: fileNames) {
			//until the first underscore
			String replace = s.substring(0, s.indexOf('_'));
			hm.put(s, replace);
		}
		return hm;
	}
	public static HashMap<String, String> assignReplacementNamesTillLast(ArrayList<String> fileNames) {
		HashMap<String, String> hm = new HashMap<String, String>();
		for(String s: fileNames) {
			//until the first underscore
			String replace = s.substring(0, s.lastIndexOf('_'));
			hm.put(s, replace);
		}
		return hm;
	}
	public static ArrayList<SetAndPosition> createInwardPositions(ArrayList<SetAndPosition> pos, int minSizeInward) {
		ArrayList<SetAndPosition> ret = new ArrayList<SetAndPosition>();
		for(SetAndPosition sp: pos) {
			SetAndPosition temp = new SetAndPosition(sp.getName(), sp.getMin()+minSizeInward);
			temp.setMax(sp.getMax()-minSizeInward);
			ret.add(temp);
		}
		return ret;
	}
	public static boolean positionIsOkInward(String[] parts, HashMap<String, Integer> index, ArrayList<SetAndPosition> pos,
			String matchStart, String matchEnd, String file, int maxPosDeviation, HashMap<String, String> countMap) {
		String matchS = parts[index.get(matchStart)];
		String matchE = parts[index.get(matchEnd)];
		int matchStartInt = Integer.parseInt(matchS);
		int matchEndInt = Integer.parseInt(matchE);
		String key = parts[index.get(file)];
		if(countMap != null) {
			key = countMap.get(parts[index.get(file)]);
		}
		for(SetAndPosition sap: pos) {
			//this might not be the best match ever...
			if(key.equals(sap.getName())) {
				return sap.positionsBounded(matchStartInt, matchEndInt);
			}
		}
		return false;
	}
}
