import java.util.ArrayList;
import java.util.HashMap;

public class GeneController {
	private HashMap<String, Gene> genes = new HashMap<String, Gene>();
	public void addGene(String gene, String type) {
		if(!genes.containsKey(gene)) {
			Gene g = new Gene(gene);
			genes.put(gene, g);
		}
		genes.get(gene).addType(type);
	}
	public void printGenes(ArrayList<String> types) {
		for(String key: genes.keySet()) {
			String str = genes.get(key).getTypes(types);
			System.out.println(key+"\t"+str);
		}
	}
}
