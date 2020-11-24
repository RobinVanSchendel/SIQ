package main;
import gui.GUI;
import gui.PropertiesManager;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class SV_Analyzer {
	public final static String VERSION = "4.3";
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			UIManager.setLookAndFeel(
			        UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InstantiationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UnsupportedLookAndFeelException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		PropertiesManager pm = new PropertiesManager();
		new GUI(VERSION, pm);
	}
}
