import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class TandemDuplicationCheck {

	public static void main(String[] args) {
		File f = new File("E:\\Cosmic_Out.txt");
		int typeColumn = -1;
		int geneColumn = -1;
		GeneController gc = new GeneController();
		int count = 0;
		ArrayList<String> types = new ArrayList<String>();
		try {
			Scanner s = new Scanner(f);
			boolean first = true;
			while(s.hasNextLine()) {
				String line = s.nextLine();
				String[] parts = line.split("\t");
				if(first) {
					for(int i = 0;i<parts.length;i++) {
						if(parts[i].equals("Alias")) {
							geneColumn = i;
						}
						if(parts[i].equals("Type")) {
							typeColumn = i;
						}
					}
					first = false;
				}
				else{
					if(parts.length>20) {
						String type = parts[typeColumn];
						String gene = parts[geneColumn];
						if(!types.contains(type)) {
							types.add(type);
						}
						gc.addGene(gene, type);
						count++;
						if(count%100000==0) {
							System.out.println("I already processed "+count+" lines");
							//break;
						}
					}
					//System.out.println(gene+"\t"+type);
					//System.exit(0);
				}
				
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("I already processed "+count+" lines");
		System.out.println("Gene\t"+String.join("\t", types));
		gc.printGenes(types);
		
	}

}
