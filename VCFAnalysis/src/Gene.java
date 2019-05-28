import java.util.ArrayList;
import java.util.HashMap;

public class Gene {
	private HashMap<String, Integer> types = new HashMap<String, Integer>();
	private String gene;
	public Gene(String gene) {
		this.gene = gene;
	}
	public void addType(String type) {
		if(!types.containsKey(type)) {
			types.put(type,0);
		}
		types.put(type, types.get(type)+1);
	}
	public String getTypes(ArrayList<String> types) {
		String ret = "";
		for(String type: types) {
			if(this.types.containsKey(type)) {
				ret+=this.types.get(type);
			}
			else {
				ret+=0;
			}
			ret+="\t";
		}
		return ret;
	}
}
