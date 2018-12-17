package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import controller.WormSorterController;
import controller.WormSorterFileController;
import data.PolyTrendLine;
import data.TrendLine;
import data.Worm;

public class StartWormSorter {

	public static void main(String[] args) {
		
		double tofMin = 500;
		double tofMax = 2000;
		
		WormSorterController wsc = new WormSorterController(); 
		
		File profDir = new File("C:\\WormSorter\\update\\");
		FileInfoList fileInfo = FileInfoList.parse("20181107_Exp1a.txt");

		for(FileInfo s: fileInfo.getFileInfos()) {
			wsc.addFile(profDir, s);
		}
		wsc.printContents();
		//System.exit(0);
		String col1 = "Extinction";
		String col2 = "Red";
		wsc.setTrendLine(col1, col2, tofMin, tofMax, 2);
		
		wsc.printWormsHighRed(col1, col2, tofMin, tofMax);
		ArrayList<Worm> al = wsc.getWormsHighRed(col1, col2, tofMin, tofMax);
		ArrayList<Worm> alNot = wsc.getWormsNotHighRed(col1, col2, tofMin, tofMax);
		System.out.println("color\t"+Worm.getHeader());
		
		col2 = "Green";
		wsc.setTrendLine(col1, col2, tofMin, tofMax, 2);
		wsc.printWormsHighRedAndGreen(al, col1, col2, tofMin, tofMax);
		ArrayList<Worm> redAndgreen = wsc.getWormsHighRedAndGreen(al, col1, col2, tofMin, tofMax);
		ArrayList<Worm> redNotgreen = new ArrayList<Worm>();
		for(Worm w: al){
			boolean contains = false;
			for(Worm redgreen: redAndgreen){
				if(w.getKey().equals(redgreen.getKey())){
					contains = true;
					break;
				}
			}
			if(!contains){
				redNotgreen.add(w);
			}
		}
		
		
		File f = new File("out.txt");
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(f);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(Worm.getHeader());
		for(Worm w: redAndgreen) {
			System.out.println("redAndgreen\t"+w);
			pw.println("redAndgreen\t"+w);
			pw.println(w.getChannelsString());
			//System.out.println(w.getChannelsString());
		}
		for(Worm w: redNotgreen) {
			System.out.println("redNotgreen\t"+w);
			//System.out.println(w.getChannelsString());
			pw.println("redNotgreen\t"+w);
			pw.println(w.getChannelsString());
		}
		for(Worm w: alNot) {
			//System.out.println("NotRed\t"+w);
			//System.out.println(w.getChannelsString());
			pw.println("NotRed\t"+w);
			pw.println(w.getChannelsString());
		}
		pw.close();
	}

}
