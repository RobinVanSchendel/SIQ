package utils;

import java.util.ArrayList;
import java.util.HashMap;

public class BarcodeCounter {
	private HashMap<String, Integer> countEvents = new HashMap<String, Integer>();
	private HashMap<String, ArrayList<String>> eventBarcodes = new HashMap<String, ArrayList<String>>();
	
	public void putOrAdd(String eventKey, String barcode, int reads) {
		if(barcode==null) {
			barcode = "dummy";
		}
		String key = createKey(eventKey,barcode);
		Integer currentReads = countEvents.get(key);
		if(currentReads==null) {
			countEvents.put(key,reads);
			addBarcode(eventKey, barcode);
		}
		else {
			countEvents.put(key,currentReads+reads);
		}
	}
	private void addBarcode(String key, String barcode) {
		ArrayList<String> barcodes = eventBarcodes.get(key);
		if(barcodes==null) {
			barcodes = new ArrayList<String>();
			barcodes.add(barcode);
			eventBarcodes.put(key,barcodes);
		}
		else {
			barcodes.add(barcode);
		}
		
	}
	public Integer get(String eventKey, String barcode) {
		if(barcode==null) {
			barcode = "dummy";
		}
		String key = createKey(eventKey,barcode);
		return countEvents.get(key);
	}
	private String createKey(String eventKey, String barcode) {
		return eventKey+"|"+barcode;
	}
	public ArrayList<String> getBarcodes(String eventKey){
		return eventBarcodes.get(eventKey);
	}
	
}
