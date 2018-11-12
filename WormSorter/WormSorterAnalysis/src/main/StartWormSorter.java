package main;

import java.io.File;
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
		
		File profDir = new File("C:\\WormSorter\\20181029");
		FileInfoList fileInfo = FileInfoList.parse("20181107_Exp1a.txt");

		for(FileInfo s: fileInfo.getFileInfos()) {
			wsc.addFile(profDir, s);
		}
		wsc.printContents();
		//System.exit(0);
		wsc.setTrendLine("Extinction", "Red", tofMin, tofMax);
		
		wsc.printWormsHighRed(tofMin, tofMax);
		ArrayList<Worm> al = wsc.getWormsHighRed(tofMin, tofMax);
		ArrayList<Worm> alNot = wsc.getWormsNotHighRed(tofMin, tofMax);
		System.out.println("color\t"+Worm.getHeader());
		for(Worm w: al) {
			System.out.println("Red\t"+w);
			//System.out.println(w.getChannelsString());
		}
		for(Worm w: alNot) {
			System.out.println("NotRed\t"+w);
			//System.out.println(w.getChannelsString());
		}
	}

}
