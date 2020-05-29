package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.SequenceIterator;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequence.IOTools;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.jcvi.jillion.core.qual.QualitySequence;
import org.jcvi.jillion.core.residue.nt.NucleotideSequence;
import org.jcvi.jillion.trace.chromat.Chromatogram;
import org.jcvi.jillion.trace.chromat.ChromatogramFactory;

import batch.SequenceController;
import batch.SequenceControllerThread;
import dnaanalysis.Utils;
import utils.AnalyzedFileController;
import utils.CompareSequence;
import utils.KMERLocation;
import utils.NGSPair;
import utils.StreamGobbler;
import utils.Subject;

public class GUI implements ActionListener, MouseListener {
	//JFileChooser chooser = new JFileChooser(new File("C:\\Users\\rvanschendel\\Documents\\Project_Joost"));
	JFileChooser chooser = new JFileChooser() {
	    @Override
	    public void approveSelection(){
	        File f = getSelectedFile();
	        if(f.exists() && getDialogType() == SAVE_DIALOG){
	            int result = JOptionPane.showConfirmDialog(this,"The file exists, overwrite?","Existing file",JOptionPane.YES_NO_CANCEL_OPTION);
	            switch(result){
	                case JOptionPane.YES_OPTION:
	                    super.approveSelection();
	                    return;
	                case JOptionPane.NO_OPTION:
	                    return;
	                case JOptionPane.CLOSED_OPTION:
	                    return;
	                case JOptionPane.CANCEL_OPTION:
	                    cancelSelection();
	                    return;
	            }
	        }
	        super.approveSelection();
	    }        
	};
	JFrame guiFrame = new JFrame();
	DefaultListModel<File> model = new DefaultListModel<File>();
	JList<File> jFiles = new JList<File>(model);
	File subject;
	//JTextField left = new JTextField("ttaggcacatgacccgtgtttcctcac");
	//JTextField left = new JTextField("GCATGCGTCGACCCgggaggcctgatttca");
	JTextField left = new JTextField("");
	JSpinner minSupport; 
	//JTextField right = new JTextField("cagtggtgtaaatgctggtccatggct");
	//JTextField right = new JTextField("CCCCCCCCTCCCCCACCCCCTCCCtcgcAATT");
	JTextField right = new JTextField("");
	JCheckBox maskLowQuality = new JCheckBox("maskLowQuality");
	JCheckBox maskLowQualityRemove = new JCheckBox("Mask low quality bases");
	JCheckBox removeRemarkRows = new JCheckBox("Remove sequences with remarks");
	JCheckBox split = new JCheckBox("Split reads in multiple ranges");
	JButton R = new JButton("R");
	JButton Rin = new JButton("Rin");
	private ArrayList<RichSequence> sequences;
	JProgressBar progressBar;
	JLabel maxE = new JLabel("maxError:");
	private JSpinner maxError;
	HashMap<String, String> hmAdditional;
	private PropertiesManager pm;
	private ArrayList<JCheckBox> outputs = new ArrayList<JCheckBox>();
	private JButton analyzeFiles;
	private boolean ab1Perspective = true;
	private String version;
	private JTable ngs;
	private NGSTableModel ngsModel;
	private JButton run;
	private JSpinner maxReads;
	private JButton excelNGS, switchToAB1;
	private JSpinner baseError, cpus, tinsDist;
	private File lastSavedExcel;
	
	
	@SuppressWarnings("serial")
	public GUI(String version, PropertiesManager pm)
    {
		//TODO: make a real icon
		//URL iconURL = getClass().getResource("/butterfly.png");
		//ImageIcon icon = new ImageIcon(iconURL);
		//guiFrame.setIconImage(icon.getImage());
		this.version = version;
		this.pm = pm;
		//this.switchToNGS(true);
		switchToAB1(true);
		return;
    }
	private void addOutputPanel() {
		JPanel jpanel = new JPanel();
		//jpanel.setSize(30, 800);
		jpanel.setLayout(new GridLayout(0,1));
		String[] columns = CompareSequence.getOneLineHeaderArray();
		JLabel label = new JLabel("Select output columns:");
		//label.setBounds(450, 90, 130, 20);
		//guiFrame.add(label);
		placeComp(label, guiFrame, 4, 3, 2, 1);
		JButton selectAll = new JButton("Select All");
		selectAll.addActionListener(this);
		//selectAll.setBounds(450, 110, 100, 20);
		//guiFrame.add(selectAll);
		JButton deselectAll = new JButton("Deselect All");
		deselectAll.addActionListener(this);
		//deselectAll.setBounds(450, 130, 100, 20);
		//guiFrame.add(deselectAll);
		JPanel jpanel2 = new JPanel();
		jpanel2.add(selectAll);
		jpanel2.add(deselectAll);
		//placeComp(selectAll, guiFrame, 4, 5, 1, 1);
		placeComp(jpanel2, guiFrame, 4, 5, 1, 1);
		
		
		//jpanel.add(selectAll);
		//jpanel.add(deselectAll);
		String[] mandatory = CompareSequence.mandatoryColumns();
		for(String column: columns) {
			JCheckBox item = new JCheckBox(column);
			item.setSelected(pm.getPropertyBoolean(column));
			item.setActionCommand("OUTPUT"+column);
			item.addActionListener(this);
			jpanel.add(item);
			outputs.add(item);
			for(String mandatoryColumn: mandatory) {
				if(column.equals(mandatoryColumn)) {
					item.setEnabled(false);
					item.setSelected(true);
					pm.setProperty(item.getText(), item.isSelected()+"");
					break;
				}
			}
		}
		JScrollPane jsp = new JScrollPane(jpanel);
		jsp.setPreferredSize( new Dimension( 200, 500 ) );
		//jsFile.setBounds(55, 90, 330, 400);
		//jsp.setBounds(450, 150, 200, 340);
		placeComp(jsp, guiFrame,4,4,1,1);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("Select All")) {
			//System.out.println("hier!");
			for(JCheckBox jcb: outputs) {
				if(jcb.isEnabled()) {
					jcb.setSelected(true);
					pm.setProperty(jcb.getText(), jcb.isSelected()+"");
				}
			}
			pm.writePropFile();
		}
		else if(e.getActionCommand().equals("Deselect All")) {
			for(JCheckBox jcb: outputs) {
				if(jcb.isEnabled()) {
					jcb.setSelected(false);
					pm.setProperty(jcb.getText(), jcb.isSelected()+"");
				}
			}
			pm.writePropFile();
		}
		else if(e.getActionCommand().startsWith("OUTPUT")) {
			if(e.getSource() instanceof JCheckBox) {
				JCheckBox jcb = (JCheckBox) e.getSource();
				pm.setProperty(jcb.getText(), jcb.isSelected()+"");
			}
		}
		else if(e.getActionCommand().equals("dirChooser")){
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setMultiSelectionEnabled(true);
			if(chooser.showOpenDialog(guiFrame) == JFileChooser.APPROVE_OPTION){
				File dir = chooser.getSelectedFile();
				if(dir.isDirectory()) {
					pm.setProperty("lastDir", dir.getAbsolutePath());
				}
				fillTable();
			}
			chooser.setMultiSelectionEnabled(false);
		}
		else if(e.getActionCommand().equals("dirChooserNGS")){
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setMultiSelectionEnabled(true);
			if(chooser.showOpenDialog(guiFrame) == JFileChooser.APPROVE_OPTION){
				File dir = chooser.getSelectedFile();
				if(dir.isDirectory()) {
					pm.setProperty("lastDir", dir.getAbsolutePath());
				}
				this.addFilesToNGSModel();
			}
			chooser.setMultiSelectionEnabled(false);
		}
		else if(e.getActionCommand().equals("fileChooserNGS")){
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.setMultiSelectionEnabled(true);
			if(chooser.showOpenDialog(guiFrame) == JFileChooser.APPROVE_OPTION){
				this.addFilesToNGSModel();
				File dir = chooser.getSelectedFile().getParentFile();
				if(dir.isDirectory()) {
					pm.setProperty("lastDir", dir.getAbsolutePath());
				}
			}
		}
		else if(e.getActionCommand().equals("fileChooser")){
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.setMultiSelectionEnabled(true);
			if(chooser.showOpenDialog(guiFrame) == JFileChooser.APPROVE_OPTION){
				File dir = chooser.getSelectedFile().getParentFile();
				if(dir.isDirectory()) {
					pm.setProperty("lastDir", dir.getAbsolutePath());
				}
				fillTable();
			}
		}
		else if(e.getActionCommand().equals("Start")){
			if(subject == null || subject.getName().equals("<Reference File>")){
				JOptionPane.showMessageDialog(guiFrame,
					    "Please select a subject Fasta file for reference first",
					    "No Fasta subject file selected",
					    JOptionPane.ERROR_MESSAGE);
				return;
			}
			//guiFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			progressBar.setMaximum(model.size()-1);
			progressBar.setValue(0);
			left.setText(left.getText().trim());
			right.setText(right.getText().trim());
			
			//sequences = Utils.fillArrayListSequences(subject);
			//hmAdditional = Utils.fillHashWithAddSequences(sequences);
			AnalyzedFileController afc = new AnalyzedFileController(pm);
			File[] files = new File[model.getSize()];
			for(int i = 0;i<model.getSize();i++) {
				files[i] = model.getElementAt(i);
				//System.out.println("adding "+i +" "+model.getElementAt(i));
			}
			afc.setFiles(files);
			afc.setLeft(left.getText());
			afc.setRight(right.getText());
			if(!afc.leftOK()) {
				JOptionPane.showMessageDialog(guiFrame,
					    "left flank cannot be found in the reference",
					    "Problem with flanks",
					    JOptionPane.ERROR_MESSAGE);
			}
			else if(!afc.rightOK()) {
				JOptionPane.showMessageDialog(guiFrame,
					    "right flank cannot be found in the reference",
					    "Problem with flanks",
					    JOptionPane.ERROR_MESSAGE);
				
			}
			else {
				//now we can save them
				saveFlanks(left.getText(), right.getText());
				
				afc.setMaxError((double)maxError.getValue());
				afc.setMaskLowQuality(maskLowQuality.isSelected());
				afc.setMaskLowQualityRemove(maskLowQualityRemove.isSelected());
				afc.setProgressBar(progressBar);
				afc.setFileChooser(chooser);
				afc.setStartButton(analyzeFiles);
				if(left.getText().length()==0 && right.getText().length()==0) {
					afc.setSplit(split.isSelected());
				}
				Thread newThread = new Thread(afc);
				newThread.start();
			}
		}
		else if(e.getActionCommand().equals("chooseSubject")){
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.setMultiSelectionEnabled(false);
			sequences = null;
			if(chooser.showOpenDialog(guiFrame) == JFileChooser.APPROVE_OPTION){
				pm.setProperty("lastDir", chooser.getSelectedFile().getParent());
				//remove the subject if we already have one
				if(subject != null){
					subject = null;
					model.remove(0);
				}
				//System.out.println("HERE");
				//System.out.println(chooser.getSelectedFile());
				//try to see if this is a Fasta file
				BufferedReader is = null;
				try {
					is = new BufferedReader(new FileReader(chooser.getSelectedFile()));
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}
				//get a SequenceDB of all sequences in the file
				SequenceIterator si = IOTools.readFastaDNA(is, null);
				if(si.hasNext()){
					try {
						Sequence s = si.nextSequence();
						subject = chooser.getSelectedFile();
						model.insertElementAt(subject, 0);
						if(si.hasNext()) {// && !mbc.tryToMatchFasta()){
							JOptionPane.showMessageDialog(guiFrame, "You selected a fasta file with two or more sequences\nI will search for templated flanks in the extra sequences.");
						}
					} catch (NoSuchElementException e1) {
						JOptionPane.showMessageDialog(guiFrame,
							    "The file "+chooser.getSelectedFile().getName()+" is not in fasta format\n"
							    		+ "please select another file",
							    "No Fasta file",
							    JOptionPane.ERROR_MESSAGE);
						e1.printStackTrace();
					} catch (BioException e1) {
						JOptionPane.showMessageDialog(guiFrame,
							    "The file "+chooser.getSelectedFile().getName()+" is not in fasta format\n"
							    		+ "please select another file",
							    "No Fasta file",
							    JOptionPane.ERROR_MESSAGE);
						e1.printStackTrace();
						
					}
				}
				if(pm.getProperty(subject.getAbsolutePath()+"_left")!= null ) {
					//only overwrite empty
					if(left.getText().length()==0) {
						left.setText(pm.getProperty(subject.getAbsolutePath()+"_left"));
					}
				}
				if(pm.getProperty(subject.getAbsolutePath()+"_right")!= null) {
					//only overwrite empty
					if(right.getText().length()==0) {
						right.setText(pm.getProperty(subject.getAbsolutePath()+"_right"));
					}
				}
			}
		}
		else if(e.getActionCommand().contentEquals("Remove sequences with remarks")) {
			pm.setProperty("printCorrectColumnsOnly", ""+removeRemarkRows.isSelected());
			
		}
		else if(e.getActionCommand().contentEquals("Run")) {
			//something to do?
			if(ngsModel.getRowCount()==0) {
				return;
			}
			//make sure flash is there
			//no longer required!
			//yes, if no assembled files are filled in
			
			//still need to make sure that all rows are ok
			Vector<NGS> v = ngsModel.getData();
			SequenceControllerThread sct = new SequenceControllerThread();
			int maxReadsInt = ((Double)maxReads.getValue()).intValue();
			int minSupportInt = ((Integer)minSupport.getValue()).intValue();
			double maxErrorDouble = ((Double)baseError.getValue()).doubleValue();
			int cores = ((Integer)cpus.getValue()).intValue();
			int tinsDistValue = ((Integer)tinsDist.getValue()).intValue();
			
			
			sct.setNGSfromGUI(v, ngsModel, guiFrame, maxReadsInt,minSupportInt,maxErrorDouble, pm.getProperty("flash"), cores, tinsDistValue);
			
			//check if requirements are met
			if(sct.isAssemblyRequired()) {
				if(pm.getProperty("flash")==null) {
					JOptionPane.showMessageDialog(guiFrame, "FLASH is not set, please set the flash executable using the set FLASH button", "FLASH not set", JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			
			Thread newThread = new Thread(sct);
			newThread.start();
		}
		else if(e.getActionCommand().contentEquals("ExcelNGS")) {
			exportToExcel();
		}
		else if(e.getActionCommand().contentEquals("SwitchMode")) {
			if(this.ab1Perspective) {
				this.switchToNGS(false);
			}
			else {
				this.switchToAB1(false);
			}
		}
		else if(e.getActionCommand().contentEquals("flash")) {
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.setMultiSelectionEnabled(false);
			if(chooser.showOpenDialog(guiFrame) == JFileChooser.APPROVE_OPTION){
				File f = chooser.getSelectedFile();
				if(f.exists()) {
					pm.setProperty("flash", f.getAbsolutePath());
					setFlashLabel();
				}
			}
		}
		else if(e.getActionCommand().contentEquals("clearTable")) {
			ngsModel.removeAll();
			ngsModel.addNGS(new NGS());
		}
		else if(e.getActionCommand().contentEquals("template")) {
			int columns = ngs.getColumnModel().getColumnCount();
			StringBuffer headerB = new StringBuffer();
			for(int i=0;i<columns;i++) {
				String header = (String) ngs.getColumnModel().getColumn(i).getHeaderValue();
				if(headerB.length()>0) {
					headerB.append("\t");
				}
				headerB.append(header.replaceAll("\\<[^>]*>",""));
			}
			JTextArea ta = new JTextArea(headerB.toString());
			//ta.setPreferredSize(new Dimension(400,200));
			JScrollPane jsp = new JScrollPane(ta);
			jsp.setPreferredSize(new Dimension(400,200));
			JOptionPane.showMessageDialog(null, jsp,"Please copy & paste and fill in",JOptionPane.INFORMATION_MESSAGE);
		}
		else if(e.getActionCommand().contentEquals("split")) {
			pm.setProperty("split", ""+split.isSelected());
		}
		else if(e.getActionCommand().contentEquals("R")) {
			if(lastSavedExcel!=null) {
				ReportPanel.runR(lastSavedExcel, false);
			}
			else {
				if(chooser.showOpenDialog(guiFrame) == JFileChooser.APPROVE_OPTION){
					File f = chooser.getSelectedFile();
					if(f.getAbsolutePath().endsWith(".xlsx")) {
						ReportPanel.runR(f, false);
					}
					else {
						JOptionPane.showMessageDialog(guiFrame,
							    "Please select an Excel file produced by SIQ",
							    "No Excel file (.xlsx) was selected",
							    JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		}
		else if(e.getActionCommand().contentEquals("Rin")) {
			if(lastSavedExcel!=null) {
				ReportPanel.runR(lastSavedExcel, true);
			}
			else {
				if(chooser.showOpenDialog(guiFrame) == JFileChooser.APPROVE_OPTION){
					File f = chooser.getSelectedFile();
					if(f.getAbsolutePath().endsWith(".xlsx")) {
						ReportPanel.runR(f, true);
					}
					else {
						JOptionPane.showMessageDialog(guiFrame,
							    "Please select an Excel file produced by SIQ",
							    "No Excel file (.xlsx) was selected",
							    JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		}
		
		System.out.println("ActionCommand: "+e.getActionCommand());
	}

	
	/*
	private String analyzeFileAll(File f, String left, String right, boolean checkLeftRight, boolean printCompare) {
		StringBuffer sb = new StringBuffer();
		RichSequence subject2 = null;
		//DISABLE TRANSLOCATION SEARCH AS IT CONFLICTS WITH SEARCHING FOR TEMPLATED FLANK INSERTIONS
		if(sequences.size()>1) {
			//subject2 = sequences.get(1);
		}

		Chromatogram chromo = null;
		try {
			chromo = ChromatogramFactory.create(f);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		//SymbolList symbols = trace.getSequence();
		String name = f.getName();
		for(RichSequence subject: sequences){
			if(checkLeftRight){
				if(left.length()>0 && right.length()>0){
					//System.out.println("hier!");
					int leftRightPos = subject.seqString().indexOf((left+right).toLowerCase());
					int leftPos = subject.seqString().indexOf(left.toLowerCase());
					int rightPos = subject.seqString().indexOf(right.toLowerCase());
					
					if(leftPos < 0 || rightPos < 0){
						JOptionPane.showConfirmDialog(guiFrame,
							    "left or right cannot be found in the fasta file"
							    		+ " please select the correct flanks!",
							    "left + right problem",
							    JOptionPane.WARNING_MESSAGE,
							    JOptionPane.OK_CANCEL_OPTION);
						System.out.println(subject.seqString());
						System.out.println((left+right).toLowerCase());
						return null;
					}
					if(subject2 == null && leftPos > rightPos){
						JOptionPane.showConfirmDialog(guiFrame,
							    "The left flank can be found, but past the right flank, which cannot be correct"
							    		+ " please select the correct flanks!",
							    "left + right problem",
							    JOptionPane.WARNING_MESSAGE,
							    JOptionPane.OK_CANCEL_OPTION);
						System.out.println(subject.seqString());
						System.out.println("leftPost:"+leftPos +":rightPos"+rightPos);
						return null;
					}
					if(subject2 == null && leftRightPos < 0) {
						JOptionPane.showConfirmDialog(guiFrame,
							    "left and right flank cannot be found connected in the fasta file."
							    + " If you are using two break sites, all is ok",
							    "left + right found, but not connected",
							    JOptionPane.WARNING_MESSAGE,
							    JOptionPane.OK_CANCEL_OPTION);
					}
				}
				//translocation, look for right in the other file
				if(subject2 != null){
					int leftPos = subject.seqString().indexOf(left.toLowerCase());
					int rightPos = subject2.seqString().indexOf(right.toLowerCase());
					if(left.length()== 0 || right.length() == 0 || (leftPos < 0 && rightPos < 0)){
						JOptionPane.showConfirmDialog(guiFrame,
							    "left and right cannot be found in the fasta file"
							    		+ " please select the correct flanks!",
							    "left + right problem",
							    JOptionPane.ERROR_MESSAGE,
							    JOptionPane.OK_CANCEL_OPTION);
						System.out.println(subject.seqString());
						System.out.println((left+right).toLowerCase());
						return null;
					}
				}
			}
			//Sequence query = new SimpleSequence(symbols, name, name, Annotation.EMPTY_ANNOTATION);
			NucleotideSequence seq = chromo.getNucleotideSequence();
			QualitySequence quals = chromo.getQualitySequence();
			RichSequence query = null;
			try {
				query = RichSequence.Tools.createRichSequence(name, DNATools.createDNA(seq.toString()));
			} catch (IllegalSymbolException e) {
				e.printStackTrace();
			}
			KMERLocation kmerl = new KMERLocation(subject.seqString());
			CompareSequence cs = new CompareSequence(subject, subject2, seq.toString(), quals, left, right, (String)pamChooser.getSelectedItem(), f.getParent(), true, name, kmerl);
			//cs.setAndDetermineCorrectRange(0.01);
			cs.setAndDetermineCorrectRange((double)maxError.getValue());
			if(this.maskLowQuality.isSelected()){
				cs.maskSequenceToHighQuality(left, right);
			}
			if(this.maskLowQualityRemove.isSelected()){
				cs.maskSequenceToHighQualityRemove();
			}
			cs.determineFlankPositions();
			cs.setAdditionalSearchString(hmAdditional);
			if(!pm.getPropertyBoolean("printCorrectColumnsOnly") || (pm.getPropertyBoolean("printCorrectColumnsOnly") && cs.getRemarks().length()==0)) {
				if(printCompare){
					String s = cs.toStringCompare(100); 
					if(s != null){
						if(sb.length()>0){
							sb.append("\n");
						}
						sb.append(s);
					}
				}
				else{
					if(sb.length()>0){
						sb.append("\n");
					}
					sb.append(cs.toStringOneLine());
				}
			}
		}
		return sb.toString();
	}
	*/


	private void exportToExcel() {
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setMultiSelectionEnabled(false);
		if(chooser.showSaveDialog(guiFrame) == JFileChooser.APPROVE_OPTION){
			File f = chooser.getSelectedFile();
			//make sure it is an .xlsx file
			System.out.println("File "+f);
			if(!f.getName().endsWith(".xlsx")) {
				f = new File(f.getAbsolutePath()+".xlsx");
				System.out.println("File "+f);
			}
			if(f.exists() && !f.renameTo(f)) {
				JOptionPane.showMessageDialog(guiFrame,"Excel file is in use, please close it and try again!");
				return;
			}
			
			exportToExcel(f);
			this.lastSavedExcel = f;
			
			//open it in Excel
			try {
				Desktop.getDesktop().open(f);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}

	private void exportToExcel(File outputFile) {
		// TODO Auto-generated method stub
		Vector<NGS> v = ngsModel.getData();
		boolean firstFile = true;
		int totalRow = 0;
		XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("rawData");
		for(NGS n: v) {
			File tempInput = n.getOutput();
			int index = 0;
			try {
				Scanner s = new Scanner(tempInput);
				while(s.hasNext()) {
					String line = s.nextLine();
					if(index == 0) {
						if(firstFile) {
							printLineToExcel(sheet, line, totalRow);
							totalRow++;
						}
					}
					else {
						printLineToExcel(sheet, line, totalRow);
						totalRow++;
					}
					index++;
				}
				s.close();
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			firstFile = false;
		}
		//write stats
		sheet = workbook.createSheet("Information");
		totalRow=0;
		printLineToExcel(sheet,"File\tType\t#Reads",totalRow++);
		for(NGS n: v) {
			
			File tempInput = n.getOutputStats();
			try {
				Scanner s = new Scanner(tempInput);
				while(s.hasNext()) {
					String line = s.nextLine();
					printLineToExcel(sheet, line, totalRow++);
				}
				s.close();
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		sheet = workbook.createSheet("RunInfo");
		totalRow=0;
		//header
		int columns = ngs.getColumnModel().getColumnCount();
		StringBuffer headerB = new StringBuffer();
		for(int i=0;i<columns;i++) {
			String header = (String) ngs.getColumnModel().getColumn(i).getHeaderValue();
			if(headerB.length()>0) {
				headerB.append("\t");
			}
			headerB.append(header.replaceAll("\\<[^>]*>",""));
		}
		printLineToExcel(sheet, headerB.toString(), totalRow++);
		//contents
		for(int row=0;row<ngs.getModel().getRowCount();row++) {
			//header is already present
			Row rowObject = sheet.createRow(row+1);
			for(int col=0;col<ngs.getModel().getColumnCount();col++) {
				Cell cell = rowObject.createCell(col);
				Object o = ngs.getModel().getValueAt(row, col);
	        	//obviously not the best method!
				if(o instanceof String) {
					cell.setCellValue((String)o);
				}
				else if(o instanceof File) {
					cell.setCellValue(((File)o).getAbsolutePath());
				}
				else if(o instanceof Double) {
					cell.setCellValue((Double)o);
				}
				else if(o instanceof Integer) {
					cell.setCellValue((Integer)o);
				}
				else if(o instanceof Float) {
					cell.setCellValue((Float)o);
				}
			}
		}
		try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            workbook.write(outputStream);
        } catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        try {
			workbook.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private void printLineToExcel(XSSFSheet sheet, String line, int rowNr) {
		 Row row = sheet.createRow(rowNr);
		 String[] parts = line.split("\t");
		 int columnCount = 0;
		 //System.out.println("rowNr: "+rowNr);
		 for(String o: parts) {
	        	Cell cell = row.createCell(columnCount++);
	        	//obviously not the best method!
	        	try {
	        		int nr = Integer.parseInt(o);
	        		cell.setCellValue(nr);
	        	}
	        	catch(Exception e) {
	        		try{
	        			double nr = Double.parseDouble(o);
	        			cell.setCellValue(nr);
	        		}
	        		catch(Exception el) {
	        			cell.setCellValue(o);
	        		}
	        	}
	        }
	}

	private void saveFlanks(String left, String right) {
		pm.setProperty(subject.getAbsolutePath()+"_left", left);
		pm.setProperty(subject.getAbsolutePath()+"_right", right);
	}

	private String analyzeFile(File f, String left, String right, boolean checkLeftRight, boolean printCompare) {
		//get a SequenceDB of all sequences in the file
		RichSequence subject = null;
		
		if(sequences.size()>0) {
			subject = sequences.get(0);
		}
		//DISABLE
		if(sequences.size()>1) {
			//subject2 = sequences.get(1);
		}
		
		Chromatogram chromo = null;
		try {
			chromo = ChromatogramFactory.create(f);
		} catch (Exception e1) {
			System.out.println(f.getName()+" gives exception");
			e1.printStackTrace();
			return f.getName()+" results in exception";
		}
		//SymbolList symbols = trace.getSequence();
		String name = f.getName();
		if(checkLeftRight){
			if(left.length()>0 && right.length()>0){
				//System.out.println("hier!");
				int leftRightPos = subject.seqString().indexOf((left+right).toLowerCase());
				int leftPos = subject.seqString().indexOf(left.toLowerCase());
				int rightPos = subject.seqString().indexOf(right.toLowerCase());
				
				if(leftPos < 0 && rightPos < 0){
					JOptionPane.showMessageDialog(guiFrame,
						    "left and right cannot be found in the fasta file"
						    		+ " please select the correct flanks!",
						    "left + right problem",
						    JOptionPane.ERROR_MESSAGE);
					System.out.println(subject.seqString());
					System.out.println((left+right).toLowerCase());
					return null;
				}
				if(leftPos > rightPos){
					JOptionPane.showMessageDialog(guiFrame,
						    "The left flank can be found, but past the right flank, which cannot be correct"
						    		+ " please select the correct flanks!",
						    "left + right problem",
						    JOptionPane.ERROR_MESSAGE);
					System.out.println(subject.seqString());
					System.out.println("leftPost:"+leftPos +":rightPos"+rightPos);
					return null;
				}
				if(leftRightPos < 0) {
					Object[] options = { "OK", "Cancel" };
					int n = JOptionPane.showOptionDialog(null, "left and right flank cannot be found connected in the fasta file."
						    + " If you are using two break sites, all is ok", "Warning",
					        JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
					        null, options, options[0]);
					if (n == JOptionPane.CANCEL_OPTION) {
						return null;
					}
				}
			}
		}
		//Sequence query = new SimpleSequence(symbols, name, name, Annotation.EMPTY_ANNOTATION);
		NucleotideSequence seq = chromo.getNucleotideSequence();
		QualitySequence quals = chromo.getQualitySequence();
		
		Subject subjectObject = new Subject(subject,left,right);
		//kmerl = null;
		CompareSequence cs = new CompareSequence(subjectObject, seq.toString(), quals, f.getParent(), true, name);
		cs.setAndDetermineCorrectRange((double)maxError.getValue());
		if(this.maskLowQuality.isSelected()){
			cs.maskSequenceToHighQuality(left, right);
		}
		if(this.maskLowQualityRemove.isSelected()){
			cs.maskSequenceToHighQualityRemove();
		}
		cs.determineFlankPositions(true);
		cs.setAdditionalSearchString(hmAdditional);
		//do we want to print it?
		if(pm.getPropertyBoolean("printCorrectColumnsOnly") && cs.getRemarks().length()>0) {
			return null;
		}
		if(printCompare){
			return cs.toStringCompare(100);
		}
		return cs.toStringOneLine();
	}
	/*
	private String analyzeFileTryToMatch(File f, String left, String right, boolean checkLeftRight, boolean printCompare) {
		
		if(mbc.tryToMatchFasta() && sequences == null ){
			sequences = Utils.fillArrayListSequences(subject);
			System.out.println("We loaded "+sequences.size()+" sequences");
		}
		Chromatogram chromo = null;
		try {
			chromo = ChromatogramFactory.create(f);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		//SymbolList symbols = trace.getSequence();
		String name = f.getName();
		
		RichSequence subject = Utils.matchNameSequence(sequences,name);
		if(subject == null){
			System.err.println("No Match could be found "+name);
			//JOptionPane.showMessageDialog(guiFrame,"Problem with match", "The file with name "+name+" could not be matched to a fasta file Name",JOptionPane.ERROR_MESSAGE);
			return null;
		}
		if(checkLeftRight){
			System.out.println("hier!");
			if(left.length()>0 && right.length()>0){
				System.out.println("hier!");
				int leftPos = subject.seqString().indexOf(left.toLowerCase());
				int rightPos = subject.seqString().indexOf(right.toLowerCase());
				
				if(leftPos < 0 || rightPos < 0){
					JOptionPane.showConfirmDialog(guiFrame,
						    "left and right cannot be found in the fasta file"
						    		+ " please select the correct flanks!",
						    "left + right problem",
						    JOptionPane.WARNING_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
					System.out.println(subject.seqString());
					System.out.println((left+right).toLowerCase());
					return null;
				}
			}
		}
		//Sequence query = new SimpleSequence(symbols, name, name, Annotation.EMPTY_ANNOTATION);
		NucleotideSequence seq = chromo.getNucleotideSequence();
		QualitySequence quals = chromo.getQualitySequence();
		KMERLocation kmerl = new KMERLocation(subject.seqString());
		CompareSequence cs = new CompareSequence(subject, null, seq.toString(), quals, left, right, (String)pamChooser.getSelectedItem(), f.getParent(), true, name, kmerl);
		if(this.maskLowQuality.isSelected()){
			cs.setAndDetermineCorrectRange((double)maxError.getValue());
			cs.maskSequenceToHighQuality(left, right);
		}
		if(this.maskLowQualityRemove.isSelected()){
			cs.setAndDetermineCorrectRange((double)maxError.getValue());
			cs.maskSequenceToHighQualityRemove();
		}
		cs.determineFlankPositions();
		cs.setAdditionalSearchString(hmAdditional);
		if(printCompare){
			return cs.toStringCompare(100);
		}
		return cs.toStringOneLine();
	}
	*/

	private void fillTable() {
		model.removeAllElements();
		if(subject != null){
			model.addElement(subject);
		}
		else {
			File stub = new File("<Reference File>");
			subject = stub;
			model.addElement(subject);
		}
		Vector<File> files = new Vector<File>();
		if(chooser.getSelectedFile().isDirectory()){
			for(File chosenFile: chooser.getSelectedFiles()) {
				for(File file: chosenFile.listFiles()){
					if(file.getName().endsWith(".ab1")){
						files.add(file);
					}
					else if(file.getName().endsWith(".fastq") || file.getName().endsWith(".fastq.gz")){
						System.out.println("adding "+file.getName());
						files.add(file);
					}
					else if(file.isDirectory()){
						files.addAll(fillTable(file));
					}
				}
			}
		}
		else{
			for(File file: chooser.getSelectedFiles()){
				if(file.getName().endsWith(".ab1")){
					files.add(file);
				}
				else if(file.getName().endsWith(".fastq") || file.getName().endsWith(".fastq.gz")){
					files.add(file);
				}
			}
		}
		if(files.size()==0) {
			JOptionPane.showMessageDialog(guiFrame, "No .ab1 or .fastq(.gz) files were found, please choose another file/directory");
		}
		boolean ab1 = false;
		boolean ngs = false;
		
		for(File f: files) {
			if(isSangerFile(f)) {
				ab1 = true;
			}
			else if(isNGSFile(f)) {
				ngs = true;
			}
		}
		//user could have selected both NGS and Sanger files
		if(ab1 && ngs) {
			 JPanel panel = new JPanel();
		        panel.add(new JLabel("Sanger and NGS files detected, which one do you want to analyze?"));
			 Object[] options1 = { "Sanger (.ab1)", "NGS (.fastq(.gz))",
             "Cancel" };
			int option = JOptionPane.showOptionDialog(guiFrame, panel, "Sanger files and NGS files detected",
	                 JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
	                null, options1, null);
			Vector<File> tempFiles = new Vector<File>();
			//Sanger
			if(option == JOptionPane.YES_OPTION) {
				for(File f: files) {
					if(isSangerFile(f)) {
						tempFiles.add(f);
					}
				}
				ab1 = true;
				ngs = false;
			}
			else if(option == JOptionPane.NO_OPTION) {
				for(File f: files) {
					if(isNGSFile(f)) {
						tempFiles.add(f);
					}
				}
				ab1 = false;
				ngs = true;
			}
			else if(option == JOptionPane.CANCEL_OPTION) {
				return;
			}
			//contains now only Sanger or NGS files
			files = tempFiles;
		}
		for(File f: files) {
			model.addElement(f);
		}
		if(ngs) {
			switchToNGS(false);
		}
		else if(ab1) {
			switchToAB1(false);
		}
	}

	private boolean isNGSFile(File f) {
		return f.getAbsolutePath().endsWith(".fastq") 
				|| f.getAbsolutePath().endsWith(".fastq.gz");
	}

	private boolean isSangerFile(File f) {
		return f.getAbsolutePath().endsWith(".ab1") && !f.getName().startsWith(".");
	}

	private void switchToAB1(boolean force) {
		if(!force && ab1Perspective) {
			return;
		}
		System.out.println("Switch to AB1! "+force);
		this.ab1Perspective = true;
		guiFrame.getContentPane().removeAll();
		guiFrame.setVisible(false);
		//switch to AB1
		//make sure the program exits when the frame closes
        guiFrame.setTitle("Tijsterman lab - SIQ "+version+" "+getMode());
        
        //This will center the JFrame in the middle of the screen
        if(force) {
        	guiFrame.setSize(1000,800);
        	guiFrame.setLocationRelativeTo(null);
        	guiFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }
        guiFrame.setLayout(new GridBagLayout());
		guiFrame.setMinimumSize(guiFrame.getSize());
		guiFrame.pack();
        
        //The first JPanel contains a JLabel and JCombobox
        //comboPanel.setLayout(new GridLayout(0,4));
        //JLabel comboLbl = new JLabel("Select subject:");
        JLabel lblSubject = new JLabel();
		//lblSubject.setBounds(91, 1, 55, 15);
		lblSubject.setText("Subject");
		lblSubject.setHorizontalAlignment(SwingConstants.CENTER);
		placeComp(lblSubject, guiFrame,0,0,1,1);
		//guiFrame.add(lblSubject);
		
		JLabel lblQuery = new JLabel();
		lblQuery.setText("Query");
		lblQuery.setHorizontalAlignment(SwingConstants.CENTER);
		//lblQuery.setBounds(275, 1, 55, 15);
		placeComp(lblQuery, guiFrame,1,0,1,1);
		//guiFrame.add(lblQuery);
		
		JLabel lblOptions = new JLabel();
		//lblOptions.setBounds(600, 1, 55, 15);
		lblOptions.setText("Flanks");
		lblOptions.setHorizontalAlignment(SwingConstants.CENTER);
		placeComp(lblOptions, guiFrame,4,0,1,1);
		//guiFrame.add(lblOptions);
        
		JLabel lblOtions = new JLabel();
		//lblOptions.setBounds(600, 1, 55, 15);
		lblOtions.setText("Options");
		lblOtions.setHorizontalAlignment(SwingConstants.CENTER);
		placeComp(lblOtions, guiFrame,6,0,1,1);
		
        JButton chooseSubject = new JButton("Select reference (.fa)");
        chooseSubject.setToolTipText("Click this button to select a valid reference fasta file. Only the first fasta sequence will be used as a reference.");
        chooseSubject.setActionCommand("chooseSubject");
        chooseSubject.addActionListener(this);
        //chooseSubject.setBounds(54, 35, 140, 25);
        //guiFrame.add(chooseSubject);
        placeComp(chooseSubject, guiFrame,0,1,1,1,0.2);
        
        JButton dirChooser = new JButton("Select directory");
        dirChooser.setToolTipText("Select a directory that contains either .ab1 or .fastq(.gz) files. Upon selection of .fastq(.gz) files, the program will switch to NGS mode. Directories will be searched recursively.");
        dirChooser.setActionCommand("dirChooser");
        dirChooser.addActionListener(this);
        //dirChooser.setBounds(212, 22, 174, 25);
        placeComp(dirChooser, guiFrame,1,1,1,1,0.2);
        placeComp(new JLabel("           "), guiFrame,2,0,1,1);
        placeComp(new JLabel("           "), guiFrame,5,0,1,1);
        //guiFrame.add(dirChooser);
        left.addActionListener(this);
         
        JButton fileChooser = new JButton("Select files (.ab1)");
        fileChooser.setToolTipText("Select one or more .ab1 or .fastq(.gz) files. Upon selection of .fastq(.gz) files, the program will switch to NGS mode.");
        fileChooser.setActionCommand("fileChooser");
        fileChooser.addActionListener(this);
        fileChooser.setBounds(212, 53, 174, 25);
        placeComp(fileChooser, guiFrame,1,2,1,1);
        //guiFrame.add(fileChooser);
        
        
        
        JLabel leftFlank = new JLabel("left flank:");
        //leftFlank.setBounds(448, 22, 55, 15);
        //left.setBounds(509, 22, 150, 21);
        //placeComp(leftFlank, guiFrame,3,1,1,1);
        left.setPreferredSize(new Dimension(150,20));
        JPanel jpanel1 = new JPanel();
        jpanel1.add(leftFlank);
        jpanel1.add(left);
        placeComp(jpanel1, guiFrame,4,1,1,1);
        //guiFrame.add(leftFlank);
        //guiFrame.add(left);
        JLabel rightFlank = new JLabel("right flank:");
        //rightFlank.setBounds(448, 53, 55, 15);
        //guiFrame.add(rightFlank);
        //right.setBounds(509, 53, 150, 21);
        //guiFrame.add(right);
        JPanel jpanel2 = new JPanel();
        jpanel2.add(rightFlank);
        jpanel2.add(right);
        right.setPreferredSize(new Dimension(150,20));
        placeComp(jpanel2, guiFrame,4,2,1,1);
        
        //maskLowQualityRemove.setBounds(680, 22, 142, 16);
        maskLowQualityRemove.setSelected(true);
        //maskLowQualityRemove.setActionCommand("maskLowQualityRemove");
        //guiFrame.add(maskLowQualityRemove);
        placeComp(maskLowQualityRemove, guiFrame,6,1,1,1);
        
        //removeRemarkRows.setBounds(680,62,200,16);
        removeRemarkRows.addActionListener(this);
        if(pm.getPropertyBoolean("printCorrectColumnsOnly")) {
        	removeRemarkRows.setSelected(pm.getPropertyBoolean("printCorrectColumnsOnly"));
        }
        //guiFrame.add(removeRemarkRows);
        placeComp(removeRemarkRows, guiFrame,6,3,1,1);
        
        split.addActionListener(this);
        split.setActionCommand("split");
        split.setToolTipText("If no flanks are selected all high quality parts of a sequence will be matched against the reference");
        if(pm.getPropertyBoolean("split")) {
        	split.setSelected(pm.getPropertyBoolean("split"));
        }
        //guiFrame.add(removeRemarkRows);
        placeComp(split, guiFrame,6,4,1,1);
        
        JPanel jpanel = new JPanel();
        SpinnerModel model = new SpinnerNumberModel(0.05, 0, 1.0, 0.01);
        maxError = new JSpinner(model);
        maxError.setPreferredSize(new Dimension(50,20));
        //maxError.setBounds(684, 40, 47, 22);
        //guiFrame.add(maxError);
        //placeComp(maxError,guiFrame,5,2,1,1);
        jpanel.add(maxError);
       
        
        JLabel lblMaxError = new JLabel();
		lblMaxError.setBounds(735, 42, 100, 15);
		lblMaxError.setText("max error (fraction)");
		jpanel.add(lblMaxError);
		placeComp(jpanel, guiFrame,6,2,1,1);
		//guiFrame.add(lblMaxError);
		
		jFiles.addMouseListener(this);
		JScrollPane jsFile = new JScrollPane(jFiles);
		jsFile.setPreferredSize( new Dimension( 200, 500 ) );
		//jsFile.setBounds(55, 90, 330, 400);
        //guiFrame.add(jsFile);
        //placeComp(jsFile, guiFrame, 0, 3, 3, 2);
        placeComp(jsFile,guiFrame,0,4,2,1,0.5);
        
        

        analyzeFiles = new JButton( "Start analysis");
        analyzeFiles.setActionCommand("Start");
        analyzeFiles.addActionListener(this);
        //analyzeFiles.setBounds(55, 500, 100, 20);
        //guiFrame.add(analyzeFiles);
        placeComp(analyzeFiles, guiFrame, 0, 5, 1, 1);
        
        progressBar = new JProgressBar(0, 500);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setVisible(true);
        //progressBar.setBounds(55, 525, 330, 20);
        placeComp(progressBar, guiFrame, 0, 6, 2, 1);
        //guiFrame.add(progressBar);
        
        JButton switchToAB1 = new JButton("Switch to NGS mode");
        switchToAB1.setActionCommand("SwitchMode");
        //switchToAB1.setBounds(500, 525, 150, 25);
        switchToAB1.addActionListener(this);
        //guiFrame.add(switchToAB1);
        placeComp(switchToAB1, guiFrame, 4, 6, 1, 1);
        
        addOutputPanel();
        //guiFrame.add(createOutputPanel());
        
        //JButton switchNGS = new JButton("Switch to FASTQ analysis");
        //switchNGS.setBounds(500, 525, 120, 20);
        //guiFrame.add(switchNGS);
        
		
        guiFrame.setVisible(true);
        
        jFiles.setCellRenderer(new DefaultListCellRenderer(){
     	   @Override
     	   public Component getListCellRendererComponent(JList<?> list,
     	         Object value, int index, boolean isSelected, boolean cellHasFocus) {
     	      if (value != null) {
     	         value = ((File)value).getName();
     	      }
     	      
     	      Component c = super.getListCellRendererComponent(list, value, index, isSelected,
     	            cellHasFocus);
     	      if(index == 0) {
     	    	c.setForeground(Color.blue);  
     	      }
     	      else {
     	    	  c.setForeground(Color.black);
     	      }
     	      return c;
     	   }
     	});
        
        if(pm.getProperty("lastDir") != null) {
        	File f = new File(pm.getProperty("lastDir"));
        	this.chooser.setCurrentDirectory(f);
        }
	}

	private String getMode() {
		if(this.ab1Perspective) {
			return "Sanger mode";
		}
		else {
			return "NGS mode"; 
		}
	}

	private void switchToNGS(boolean force) {
		if(!ab1Perspective) {
			return;
		}
		if(force) {
        	guiFrame.setSize(1000,800);
        	guiFrame.setLocationRelativeTo(null);
        	guiFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }
		System.out.println("Switch to NGS!");
		//switch to NGS
		guiFrame.getContentPane().removeAll();
		guiFrame.setVisible(false);
		guiFrame.setLayout(new GridBagLayout());
		//guiFrame.setMinimumSize(guiFrame.getSize());
		//guiFrame.pack();
		
		//System.out.println("ModeNGS "+getMode());
		//c.weightx = 0.1;
		JLabel lblQuery = new JLabel();
		lblQuery.setText("Query:");
		lblQuery.setHorizontalAlignment(SwingConstants.CENTER);
		//lblQuery.setBounds(275, 1, 55, 15);
		placeComp(lblQuery,guiFrame,0,0,2,1);
		//guiFrame.add(lblQuery,c);
		
		JButton dirChooser = new JButton("Select directory");
        dirChooser.setActionCommand("dirChooserNGS");
        dirChooser.addActionListener(this);
        //dirChooser.setMinimumSize(new Dimension(150,20));
        //dirChooser.setBounds(212, 22, 174, 25);
        placeComp(dirChooser,guiFrame,0,1,1,1);
        //guiFrame.add(dirChooser,c);
         
        JButton fileChooser = new JButton("Select files");
        fileChooser.setActionCommand("fileChooserNGS");
        fileChooser.addActionListener(this);
        //fileChooser.setBounds(212, 53, 174, 25);
        placeComp(fileChooser,guiFrame,1,1,1,1);
        
        JLabel options = new JLabel("Options:");
        options.setHorizontalAlignment(SwingConstants.CENTER);
		placeComp(options,guiFrame,5,0,2,1);
		
        JLabel maxReadsL = new JLabel("Max reads to analyze");
        maxReadsL.setToolTipText("Select the number of reads that you want to analyze. 0 means all reads will be analyzed");
        //maxReadsL.setBounds(510, 22, 120, 25);
        //guiFrame.add(maxReadsL,c);
        placeComp(maxReadsL,guiFrame,5,1,1,1);
        
        SpinnerModel model = new SpinnerNumberModel(0, 0, Double.MAX_VALUE, 100000);
        maxReads = new JSpinner(model);
        maxReads.setPreferredSize(new Dimension(80, 16));
        //maxReads.setBounds(400, 22, 100, 25);
        placeComp(maxReads,guiFrame,6,1,1,1);
        placeComp(new JLabel("           "), guiFrame,9,0,1,1);
        
        JLabel minNumberReadstoCallEvent = new JLabel("Min support");
        minNumberReadstoCallEvent.setToolTipText("Set the minimum number of reads required for an event to be included in the output");
        placeComp(minNumberReadstoCallEvent,guiFrame,5,2,1,1);
        SpinnerModel model2 = new SpinnerNumberModel(5, 0, Integer.MAX_VALUE, 1);
        minSupport = new JSpinner(model2);
        minSupport.setPreferredSize(new Dimension(80, 16));
        placeComp(minSupport,guiFrame,6,2,1,1);
        
        
        JLabel maxBaseErrro = new JLabel("Max base error");
        maxBaseErrro.setToolTipText("The maximum base error tolerated, more reads will be correct if higher, but more sequencing errors are included (default: 0.05)");
        placeComp(maxBaseErrro,guiFrame,5,3,1,1);
        SpinnerModel model3 = new SpinnerNumberModel(0.05, 0, 1, 0.01);
        baseError = new JSpinner(model3);
        baseError.setPreferredSize(new Dimension(80, 16));
        placeComp(baseError,guiFrame,6,3,1,1);
        
        //CPUs
        JLabel cpu = new JLabel("Max cpus");
        cpu.setToolTipText("The maximum number of cpus used (default: all)");
        placeComp(cpu,guiFrame,7,1,1,1);
        int cores = Runtime.getRuntime().availableProcessors(); 
        SpinnerModel model4 = new SpinnerNumberModel(cores, 1, cores, 1);
        cpus = new JSpinner(model4);
        cpus.setPreferredSize(new Dimension(80, 16));
        placeComp(cpus,guiFrame,8,1,1,1);
        
        //TINS search space
        JLabel tins = new JLabel("TINS search distance");
        tins.setToolTipText("<html>The maximum search space used to call something a Templated Insert (TINS) (default: 100)<br>"
        		+ "Both junctions will be searched in forward and reverse complement direction.<br>"
        		+ "e.g. 100 means 100 until -100 relative to the lefFlank<br>"
        		+ "and -100 until 100 relative to the rightFlank<br>"
        		+ "Note that a smaller search space may increase the number of called TINS, but could also lead to TINS not being found away from the junctions</html>");
        placeComp(tins,guiFrame,7,2,1,1);
        SpinnerModel model5 = new SpinnerNumberModel(100, 10, 1000, 1);
        tinsDist = new JSpinner(model5);
        tinsDist.setPreferredSize(new Dimension(80, 16));
        placeComp(tinsDist,guiFrame,8,2,1,1);
        
        
        //maxReadsL.setBounds(510, 22, 120, 25);
        //guiFrame.add(maxReadsL,c);
        JButton removeRows = new JButton("Clear table");
        removeRows.setActionCommand("clearTable");
        removeRows.addActionListener(this);
        placeComp(removeRows,guiFrame,0,2,1,1);
        
        
        JButton template = new JButton("Table template");
        template.setActionCommand("template");
        template.addActionListener(this);
        placeComp(template,guiFrame,1,2,1,1);
        //placeComp(new JLabel(" "),guiFrame,0,1,1,1);
        
        JLabel copyPaste = new JLabel("You can copy & paste your table below:");
        placeComp(copyPaste,guiFrame,0,3,1,1);
        
        addJTableNGS();
        
        R = new JButton("Plot with R");
        R.setActionCommand("R");
        R.addActionListener(this);
        placeComp(R, guiFrame, 4,5,1,1);

        Rin = new JButton("Plot with R (inverse)");
        Rin.setActionCommand("Rin");
        Rin.addActionListener(this);
        placeComp(Rin, guiFrame, 5,5,1,1);
        
        
        switchToAB1 = new JButton("Switch to Sanger mode");
        switchToAB1.setActionCommand("SwitchMode");
        //switchToAB1.setBounds(400, 500, 150, 25);
        switchToAB1.addActionListener(this);
        placeComp(switchToAB1,guiFrame,7,5,1,1);
        //guiFrame.add(switchToAB1,c);
        
        
        JButton setFlash = new JButton("Set FLASH executable");
        setFlash.setToolTipText("Download flash from: https://sourceforge.net/projects/flashpage/ and set the executable using this button");
        setFlash.setActionCommand("flash");
        //setFlash.setBounds(550, 500, 150, 25);
        setFlash.addActionListener(this);
        placeComp(setFlash,guiFrame,8,5,1,1);
        
        setFlashLabel();
        
        
        
        run = new JButton("Run");
        run.setActionCommand("Run");
        //run.setBounds(20,500,120,20);
        run.addActionListener(this);
        placeComp(run,guiFrame,0,5,1,1);
        
        excelNGS = new JButton("Export to Excel");
        excelNGS.setActionCommand("ExcelNGS");
        //excelNGS.setBounds(150,500,120,20);
        excelNGS.addActionListener(this);
        excelNGS.setEnabled(false);
        placeComp(excelNGS,guiFrame,1,5,1,1);
        
        this.ab1Perspective = false;
        guiFrame.setTitle("Tijsterman lab - SATL "+version+" "+getMode());
        //guiFrame.pack();
        guiFrame.setVisible(true);
	}

	private void setFlashLabel() {
		String flash = pm.getProperty("flash");
		if(flash != null) {
			//check if it still exists
			File flashFile = new File(flash);
			if(!flashFile.exists()) {
				//erase
				pm.setProperty("flash", null);
			}
			else {
				
			}
        }
	}

	private void addJTableNGS() {
		ngsModel = new NGSTableModel();
		ngs = new JTable(ngsModel) {
			//Implement table header tool tips. 
			protected String[] columnToolTips = {
					"<html>NGS file R1 containing all reads in .fastq(.gz) (optional)<br>"
					+ "if R1 and R2 are provided, you need an external application to combine<br>"
					+ "the reads. This program can launch FLASH to do just that if the executable is set</html>",
					"<html>NGS file R2 containing all reads in .fastq(.gz) (optional)<br>"
							+ "if R1 and R2 are provided, you need an external application to combine<br>"
							+ "the reads. This program can launch FLASH to do just that if the executable is set</html>",
                    "Reference file (in fasta format)",
                    "Any Alias name that you want to give to this file",
                    "the left part of the cut site (>=15nt) (case insensitive)",
                    "the right part of the cut site (>=15nt) (case insensitive)",
                    "the left primer used in your NGS experiment. Has to be present in the reference file (case insensitive)",
                    "the right primer (5'->3') used in your NGS experiment. Has to be present in the reference file (case insensitive)",
                    "events can only start at X bases from the left and right primer. This is to ensure your primers bound at the primer site. In our hands 5 is a good value",
                    "% of total reads analyzed",
                    "# of reads analyzed (orange means reads are being assembled)",
                    "# of reads correct",
                    "% of reads analyzed that are correct"};
		    protected JTableHeader createDefaultTableHeader() {
		        return new JTableHeader(columnModel) {
		            public String getToolTipText(MouseEvent e) {
		                String tip = null;
		                java.awt.Point p = e.getPoint();
		                int index = columnModel.getColumnIndexAtX(p.x);
		                int realIndex = columnModel.getColumn(index).getModelIndex();
		                return columnToolTips[realIndex];
		            }
		        };
		    }
		};
		//add Delete functionality
		InputMap inputMap = ngs.getInputMap(JComponent.WHEN_FOCUSED);
		ActionMap actionMap = ngs.getActionMap();

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete");
		actionMap.put("delete", new AbstractAction() {
		    public void actionPerformed(ActionEvent evt) {
		       // Note, you can use getSelectedRows() and/or getSelectedColumns
		       // to get all the rows/columns that have being selected
		       // and simply loop through them using the same method as
		       // described below.
		       // As is, it will only get the lead selection
		       for(int row: ngs.getSelectedRows()) {
		    	   for(int col: ngs.getSelectedColumns()) {
				       if (row >= 0 && col >= 0) {
				           row = ngs.convertRowIndexToModel(row);
				           col = ngs.convertColumnIndexToModel(col);
				           ngs.getModel().setValueAt(null, row, col);
				       }
		    	   }
		       }
		    }
		});
		
		for (int i =0; i<ngsModel.getColumnCount();i++) {
			ngs.setDefaultRenderer(ngs.getColumnClass(i), new NGSCellRenderer());
	    }
		TableCellRenderer jpb = new ProgressCellRender();
		ngs.setDefaultRenderer(ngsModel.getColumnClass("%Complete"), jpb);
		JScrollPane scrollPane = new JScrollPane(ngs);
		scrollPane.setColumnHeader(new JViewport() {
			  @Override public Dimension getPreferredSize() {
			    Dimension d = super.getPreferredSize();
			    d.height = 32;
			    return d;
			  }
			});
		//ngs.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
		//ngs.setAutoCreateRowSorter(true);
		ngs.setCellSelectionEnabled(true);
		//scrollPane.setPreferredSize( new Dimension( 800, 400 ) );
		//scrollPane.setBounds(20, 100, 950, 400);
		//scrollPane.setVerticalScrollBarPolicy(
		  //      JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		placeComp(scrollPane,guiFrame,0,4,1);
		//guiFrame.add(scrollPane);
		ExcelAdapter myAd = new ExcelAdapter(ngs);
		addFilesToNGSModel();
	}
	//TODO fix to incorporate R1 and R2 pairs
	private void addFilesToNGSModel() {
		this.ngsModel.removeAll();
		Vector<File> v = new Vector<File>();
		for(File f: chooser.getSelectedFiles()) {
			if(f.isDirectory()) {
				for(File file: f.listFiles()) {
					if(this.isNGSFile(file)) {
						v.add(file);
					}
				}
			}
			else if(this.isNGSFile(f)) {
				v.add(f);
			}
		}
		Vector<NGSPair> pairs = NGSPair.obtainPairs(v);
		if(pairs.size()>0) {
			for(NGSPair ngsPair: pairs) {
				this.ngsModel.addNGS(new NGS(ngsPair.getR1(),ngsPair.getR2()));
			}
		}
		else if(pairs.size()==0 && v.size()>0) {
			//assume that these are assembled files
			for(File f: v) {
				NGS ngs = new NGS();
				ngs.setAssembled(f);
				ngsModel.addNGS(ngs);
			}
		}
		//add dummy
		else {
			ngsModel.addNGS(NGS.getDummy());
		}
	}

	private Vector<Vector<String>> getFileVector() {
		Vector<Vector<String>> v = new Vector<Vector<String>>();
		for(File f: chooser.getSelectedFiles()) {
			Vector<String> w = new Vector<String>();
			w.add(f.getAbsolutePath());
			v.add(w);
		}
		return v;
	}

	private Vector<String> getNGSHeader() {
		Vector<String> v = new Vector<String>();
		v.add("File");
		v.add("Subject");
		v.add("Alias");
		v.add("leftFlank");
		v.add("rightFlank");
		v.add("leftPrimer");
		v.add("rightPrimer");
		v.add("minPassedPrimer");
		return v;
	}

	private Vector<File> fillTable(File dir) {
		Vector<File> v = new Vector<File>();
		if(dir.isDirectory()){
			for(File file: dir.listFiles()){
				if(this.isSangerFile(file) || this.isNGSFile(file)){
					v.add(file);
				}
				else if(file.isDirectory()){
					v.addAll(fillTable(file));
				}
			}
		}
		return v;
	}
	public void setMaxError(double e) {
		maxError.setValue(e);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if(e.getClickCount()== 2) {
			File f = ((JList<File>)e.getSource()).getSelectedValue();
			try {
				Desktop.getDesktop().open(f);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	public static void placeComp(Component comp, JFrame panel, int x, int y, int w, int h) {
	    GridBagConstraints cons = new GridBagConstraints();
	    cons.gridx = x;
	    cons.gridy = y;
	    cons.gridwidth = w;
	    cons.gridheight = h;
	    cons.fill = GridBagConstraints.HORIZONTAL;
	    cons.insets = new Insets(5,5,5,5);
	    //comp.setPreferredSize(new Dimension(250,25));
	    panel.add(comp, cons);
	 }
	public static void placeComp(Component comp, JFrame panel, int x, int y, int w, int h, double weightX) {
	    GridBagConstraints cons = new GridBagConstraints();
	    cons.gridx = x;
	    cons.gridy = y;
	    cons.gridwidth = w;
	    cons.gridheight = h;
	    cons.weightx = weightX;
	    cons.fill = GridBagConstraints.HORIZONTAL;
	    //cons.fill = GridBagConstraints.BOTH;
	    cons.insets = new Insets(5,5,5,5);
	    //comp.setPreferredSize(new Dimension(250,25));
	    panel.add(comp, cons);
	 }
	public static void placeComp(Component comp, JFrame panel, int x, int y, double weightX) {
	    GridBagConstraints cons = new GridBagConstraints();
	    cons.gridx = x;
	    cons.gridy = y;
	    cons.gridwidth = GridBagConstraints.REMAINDER;
	    cons.weighty = 0.8;
	    cons.weightx = weightX;
	    cons.fill = GridBagConstraints.BOTH;
	    //comp.setPreferredSize(new Dimension(250,25));
	    panel.add(comp, cons);
	 }
}