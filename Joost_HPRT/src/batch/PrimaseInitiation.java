package batch;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import utils.CompareSequence;

public class PrimaseInitiation {

	public static void main(String[] args) {
		String fileName = "PrimaseTable.txt";
		//String fileName = "PrimaseTablePart.txt";
		SequenceController sq = new SequenceController();
		File file = new File(fileName);
		ArrayList<CompareSequence> al = new ArrayList<CompareSequence>();
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
					if(parts.length>0){
						String files = parts[fileColumn];
						String subject = parts[subjectColumn];
						String leftFlank = parts[lFColumn];
						String rightFlank = parts[rFColumn];
						String type = "";
						if(typeColumn>=0 && parts.length>typeColumn){
							type = parts[typeColumn];
						}
						System.out.println(files);
						al.addAll(sq.readFilesTryToMatch(files, subject, leftFlank, rightFlank, type, null));
						System.out.println("We have now "+al.size()+" correct sequences");
					}
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HashMap<String, ArrayList<CompareSequence>> hm = new HashMap<String,ArrayList<CompareSequence>>();
		for(CompareSequence cs: al){
			String str = cs.getSubject()+"|"+cs.getUniqueClass();
			if(hm.containsKey(str)){
				hm.get(str).add(cs);
			}
			else{
				ArrayList<CompareSequence> temp = new ArrayList<CompareSequence>();
				temp.add(cs);
				hm.put(str, temp);
			}
		}
		for (String key : hm.keySet()) {
			ArrayList<CompareSequence> temp = hm.get(key);
			ArrayList<String> ids = new ArrayList<String>();
			if(temp.size()>1){
				ArrayList<CompareSequence> tempAL = new ArrayList<CompareSequence>();
				//check ID parts
				for(CompareSequence cs: temp){
					boolean removed = false;
					String str = cs.getIDPart();
					String[] parts = str.split("_");
					if(parts.length == 2){
						//double, flag
						if(ids.contains(str)){
							//System.out.println("1Removing "+cs.getIDPart());
							//al.remove(cs);
							cs.flagPossibleDouble(true);
							removed = true;
						}
					}
					if(parts.length > 2){
						String part = "";
						//again dubious, but required
						System.out.println(parts[1]);
						if(!parts[1].equals("") && parts[1].matches("[0-9]*") && Integer.parseInt(parts[1])<90){
							for(int i = 0;i<parts.length-1;i++){
								if(part.length()>0){
									part += "_";
								}
								part += parts[i];
							}
							//now do some dubious check
							for(int i=1;i<5;i++){
								//found already in a related sample
								//double, flag
								if(ids.contains(part+"_"+i)){
									//al.remove(cs);
									cs.flagPossibleDouble(true);
									removed = true;
								}
							}
							
						}
					}
					ids.add(str);
				}
			}
		}
		System.out.println(CompareSequence.getOneLineHeader());
		for(CompareSequence cs: al){
			System.out.println(cs.toStringOneLine());
		}
	}

}
