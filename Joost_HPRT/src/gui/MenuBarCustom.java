package gui;

import java.awt.MenuBar;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import utils.CompareSequence;

public class MenuBarCustom implements ActionListener{
	private JMenuBar menuBar;
	private JMenu menu, menuOutput;
	private JMenuItem cbMenuItem;
	private JCheckBoxMenuItem cbMenuItem2, printCorrectColumnsOnly;
	private PropertiesManager pm;
	
	public MenuBarCustom(PropertiesManager pm){
		menuBar = new JMenuBar();
		menu = new JMenu("Options");
		menuBar.add(menu);
		printCorrectColumnsOnly = new JCheckBoxMenuItem("Output only rows without remarks");
		menu.add(printCorrectColumnsOnly);
		printCorrectColumnsOnly.addActionListener(this);
		printCorrectColumnsOnly.setActionCommand("printCorrectColumnsOnly");
		
		cbMenuItem = new JMenuItem("Add Blast Search");
		cbMenuItem.addActionListener(this);
		menu.add(cbMenuItem);
		//cbMenuItem2 = new JCheckBoxMenuItem("Match all sequences");
		//menu.add(cbMenuItem2);
		//menuOutput = new JMenu("Output");
		//menuBar.add(menuOutput);
		this.pm = pm;
		loadProperties();
	}
	

	private void loadProperties() {
		printCorrectColumnsOnly.setSelected(pm.getPropertyBoolean("printCorrectColumnsOnly"));
	}

	public JMenuBar getMenuBar() {
		return menuBar;
	}
	/*
	public boolean tryToMatchFasta(){
		return cbMenuItem.isSelected();
	}
	public boolean tryAllFasta(){
		return cbMenuItem2.isSelected();
	}
	*/
	public boolean onlyOutputCorrectRows() {
		return printCorrectColumnsOnly.isSelected(); 
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		if(ae.getActionCommand().equals("printCorrectColumnsOnly")) {
			pm.setProperty("printCorrectColumnsOnly", ""+printCorrectColumnsOnly.isSelected());
			System.out.println("hier "+ printCorrectColumnsOnly.isSelected());
		}
		else if(ae.getActionCommand().equals("Add Blast Search")) {
			BlastPanel bp = new BlastPanel(pm);
			bp.setVisible(true);
		}
	}
}
