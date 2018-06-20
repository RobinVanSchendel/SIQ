package gui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.SequenceIterator;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequence.IOTools;
import org.biojavax.bio.seq.RichSequenceIterator;
import org.jcvi.jillion.core.qual.QualitySequence;
import org.jcvi.jillion.core.residue.nt.NucleotideSequence;
import org.jcvi.jillion.trace.chromat.Chromatogram;
import org.jcvi.jillion.trace.chromat.ChromatogramFactory;

import utils.CompareSequence;
import utils.Utils;

public class GUI implements ActionListener {
	//JFileChooser chooser = new JFileChooser(new File("C:\\Users\\rvanschendel\\Documents\\Project_Joost"));
	JFileChooser chooser = new JFileChooser();
	JFrame guiFrame = new JFrame();
	DefaultListModel<File> model = new DefaultListModel<File>();
	JList<File> jFiles = new JList<File>(model);
	File subject;
	//JTextField left = new JTextField("ttaggcacatgacccgtgtttcctcac");
	//JTextField left = new JTextField("GCATGCGTCGACCCgggaggcctgatttca");
	JTextField left = new JTextField("");
	//JTextField right = new JTextField("cagtggtgtaaatgctggtccatggct");
	//JTextField right = new JTextField("CCCCCCCCTCCCCCACCCCCTCCCtcgcAATT");
	JTextField right = new JTextField("");
	JComboBox<String> pamChooser;
	JCheckBox maskLowQuality = new JCheckBox("maskLowQuality");
	JCheckBox maskLowQualityRemove = new JCheckBox("maskLowQualityRemove");
	private MenuBarCustom mbc;
	private ArrayList<RichSequence> sequences;
	JProgressBar progressBar;
	
