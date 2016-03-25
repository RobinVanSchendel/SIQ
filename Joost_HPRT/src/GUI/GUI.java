package GUI;

import java.awt.BorderLayout;
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
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.biojava.bio.Annotation;
import org.biojava.bio.BioException;
import org.biojava.bio.program.abi.ABITrace;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.SequenceIterator;
import org.biojava.bio.seq.impl.SimpleSequence;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SymbolList;
import org.biojavax.bio.seq.RichSequence.IOTools;
import org.jcvi.jillion.core.qual.QualitySequence;
import org.jcvi.jillion.core.residue.nt.NucleotideSequence;
import org.jcvi.jillion.trace.chromat.Chromatogram;
import org.jcvi.jillion.trace.chromat.ChromatogramFactory;

import Utils.CompareSequence;
import Utils.Utils;

public class GUI implements ActionListener {
	//JFileChooser chooser = new JFileChooser(new File("C:\\Users\\rvanschendel\\Documents\\Project_Joost"));
	JFileChooser chooser = new JFileChooser();
	JFrame guiFrame = new JFrame();
	DefaultListModel<File> model = new DefaultListModel<File>();
	JList<File> jFiles = new JList<File>(model);
	File subject;
	//JTextField left = new JTextField("ttaggcacatgacccgtgtttcctcac");
	JTextField left = new JTextField("GCATGCGTCGACCCgggaggcctgatttca");
	//JTextField right = new JTextField("cagtggtgtaaatgctggtccatggct");
	JTextField right = new JTextField("CCCCCCCCTCCCCCACCCCCTCCCtcgcAATT");
	JComboBox<String> pamChooser;
	JCheckBox maskLowQuality = new JCheckBox("maskLowQuality");
	JCheckBox maskLowQualityRemove = new JCheckBox("maskLowQualityRemove");
	private MenuBarCustom mbc;
	private ArrayList<Sequence> sequences;
	
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
        
        JButton dirChooser = new JButton("Dir with .ab1 files");
        dirChooser.setActionCommand("dirChooser");
        dirChooser.addActionListener(this);
        comboPanel.add(dirChooser);
        
        comboPanel.add(new JLabel("or")); 
        JButton fileChooser = new JButton(".ab1 files");
        fileChooser.setActionCommand("fileChooser");
        fileChooser.addActionListener(this);
        comboPanel.add(fileChooser);
        
        pamChooser = new JComboBox<String>();
        comboPanel.add(new JLabel("select PAM site:"));
        comboPanel.add(pamChooser);
        
        maskLowQuality.setSelected(false); 
        comboPanel.add(maskLowQuality);
        
        maskLowQualityRemove.setSelected(false); 
        comboPanel.add(maskLowQualityRemove);
        
        //The JFrame uses the BorderLayout layout manager.
        //Put the two JPanels and JButton in different areas.
        guiFrame.add(comboPanel, BorderLayout.NORTH);
        //guiFrame.add(listPanel, BorderLayout.CENTER);
        guiFrame.add(analyzeFiles,BorderLayout.SOUTH);
        
        guiFrame.add(new JScrollPane(jFiles), BorderLayout.CENTER);
        mbc = new MenuBarCustom();
        guiFrame.setJMenuBar(mbc.getMenuBar());
        
