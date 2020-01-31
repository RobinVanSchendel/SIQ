package batch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import org.biojavax.bio.seq.RichSequence;

import utils.CompareSequence;
import utils.Utils;

public class PrimaseInitiationAnalysisOrderedDir {

	public static void main(String[] args) {
		//String fileName = "PrimaseTable.txt";
		String workDir = "Z:/Robin/Project_Primase/unc-22_reversion_assay";
		String leftRightFlank = "Z:/Robin/Project_Primase/unc-22_reversion_assay/flanks.txt";
		double quality = 0.05; //0.05
		
		//some reads are blacklisted as they are sequenced multiple times or something
		//else is going on
		String blacklist = "blacklist.txt";
		Vector<String> blacklistV = fillBlackList(blacklist);
		
		//extra parameters
		String analyseDirName = null; //"XF1494";
		//analyseDirName = "XF1488"; //"XF1494";
		analyseDirName = "XF1717";
		boolean printNonCorrect = false; //false
		boolean printXY = true;
		boolean checkLeftRight = true;
		
		
		ArrayList<CompareSequence> al = new ArrayList<CompareSequence>();
		File leftRightFlankFile = new File(leftRightFlank);
		SequenceController sq = new SequenceController();
		File file = new File(workDir);
		ArrayList<File> ab1FileDirs = getAllDirs(file);
		ArrayList<RichSequence> sequences = getAllRichSequencesOfFastaFiles(file, ab1FileDirs);
		//System.out.println(ab1FileDirs.size());
		//for(File dirFile: ab1FileDirs){
			//System.out.println(dirFile.getName());
		//}
		//check if they are ok!
		for(File tempDir: ab1FileDirs){
			String[] flanks = obtainLeftRightFlank(leftRightFlankFile, tempDir.getName());
		}
		//Good to go!
		long start = System.nanoTime();
		for(File tempDir: ab1FileDirs){
			if(analyseDirName!= null && !tempDir.getName().equals(analyseDirName)){
				continue;
			}
			//find the right sequence
			RichSequence currentSequence = null;
			for(RichSequence r: sequences){
				if(r.getName().equals(tempDir.getName())){
					currentSequence = r;
				}
			}
			//get Left and RightFlanks
			String[] flanks = obtainLeftRightFlank(leftRightFlankFile, tempDir.getName());
			al.addAll(sq.readFilesTryToMatch(tempDir, currentSequence, flanks[0], flanks[1], null, null, printNonCorrect,quality, checkLeftRight));
			//System.out.println(CompareSequence.getOneLineHeader());
			for(CompareSequence cs: al) {
				
				//System.out.println(cs.toStringOneLine());
			}
			System.out.println("We now have "+al.size()+" sequences");
			//System.exit(0);
		}
		//do something
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
						//System.out.println(parts[1]);
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
		if(printXY){
			sq.setSequences(al);
			sq.printXY();
		}
		long end = System.nanoTime();
		System.out.println("Took "+TimeUnit.SECONDS.convert((end-start),TimeUnit.NANOSECONDS)+" seconds");
	}

	private static Vector<String> fillBlackList(String blacklist) {
		// TODO Auto-generated method stub
		//System.exit(0);
		return null;
	}

	private static String[] obtainLeftRightFlank(File leftRightFlankFile,
			String name) {
		Scanner s;
		try {
			s = new Scanner(leftRightFlankFile);
			while(s.hasNextLine()){
				String[] parts = s.nextLine().split("\t");
				if(parts[0].equals(name)){
					String[] flanks = {parts[1],parts[2]};
					//make sure to close
					s.close();
					return flanks;
				}
			}
			System.err.println("Could not obtain flanks for "+name);
			System.exit(0);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	private static ArrayList<File> getAllDirs(File file) {
		ArrayList<File> dirs = new ArrayList<File>();
		for(File f: file.listFiles()){
			if(f.isDirectory()){
				dirs.add(f);
			}
		}
		return dirs;
	}

	private static ArrayList<RichSequence> getAllRichSequencesOfFastaFiles(File file, ArrayList<File> ab1FileDirs) {
		//only .fa files
		FilenameFilter filter = new FilenameFilter() {
	        public boolean accept(File directory, String fileName) {
	            return fileName.endsWith(".fa");
	        }
		}; 
	    
		ArrayList<RichSequence> sequences = new ArrayList<RichSequence>(); 
		for(File fastaFile: file.listFiles(filter)){
			//System.out.println(fastaFile.getName());
			ArrayList<RichSequence> tempSequences = Utils.fillArrayListSequences(fastaFile);
			for(RichSequence rs: tempSequences){
				//System.out.println("\t"+rs.getName());
			}
			sequences.addAll(tempSequences);
		}
		for(RichSequence r: sequences){
			//System.out.println(r.getName());
		}
		//so now check which ones are missing
		boolean missing = false;
		ArrayList<RichSequence> matchedSequences = new ArrayList<RichSequence>();
		for(File dir: ab1FileDirs){
			String name = dir.getName();
			boolean found = false;
			for(RichSequence r: sequences){
				if(r.getName().equals(name)){
					found = true;
					matchedSequences.add(r);
				}
			}
			if(!found){
				System.err.println("No fasta for directory "+name+" could be found");
				missing = true;
			}
		}
		if(missing){
			System.out.println("Unmatched sequences:");
			System.out.println(sequences.size()+":"+matchedSequences.size());
			sequences.removeAll(matchedSequences);
			for(RichSequence r: sequences){
				System.out.println(r.getName());
			}
			System.out.println("End of unmatched sequences");
			System.err.println("Script will not continue without all sequences\nExiting...");
			System.exit(0);
		}
		return sequences;
	}
}