	public GUI()
    {
        //make sure the program exits when the frame closes
        guiFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        guiFrame.setTitle("Robin's Sanger Sequence Analyzer");
        guiFrame.setSize(1200,600);
      
        //This will center the JFrame in the middle of the screen
        guiFrame.setLocationRelativeTo(null);
        
        //The first JPanel contains a JLabel and JCombobox
        final JPanel comboPanel = new JPanel();
        JLabel comboLbl = new JLabel("Subject:");
        JButton chooseSubject = new JButton("Seq (Fasta)");
        chooseSubject.setActionCommand("chooseSubject");
        chooseSubject.addActionListener(this);
        
        comboPanel.add(comboLbl);
        
        comboPanel.add(chooseSubject);
        JPanel test = new JPanel();
        left.setPreferredSize(new Dimension(100,20));
        right.setPreferredSize(new Dimension(100,20));
        test.add(new JLabel("leftFlank"));
        test.add(left);
        test.add(new JLabel("rightFlank"));
        test.add(right);
        comboPanel.add(test);
        
        comboPanel.add(new JLabel("Query:"));
        
        
        //Create the second JPanel. Add a JLabel and JList and
        //make use the JPanel is not visible.
        final JPanel listPanel = new JPanel();
        listPanel.setVisible(false);
          
        JButton analyzeFiles = new JButton( "Start analysis");
        analyzeFiles.setActionCommand("Start");
        analyzeFiles.addActionListener(this);
        
        final JPanel buttonPanel = new JPanel();
        buttonPanel.setSize(guiFrame.getWidth(), 400);
        buttonPanel.setLayout(new BorderLayout());
        buttonPanel.add(analyzeFiles);
        progressBar = new JProgressBar(0, 50);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        buttonPanel.add(progressBar, BorderLayout.CENTER);
        
        //analyzeFiles = new JButton( "Start analysis Print Comparison");
        //analyzeFiles.setActionCommand("Start Comparison");
        //analyzeFiles.addActionListener(this);
        buttonPanel.add(analyzeFiles, BorderLayout.NORTH);
        
        
        JButton dirChooser = new JButton("Dir with .ab1 files");
        dirChooser.setActionCommand("dirChooser");
        dirChooser.addActionListener(this);
        comboPanel.add(dirChooser);
        
        comboPanel.add(new JLabel("or")); 
        JButton fileChooser = new JButton(".ab1 files");
        fileChooser.setActionCommand("fileChooser");
        fileChooser.addActionListener(this);
        comboPanel.add(fileChooser);
        
        //turn off the PAM chooser for now
        pamChooser = new JComboBox<String>();
        //comboPanel.add(new JLabel("select PAM site:"));
        //comboPanel.add(pamChooser);
        
        maskLowQuality.setSelected(false); 
        comboPanel.add(maskLowQuality);
        
        maskLowQualityRemove.setSelected(true); 
        comboPanel.add(maskLowQualityRemove);
        
        //The JFrame uses the BorderLayout layout manager.
        //Put the two JPanels and JButton in different areas.
        guiFrame.add(comboPanel, BorderLayout.NORTH);
        //guiFrame.add(listPanel, BorderLayout.CENTER);
        guiFrame.add(buttonPanel,BorderLayout.SOUTH);
        
        guiFrame.add(new JScrollPane(jFiles), BorderLayout.CENTER);
        mbc = new MenuBarCustom();
        guiFrame.setJMenuBar(mbc.getMenuBar());
        
        //make sure the JFrame is visible
        guiFrame.setVisible(true);
        guiFrame.setExtendedState(guiFrame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        
        //File tempFile = new File("C:/Users/rvanschendel/Documents/Project_Primase");
        File tempFile = new File("Z:/Robin/Project_Primase/UV_TMP_hus-1-vs-N2/unc-22_sequencing");
        if(tempFile.exists() && tempFile.isDirectory()){
        	this.chooser.setCurrentDirectory(tempFile);
        }
    }

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("dirChooser")){
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setMultiSelectionEnabled(true);
			if(chooser.showOpenDialog(guiFrame) == JFileChooser.APPROVE_OPTION){
				fillTable();
			}
			chooser.setMultiSelectionEnabled(false);
		}
		else if(e.getActionCommand().equals("fileChooser")){
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.setMultiSelectionEnabled(true);
			if(chooser.showOpenDialog(guiFrame) == JFileChooser.APPROVE_OPTION){
				fillTable();
			}
		}
		else if(e.getActionCommand().equals("Start")){
			if(subject == null){
				JOptionPane.showMessageDialog(guiFrame,
					    "Please select a subject Fasta file for reference first",
					    "No Fasta subject file selected",
					    JOptionPane.ERROR_MESSAGE);
				return;
			}
			JTextArea area = new JTextArea(CompareSequence.getOneLineHeader()+"\n");
			area.setColumns(30);
			 
			guiFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			boolean check = true;
			progressBar.setMaximum(model.size()-1);
			progressBar.setValue(0);
			for(int i = 1; i<model.size();i++){
				String ret = null;
				if(mbc.tryToMatchFasta()){
					ret = analyzeFileTryToMatch(model.getElementAt(i), left.getText(), right.getText(), check, false);
				}
				else if(mbc.tryAllFasta()){
					ret = analyzeFileAll(model.getElementAt(i), left.getText(), right.getText(), check, false);
				}
				else{
					ret = analyzeFile(model.getElementAt(i), left.getText(), right.getText(), check, false);
				}
				check = false;
				//at least show the name
				if(ret == null){
					ret = model.getElementAt(i).getName();
				}
				area.append(ret+"\n");
				progressBar.setValue(i);
				progressBar.update(progressBar.getGraphics());
				guiFrame.update(guiFrame.getGraphics());
			}
			guiFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			JScrollPane scrollPane = new JScrollPane(area);
			scrollPane.setPreferredSize( new Dimension( 500, 500 ) );
			JOptionPane.showMessageDialog(
					   null, scrollPane, "Result", JOptionPane.PLAIN_MESSAGE);
		}
		//disabled
		/*
		else if(e.getActionCommand().equals("Start Comparison")){
			if(subject == null){
				JOptionPane.showMessageDialog(guiFrame,
					    "Please select a subject Fasta file for reference first",
					    "No Fasta subject file selected",
					    JOptionPane.ERROR_MESSAGE);
				return;
			}
			JTextArea area = new JTextArea(CompareSequence.getOneLineHeader()+"\n");
			area.setColumns(30);
			 
			guiFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			boolean check = true;
			for(int i = 1; i<model.size();i++){
				String ret = null;
				if(mbc.tryToMatchFasta()){
					ret = analyzeFileTryToMatch(model.getElementAt(i), left.getText(), right.getText(), check, true);
				}
				else if(mbc.tryAllFasta()){
					ret = analyzeFileAll(model.getElementAt(i), left.getText(), right.getText(), check, true);
				}
				else{
					ret = analyzeFile(model.getElementAt(i), left.getText(), right.getText(), check, true);
				}
				check = false;
				//at least show the name
				if(ret == null){
					ret = model.getElementAt(i).getName();
				}
				area.append(ret);
				if(ret != null && ret.length()>0){
					area.append("\n");
				}
			}
			guiFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			JScrollPane scrollPane = new JScrollPane(area);
			scrollPane.setPreferredSize( new Dimension( 500, 500 ) );
			JOptionPane.showMessageDialog(
					   null, scrollPane, "Result", JOptionPane.PLAIN_MESSAGE);
		}
		*/
		else if(e.getActionCommand().equals("chooseSubject")){
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.setMultiSelectionEnabled(false);
			sequences = null;
			if(chooser.showOpenDialog(guiFrame) == JFileChooser.APPROVE_OPTION){
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
						System.out.println(s.getName());
						fillInPamSite(s.seqString().toString());
						subject = chooser.getSelectedFile();
						model.insertElementAt(subject, 0);
						if(si.hasNext() && !mbc.tryToMatchFasta()){
							JOptionPane.showMessageDialog(guiFrame, "You selected a fasta file with two sequences\n"+s.getName()+"\n"+si.nextSequence().getName()+"\nI will search for translocations");
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
			}
		}
	}