        //make sure the JFrame is visible
        guiFrame.setVisible(true);
        guiFrame.setExtendedState(guiFrame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        
        File tempFile = new File("C:/Users/rvanschendel/Documents/Project_Primase");
        if(tempFile.exists() && tempFile.isDirectory()){
        	this.chooser.setCurrentDirectory(tempFile);
        }
    }

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("dirChooser")){
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setMultiSelectionEnabled(false);
			if(chooser.showOpenDialog(guiFrame) == JFileChooser.APPROVE_OPTION){
				fillTable();
			}
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
			 
			for(int i = 1; i<model.size();i++){
				String ret = null;
				if(mbc.tryToMatchFasta()){
					ret = analyzeFileTryToMatch(model.getElementAt(i), left.getText(), right.getText(), true);
				}
				else{
					ret = analyzeFile(model.getElementAt(i), left.getText(), right.getText(), true);
				}
				//at least show the name
				if(ret == null){
					ret = model.getElementAt(i).getName();
				}
				area.append(ret+"\n");
			}
			JScrollPane scrollPane = new JScrollPane(area);
			scrollPane.setPreferredSize( new Dimension( 500, 500 ) );
			JOptionPane.showMessageDialog(
					   null, scrollPane, "Result", JOptionPane.PLAIN_MESSAGE);
		}
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
				boolean hasTwoSequences = false;
				if(si.hasNext()){
					try {
						Sequence s = si.nextSequence();
						System.out.println(s.getName());
						fillInPamSite(s.seqString().toString());
						subject = chooser.getSelectedFile();
						model.insertElementAt(subject, 0);
						if(si.hasNext() && !mbc.tryToMatchFasta()){
							hasTwoSequences = true;
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

	private void fillInPamSite(String string) {
		pamChooser.removeAll();
		//find all pamSites
		Pattern p = Pattern.compile("[acgt]{21}gg");  // insert your pattern here
		Matcher m = p.matcher(string);
		while (m.find()) {
			pamChooser.addItem(m.group()+":"+(m.start()-3));
		}
		p = Pattern.compile("cc[acgt]{21}");  // insert your pattern here
		m = p.matcher(string);
		while (m.find()) {
			pamChooser.addItem(m.group()+":"+(m.end()+3));
		}
		pamChooser.setSelectedItem(null);
		
	}

	private String analyzeFile(File f, String left, String right, boolean checkLeftRight) {
		BufferedReader is = null;
		try {
			is = new BufferedReader(new FileReader(subject));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		//get a SequenceDB of all sequences in the file
		SequenceIterator si = IOTools.readFastaDNA(is, null);
		Sequence subject = null;
		Sequence subject2 = null;
		
		while(si.hasNext()){
			try {
				if(subject == null){
					subject = si.nextSequence();
				}
				if(subject2 == null && si.hasNext()){
					subject2 = si.nextSequence();
				}
			} catch (NoSuchElementException e) {
				e.printStackTrace();
			} catch (BioException e) {
				e.printStackTrace();
			}
			
		}
		
		ABITrace trace = null;
		Chromatogram chromo = null;
		try {
			chromo = ChromatogramFactory.create(f);
		} catch (IOException e1) {
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
					System.out.println("error!");
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
		Sequence query = null;
		try {
			query = DNATools.createDNASequence(seq.toString(), name);
		} catch (IllegalSymbolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		CompareSequence cs = new CompareSequence(subject, subject2, query, quals, left, right, (String)pamChooser.getSelectedItem());
		cs.setAndDetermineCorrectRange(0.05);
		if(this.maskLowQuality.isSelected()){
			cs.maskSequenceToHighQuality(left, right);
		}
		if(this.maskLowQualityRemove.isSelected()){
			cs.maskSequenceToHighQualityRemove(left, right);
		}
		cs.determineFlankPositions();
		return cs.toStringOneLine();
	}
	private String analyzeFileTryToMatch(File f, String left, String right, boolean checkLeftRight) {
		
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
		
		Sequence subject = Utils.matchNameSequence(sequences,name);
		if(subject == null){
			System.err.println("No Match could be found "+name);
			//JOptionPane.showMessageDialog(guiFrame,"Problem with match", "The file with name "+name+" could not be matched to a fasta file Name",JOptionPane.ERROR_MESSAGE);
			return null;
		}
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
			}
		}
		//Sequence query = new SimpleSequence(symbols, name, name, Annotation.EMPTY_ANNOTATION);
		NucleotideSequence seq = chromo.getNucleotideSequence();
		QualitySequence quals = chromo.getQualitySequence();
		Sequence query = null;
		try {
			query = DNATools.createDNASequence(seq.toString(), name);
		} catch (IllegalSymbolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		CompareSequence cs = new CompareSequence(subject, null, query, quals, left, right, (String)pamChooser.getSelectedItem());
		cs.setAndDetermineCorrectRange(0.05);
		if(this.maskLowQuality.isSelected()){
			cs.maskSequenceToHighQuality(left, right);
		}
		if(this.maskLowQualityRemove.isSelected()){
			cs.maskSequenceToHighQualityRemove(left, right);
		}
		cs.determineFlankPositions();
		return cs.toStringOneLine();
	}

	private void fillTable() {
		model.removeAllElements();
		if(subject != null){
			model.addElement(subject);
		}
		if(chooser.getSelectedFile().isDirectory()){
			for(File file: chooser.getSelectedFile().listFiles()){
				if(file.getName().endsWith(".ab1")){
					model.addElement(file);
				}
				else if(file.isDirectory()){
					fillTable(file);
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