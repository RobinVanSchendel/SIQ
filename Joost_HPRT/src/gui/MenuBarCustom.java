package gui;

import java.awt.MenuBar;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class MenuBarCustom {
	private JMenuBar menuBar;
	private JMenu menu;
	private JMenuItem menuItem;
	private JCheckBoxMenuItem cbMenuItem, cbMenuItem2;
	
	public MenuBarCustom(){
		menuBar = new JMenuBar();
		menu = new JMenu("Options");
		menuBar.add(menu);
		cbMenuItem = new JCheckBoxMenuItem("Try to match sequence to Fasta file name");
		menu.add(cbMenuItem);
		cbMenuItem2 = new JCheckBoxMenuItem("Match all sequences");
		menu.add(cbMenuItem2);
	}
	
	public JMenuBar getMenuBar() {
		return menuBar;
	}
	
	public boolean tryToMatchFasta(){
		return cbMenuItem.isSelected();
	}
	public boolean tryAllFasta(){
		return cbMenuItem2.isSelected();
	}
}