	private String analyzeFileAll(File f, String left, String right, boolean checkLeftRight, boolean printCompare) {
		StringBuffer sb = new StringBuffer();
		BufferedReader is = null;
		try {
			is = new BufferedReader(new FileReader(subject));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		//get a SequenceDB of all sequences in the file
		RichSequenceIterator si = IOTools.readFastaDNA(is, null);
		Vector<RichSequence> subjects = new Vector<RichSequence>();
		RichSequence subject2 = null;
		
		while(si.hasNext()){
			try {
				subjects.add(si.nextRichSequence());
			} catch (NoSuchElementException e) {
				e.printStackTrace();
			} catch (BioException e) {
				e.printStackTrace();
			}
			
		}
		
		Chromatogram chromo = null;
		try {
			chromo = ChromatogramFactory.create(f);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		//SymbolList symbols = trace.getSequence();
		String name = f.getName();
		for(RichSequence subject: subjects){
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
			CompareSequence cs = new CompareSequence(subject, subject2, query, quals, left, right, (String)pamChooser.getSelectedItem(), f.getParent());
			//cs.setAndDetermineCorrectRange(0.01);
			cs.setAndDetermineCorrectRange(0.05);
			if(this.maskLowQuality.isSelected()){
				cs.maskSequenceToHighQuality(left, right);
			}
			if(this.maskLowQualityRemove.isSelected()){
				cs.maskSequenceToHighQualityRemove();
			}
			cs.determineFlankPositions();
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
		return sb.toString();
	}

	private void fillInPamSite(String string) {
		if(pamChooser != null) {
			pamChooser.removeAll();
			//find all pamSites
			Pattern p = Pattern.compile("[acgt]{21}gg");  // insert your pattern here
			//string = string.toLowerCase();
			Matcher m = p.matcher(string);
			int lastIndex = 0;
			while (m.find(lastIndex)) {
				pamChooser.addItem(m.group()+":"+(m.start()));
				lastIndex = m.start()+1;
			}
			p = Pattern.compile("cc[acgt]{21}");  // insert your pattern here
			m = p.matcher(string);
			lastIndex = 0;
			while (m.find(lastIndex)) {
				pamChooser.addItem(m.group()+":"+(m.end()));
				lastIndex = m.start()+1;
			}
			pamChooser.setSelectedItem(null);
		}
	}

	private String analyzeFile(File f, String left, String right, boolean checkLeftRight, boolean printCompare) {
		BufferedReader is = null;
		try {
			is = new BufferedReader(new FileReader(subject));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		//get a SequenceDB of all sequences in the file
		RichSequenceIterator si = IOTools.readFastaDNA(is, null);
		RichSequence subject = null;
		RichSequence subject2 = null;
		
		while(si.hasNext()){
			try {
				if(subject == null){
					subject = si.nextRichSequence();
				}
				if(subject2 == null && si.hasNext()){
					subject2 = si.nextRichSequence();
				}
			} catch (NoSuchElementException e) {
				e.printStackTrace();
			} catch (BioException e) {
				e.printStackTrace();
			}
			
		}
		
		Chromatogram chromo = null;
		try {
			chromo = ChromatogramFactory.create(f);
		} catch (Exception e1) {
			System.out.println(f.getName()+" gives exception");
			e1.printStackTrace();
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
				if(subject2 == null && leftPos > rightPos){
					JOptionPane.showMessageDialog(guiFrame,
						    "The left flank can be found, but past the right flank, which cannot be correct"
						    		+ " please select the correct flanks!",
						    "left + right problem",
						    JOptionPane.ERROR_MESSAGE);
					System.out.println(subject.seqString());
					System.out.println("leftPost:"+leftPos +":rightPos"+rightPos);
					return null;
				}
				if(subject2 == null && leftRightPos < 0) {
					JOptionPane.showMessageDialog(guiFrame,
						    "left and right flank cannot be found connected in the fasta file."
						    + " If you are using two break sites, all is ok",
						    "left + right found, but not connected",
						    JOptionPane.WARNING_MESSAGE);
				}
			}
			//translocation, look for right in the other file
			if(subject2 != null){
				int leftPos = subject.seqString().indexOf(left.toLowerCase());
				int rightPos = subject2.seqString().indexOf(right.toLowerCase());
				if(left.length()== 0 || right.length() == 0 || (leftPos < 0 && rightPos < 0)){
					JOptionPane.showMessageDialog(guiFrame,
						    "left and right cannot be found in the fasta file"
						    		+ " please select the correct flanks!",
						    "left + right problem",
						    JOptionPane.ERROR_MESSAGE);
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
		CompareSequence cs = new CompareSequence(subject, subject2, query, quals, left, right, (String)pamChooser.getSelectedItem(), f.getParent());
		cs.setAndDetermineCorrectRange(0.05);
		if(this.maskLowQuality.isSelected()){
			cs.maskSequenceToHighQuality(left, right);
		}
		if(this.maskLowQualityRemove.isSelected()){
			cs.maskSequenceToHighQualityRemove();
		}
		cs.determineFlankPositions();
		if(printCompare){
			return cs.toStringCompare(100);
		}
		return cs.toStringOneLine();
	}
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
		RichSequence query = null;
		try {
			query = RichSequence.Tools.createRichSequence(name, DNATools.createDNA(seq.toString()));
		} catch (IllegalSymbolException e) {
			e.printStackTrace();
		}
		CompareSequence cs = new CompareSequence(subject, null, query, quals, left, right, (String)pamChooser.getSelectedItem(), f.getParent());
		if(this.maskLowQuality.isSelected()){
			cs.setAndDetermineCorrectRange(0.05);
			cs.maskSequenceToHighQuality(left, right);
		}
		if(this.maskLowQualityRemove.isSelected()){
			cs.setAndDetermineCorrectRange(0.05);
			cs.maskSequenceToHighQualityRemove();
		}
		cs.determineFlankPositions();
		if(printCompare){
			return cs.toStringCompare(100);
		}
		return cs.toStringOneLine();
	}

	private void fillTable() {
		model.removeAllElements();
		if(subject != null){
			model.addElement(subject);
		}
		if(chooser.getSelectedFile().isDirectory()){
			for(File chosenFile: chooser.getSelectedFiles()) {
				for(File file: chosenFile.listFiles()){
					if(file.getName().endsWith(".ab1")){
						model.addElement(file);
					}
					else if(file.isDirectory()){
						fillTable(file);
					}
				}
			}
		}
		else{
			for(File f: chooser.getSelectedFiles()){
				model.addElement(f);
			}
		}
	}

	private void fillTable(File dir) {
		if(dir.isDirectory()){
			for(File file: dir.listFiles()){
				if(file.getName().endsWith(".ab1")){
					model.addElement(file);
				}
				else if(file.isDirectory()){
					fillTable(file);
				}
			}
		}
	}
}