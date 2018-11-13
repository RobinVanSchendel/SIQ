package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class PeakDetector {

	public static void main(String[] args) {
		Scanner s;
		try {
			s = new Scanner(new File("peaks.txt"));
			while(s.hasNextLine()) {
				String str = s.nextLine();
				String[] parts = str.split("\t");
				ArrayList<Integer> al = new ArrayList<Integer>();
				DescriptiveStatistics dv = new DescriptiveStatistics();
				for(String part: parts) {
					int i = Integer.parseInt(part);
					al.add(i);
					dv.addValue(i);
				}
				ArrayList<Integer> ids = findMaxIndexes(al,200);
				for(int i: ids) {
					System.out.println(i+":"+al.get(i));
				}
				System.out.println(dv.toString());
				
				//System.exit(0);
				System.out.println("next");
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	static int findPeakUtil(int arr[], int low, int high, int n) 
    { 
        // Find index of middle element 
        int mid = low + (high - low)/2;  /* (low + high)/2 */
  
        // Compare middle element with its neighbours (if neighbours 
        // exist) 
        if ((mid == 0 || arr[mid-1] <= arr[mid]) && (mid == n-1 || 
             arr[mid+1] <= arr[mid])) 
            return mid; 
  
        // If middle element is not peak and its left neighbor is 
        // greater than it,then left half must have a peak element 
        else if (mid > 0 && arr[mid-1] > arr[mid]) 
            return findPeakUtil(arr, low, (mid -1), n); 
  
        // If middle element is not peak and its right neighbor 
        // is greater than it, then right half must have a peak 
        // element 
        else return findPeakUtil(arr, (mid + 1), high, n); 
    } 
  
    // A wrapper over recursive function findPeakUtil() 
    static int findPeak(ArrayList<Integer> arr) 
    { 
        int[] values = new int[arr.size()];
        for(int i = 0;i<arr.size();i++) {
        	values[i]= arr.get(i);
        }
    	return findPeakUtil(values, 0, arr.size()-1, arr.size()); 
    } 
    private static ArrayList<Integer> findMaxIndexes(ArrayList<Integer> arr, int minDist) {

        int count = 0; // To handle special case of singleton list

        int left  = Integer.MAX_VALUE;
        int mid   = Integer.MAX_VALUE;
        int right = Integer.MAX_VALUE;
        ArrayList<Integer> loc = new ArrayList<Integer>();
        //ArrayList<Integer> max = new ArrayList<Integer>();
        
        for(Integer i: arr) {
            count++;
            left = mid;
            mid = right;
            right = i;

            if (right < mid && mid > left) {
            	//if(count-prevMax>=minDist) {
            		//loc.add(count);
            		//max.add(mid);
            		//System.out.println(count+" local max: " + mid);
            		int highestPos = getHighestIndex(arr,count,minDist);
            		//System.out.println(highestPos+" local highest: " + arr.get(highestPos));
            		if(!loc.contains(highestPos)) {
            			if(loc.size()>0 && highestPos-loc.get(loc.size()-1)>(minDist/2)) {
            				loc.add(highestPos);
            				//System.out.println("added");
            			}
            			else if(loc.size()==0) {
            				loc.add(highestPos);
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


	public static int[] calcHistogram(ArrayList<Integer> data, int numBins) {
    		int min = 0;
    		int max = data.size();
    	  final int[] result = new int[numBins];
    	  final double binSize = (max - min)/numBins;

    	  for (double d : data) {
    	    int bin = (int) ((d - min) / binSize);
    	    if (bin < 0) { /* this data is smaller than min */ }
    	    else if (bin >= numBins) { /* this data point is bigger than max */ }
    	    else {
    	      result[bin] += 1;
    	    }
    	  }
    	  return result;
    	}
}
