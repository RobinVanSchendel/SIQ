package data;

import java.util.ArrayList;

public class Channel {
	private String name;
	private ArrayList<Integer> values = new ArrayList<Integer>();

	public Channel(String string) {
		this.name = string;
	}

	public void addValue(int value) {
		values.add(value);
	}
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for(int v: values) {
			if(sb.length()>0) {
				sb.append("\t");
			}
			sb.append(v);
		}
		return sb.toString();
	}

	public int getHighest() {
		int max = 0;
		for(int i: values) {
			if(i>max) {
				max = i;
			}
		}
		return max;
	}
	public double getAverage() {
		double total = 0;
		double size = 0;
		for(int i: values) {
			if(i>0) {
				total+=i;
				size++;
			}
		}
		return total/size;
	}

	public String getStats() {
		double avg = getAverage();
		double high = getHighest();
		String s = avg+"\t"+high+"\t"+high/avg;
		return s;
	}

	public int size() {
		return values.size();
	}
	public ArrayList<Integer> getValues(){
		return values;
	}

	public void removeFrom(int lastIndex) {
		while(values.size()>lastIndex){
			values.remove(values.size()-1);
		}
	}
	public ArrayList<XY> findPeaks(int minDist) {

        int count = 0; // To handle special case of singleton list

        int left  = Integer.MAX_VALUE;
        int mid   = Integer.MAX_VALUE;
        int right = Integer.MAX_VALUE;
        ArrayList<XY> loc = new ArrayList<XY>();
        //ArrayList<Integer> max = new ArrayList<Integer>();
        
        for(Integer i: values) {
            count++;
            left = mid;
            mid = right;
            right = i;

            if (right < mid && mid > left) {
            	//if(count-prevMax>=minDist) {
            		//loc.add(count);
            		//max.add(mid);
            		//System.out.println(count+" local max: " + mid);
            		int highestPos = getHighestIndex(values,count,minDist);
            		//System.out.println(highestPos+" local highest: " + arr.get(highestPos));
            		if(!loc.contains(highestPos)) {
            			if(loc.size()>0 && highestPos-loc.get(loc.size()-1).getX()>(minDist/2)) {
            				XY xy = new XY(highestPos, values.get(highestPos));
            				loc.add(xy);
            				//System.out.println("added");
            			}
            			else if(loc.size()==0) {
            				XY xy = new XY(highestPos, values.get(highestPos));
            				loc.add(xy);
            			}
            			//max.add(arr.get(highestPos));
            		}
            		//prevMax = count;
            	//}
            }
        }
       return loc;
    }
	private static int getHighestIndex(ArrayList<Integer> arr, int count, int minDist) {
		int start = count-minDist/2;
		int end = count+minDist/2;
		if(start<0) {
			start = 0;
		}
		if(end > arr.size()) {
			end = arr.size();
		}
		int maxPos = -1;
		int maxSize = -1;
		while(start<end) {
			if(arr.get(start)>maxSize) {
				maxPos = start;
				maxSize = arr.get(start);
			}
			start++;
		}
		return maxPos;
	}
}
