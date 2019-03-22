package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class BlastPanel extends JFrame implements ActionListener{
	PropertiesManager pm;
	JFileChooser jfc = new JFileChooser();
	JTextField jt, jtDb;
	
	public BlastPanel(PropertiesManager pm) {
		this.pm = pm;
		this.setSize(600,400);
		JPanel jpanel = new JPanel();

		//Blast exec
		jpanel.add(new JLabel("Blast executable:"));
		jt = new JTextField();
		jt.setEnabled(false);
		jt.setPreferredSize(new Dimension(300,20));
		jpanel.add(jt);
		JButton blastB = new JButton("Select program");
		blastB.addActionListener(this);
		jpanel.add(blastB);
		
		//Blast db
		JPanel jpanel1 = new JPanel();
		jpanel1.add(new JLabel("Blast database:"));
		jtDb = new JTextField();
		jtDb.setEnabled(false);
		jtDb.setPreferredSize(new Dimension(300,20));
		jpanel1.add(jtDb);
		JButton blastDB = new JButton("Select DB");
		blastDB.addActionListener(this);
		jpanel1.add(blastDB);
		this.add(jpanel,  BorderLayout.NORTH);
		this.add(jpanel1, BorderLayout.CENTER);
		loadProperties();
		
		//clear all
		JPanel jpanel2 = new JPanel();
		JButton OK = new JButton("OK");
		JButton blastClear = new JButton("Clear Blast fields");
		blastClear.addActionListener(this);
		OK.addActionListener(this);
		jpanel2.add(OK);
		jpanel2.add(blastClear);
		this.add(jpanel2, BorderLayout.SOUTH);
	}
	private void loadProperties() {
		jt.setText(pm.getProperty("blast"));
		jtDb.setText(pm.getProperty("blastDB"));
		if(pm.getProperty("lastDir") != null) {
        	File f = new File(pm.getProperty("lastDir"));
        	jfc.setCurrentDirectory(f);
        }
		
	}
	@Override
	public void actionPerformed(ActionEvent arg0) {
		if(arg0.getActionCommand().equals("Select program")) {
			if(jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
				fillBlastExec();
			}
		}
		if(arg0.getActionCommand().equals("Select DB")) {
			if(jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
				fillBlastDb();
			}
		}
		if(arg0.getActionCommand().equals("Clear Blast fields")) {
			clearBlast();
		}
		if(arg0.getActionCommand().equals("OK")) {
			this.dispose();
		}
	}
	private void clearBlast() {
		pm.setProperty("blast", "");
		pm.setProperty("blastDB", "");
		loadProperties();
	}
	private void fillBlastExec() {
		File f = jfc.getSelectedFile();
		System.out.println(f.getAbsolutePath());
		pm.setProperty("blast", f.getAbsolutePath());
		jt.setText(pm.getProperty("blast"));
		
	}
	private void fillBlastDb() {
		File f = jfc.getSelectedFile();
		String name = f.getAbsolutePath();
		name = name.substring(0, name.lastIndexOf(".")); 
		pm.setProperty("blastDB", name);
		jtDb.setText(pm.getProperty("blastDB"));
	}
}
