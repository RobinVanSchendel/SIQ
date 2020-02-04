package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
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
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumnModel;

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

import dnaanalysis.Utils;
import utils.AnalyzedFileController;
import utils.CompareSequence;
import utils.KMERLocation;
import utils.Subject;

public class GUI implements ActionListener, MouseListener {
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
	JCheckBox maskLowQuality = new JCheckBox("maskLowQuality");
	JCheckBox maskLowQualityRemove = new JCheckBox("maskLowQualityRemove");
	JCheckBox removeRemarkRows = new JCheckBox("Remove sequences with remarks");
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
	
	
	@SuppressWarnings("serial")
	public GUI(String version, PropertiesManager pm)
    {
		this.version = version;
		this.pm = pm;
		switchToAB1(true);
		return;
		/*
		//make sure the program exits when the frame closes
        guiFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        guiFrame.setTitle("Sanger Sequence Analyzer "+version+" - Tijsterman Lab");
        guiFrame.setSize(1000,600);
      
        //This will center the JFrame in the middle of the screen
        guiFrame.setLocationRelativeTo(null);
        guiFrame.setLayout(null);
        
        //The first JPanel contains a JLabel and JCombobox
        //comboPanel.setLayout(new GridLayout(0,4));
        //JLabel comboLbl = new JLabel("Select subject:");
        JLabel lblSubject = new JLabel();
		lblSubject.setBounds(91, 1, 55, 15);
		lblSubject.setText("Subject");
		guiFrame.add(lblSubject);
		
		JLabel lblQuery = new JLabel();
		lblQuery.setText("Query");
		lblQuery.setBounds(275, 1, 55, 15);
		guiFrame.add(lblQuery);
		
		JLabel lblOptions = new JLabel();
		lblOptions.setBounds(600, 1, 55, 15);
		lblOptions.setText("Options");
		guiFrame.add(lblOptions);
        
        JButton chooseSubject = new JButton("Select subject (.fa)");
        chooseSubject.setActionCommand("chooseSubject");
        chooseSubject.addActionListener(this);
        chooseSubject.setBounds(54, 35, 140, 25);
        guiFrame.add(chooseSubject);
        this.pm = pm;
        
        JButton dirChooser = new JButton("Select directory");
        dirChooser.setActionCommand("dirChooser");
        dirChooser.addActionListener(this);
        dirChooser.setBounds(212, 22, 174, 25);
        guiFrame.add(dirChooser);
        
         
        JButton fileChooser = new JButton("Select files");
        fileChooser.setActionCommand("fileChooser");
        fileChooser.addActionListener(this);
        fileChooser.setBounds(212, 53, 174, 25);
        guiFrame.add(fileChooser);
        
        
        
        JLabel leftFlank = new JLabel("leftFlank:");
        leftFlank.setBounds(448, 22, 55, 15);
        left.setBounds(509, 22, 150, 21);
        guiFrame.add(leftFlank);
        guiFrame.add(left);
        JLabel rightFlank = new JLabel("rightFlank:");
        rightFlank.setBounds(448, 53, 55, 15);
        guiFrame.add(rightFlank);
        right.setBounds(509, 53, 150, 21);
        guiFrame.add(right);
        
        maskLowQualityRemove.setBounds(680, 22, 142, 16);
        maskLowQualityRemove.setSelected(true);
        guiFrame.add(maskLowQualityRemove);
        
        removeRemarkRows.setBounds(680,62,200,16);
        removeRemarkRows.addActionListener(this);
        if(pm.getPropertyBoolean("printCorrectColumnsOnly")) {
        	removeRemarkRows.setSelected(pm.getPropertyBoolean("printCorrectColumnsOnly"));
        }
        guiFrame.add(removeRemarkRows);
        
        
        SpinnerModel model = new SpinnerNumberModel(0.05, 0, 1.0, 0.01);
        maxError = new JSpinner(model);
        maxError.setPreferredSize(new Dimension(50,20));
        maxError.setBounds(684, 40, 47, 22);
        guiFrame.add(maxError);
        
        JLabel lblMaxError = new JLabel();
		lblMaxError.setBounds(735, 42, 100, 15);
		lblMaxError.setText("max error (fraction)");
		guiFrame.add(lblMaxError);
		
		jFiles.addMouseListener(this);
		JScrollPane jsFile = new JScrollPane(jFiles);
		jsFile.setBounds(55, 90, 330, 400);
        guiFrame.add(jsFile);
        
        analyzeFiles = new JButton( "Start analysis");
        analyzeFiles.setActionCommand("Start");
        analyzeFiles.addActionListener(this);
        analyzeFiles.setBounds(55, 500, 100, 20);
        guiFrame.add(analyzeFiles);
        
        progressBar = new JProgressBar(0, 500);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setVisible(true);
        progressBar.setBounds(55, 525, 330, 20);
        guiFrame.add(progressBar);
        
        guiFrame.add(createOutputPanel());
        
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
        /*
        
        //Create the second JPanel. Add a JLabel and JList and
        //make use the JPanel is not visible.
        final JPanel listPanel = new JPanel();
        listPanel.setVisible(false);
          
        analyzeFiles = new JButton( "Start analysis");
        analyzeFiles.setActionCommand("Start");
        analyzeFiles.addActionListener(this);
        
        //final JPanel buttonPanel = new JPanel();
        //buttonPanel.setSize(guiFrame.getWidth(), 400);
        //buttonPanel.setLayout(new BorderLayout());
        guiFrame.add(analyzeFiles);
        //buttonPanel.add(analyzeFiles);
        progressBar = new JProgressBar(0, 500);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setVisible(true);
        //buttonPanel.add(progressBar, BorderLayout.CENTER);
        guiFrame.add(progressBar);
        Thread t = new Thread(new Runnable() {
            public void run() {
            	guiFrame.setVisible(true);
            }
         });
         t.start();
        
        
        //analyzeFiles = new JButton( "Start analysis Print Comparison");
        //analyzeFiles.setActionCommand("Start Comparison");
        //analyzeFiles.addActionListener(this);
        //buttonPanel.add(analyzeFiles, BorderLayout.NORTH);
        
        
        JButton dirChooser = new JButton("Select directory with .ab1 files");
        dirChooser.setActionCommand("dirChooser");
        dirChooser.addActionListener(this);
        JPanel jpanel = new JPanel();
        jpanel.setLayout(new GridLayout(2,0));
        jpanel.add(dirChooser);
        
         
        JButton fileChooser = new JButton("Select .ab1 files");
        fileChooser.setActionCommand("fileChooser");
        fileChooser.addActionListener(this);
        jpanel.add(fileChooser);
        //comboPanel.add(jpanel);
        //comboPanel.add(test);
        
        //turn off the PAM chooser for now
        //comboPanel.add(new JLabel("select PAM site:"));
        //comboPanel.add(pamChooser);
        
        maskLowQuality.setSelected(false);
        //do not show this option
        //comboPanel.add(maskLowQuality);
        
        maskLowQualityRemove.setSelected(true); 
        //comboPanel.add(maskLowQualityRemove);
        
        SpinnerModel model = new SpinnerNumberModel(0.05, 0, 1.0, 0.01);
        maxError = new JSpinner(model);
        maxError.setPreferredSize(new Dimension(50,20));
        //comboPanel.add(maxE);
        //comboPanel.add(maxError);
        
        //The JFrame uses the BorderLayout layout manager.
        //Put the two JPanels and JButton in different areas.
        //JScrollPane top = new JScrollPane(comboPanel);
        //top.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        //guiFrame.add(top, BorderLayout.NORTH);
        //guiFrame.add(listPanel, BorderLayout.CENTER);
        //guiFrame.add(buttonPanel,BorderLayout.SOUTH);
        
        
        
        
        
        mbc = new MenuBarCustom(pm);
        guiFrame.setJMenuBar(mbc.getMenuBar());
        
        //make sure the JFrame is visible
        
        //guiFrame.setExtendedState(guiFrame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
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
        
        
        //File tempFile = new File("C:/Users/rvanschendel/Documents/Project_Primase");
        if(pm.getProperty("lastDir") != null) {
        	File f = new File(pm.getProperty("lastDir"));
        	this.chooser.setCurrentDirectory(f);
        }
        */
       	
    }

	private JScrollPane createOutputPanel() {
		JPanel jpanel = new JPanel();
		//jpanel.setSize(30, 800);
		jpanel.setLayout(new GridLayout(0,1));
		String[] columns = CompareSequence.getOneLineHeaderArray();
		JLabel label = new JLabel("Select output columns");
		label.setBounds(450, 90, 130, 20);
		guiFrame.add(label);
		JButton selectAll = new JButton("Select All");
		selectAll.addActionListener(this);
		selectAll.setBounds(450, 110, 100, 20);
		guiFrame.add(selectAll);
		JButton deselectAll = new JButton("Deselect All");
		deselectAll.addActionListener(this);
		deselectAll.setBounds(450, 130, 100, 20);
		guiFrame.add(deselectAll);
		
		
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
		jsp.setBounds(450, 150, 200, 340);
		return jsp;
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
			//guiFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			boolean check = true;
			progressBar.setMaximum(model.size()-1);
			progressBar.setValue(0);
			left.setText(left.getText().trim());
			right.setText(right.getText().trim());
			saveFlanks(left.getText(), right.getText());
			
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
			afc.setMaxError((double)maxError.getValue());
			afc.setMaskLowQuality(maskLowQuality.isSelected());
			afc.setMaskLowQualityRemove(maskLowQualityRemove.isSelected());
			afc.setProgressBar(progressBar);
			afc.setFileChooser(chooser);
			afc.setStartButton(analyzeFiles);
			Thread newThread = new Thread(afc);
			newThread.start();
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
				if(pm.getProperty(subject.getAbsolutePath()+"_left")!= null) {
					left.setText(pm.getProperty(subject.getAbsolutePath()+"_left"));
				}
				if(pm.getProperty(subject.getAbsolutePath()+"_right")!= null) {
					right.setText(pm.getProperty(subject.getAbsolutePath()+"_right"));
				}
			}
		}
		else if(e.getActionCommand().contentEquals("Remove sequences with remarks")) {
			pm.setProperty("printCorrectColumnsOnly", ""+removeRemarkRows.isSelected());
			
		}
		else if(e.getActionCommand().contentEquals("Validate")) {
			System.out.println("Validate");
			validateNGS();
		}
		else if(e.getActionCommand().contentEquals("Run")) {
			System.out.println("Run");
		}
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

	private void validateNGS() {
		/*
		for(int row=0;row<ngs.getRowCount();row++) {
			for(int column=0;column<ngs.getColumnCount();column++) {
				String name = ngs.getColumnName(column);
				if(name.contentEquals("File")) {
					String file = (String) ngs.getModel().getValueAt(row, column);
					File f = new File(file);
					if(f.exists()) {
						
					}
					else {
						
					}
					//iterate 
				}
				//other fields
			}
		}
		*/
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
		
		KMERLocation kmerl = new KMERLocation(subject.seqString());
		Subject subjectObject = new Subject(subject,left,right);
		//kmerl = null;
		CompareSequence cs = new CompareSequence(subjectObject, seq.toString(), quals, f.getParent(), true, name, kmerl);
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
		boolean ngs = false;
		boolean ab1 = false;
		if(chooser.getSelectedFile().isDirectory()){
			for(File chosenFile: chooser.getSelectedFiles()) {
				for(File file: chosenFile.listFiles()){
					if(file.getName().endsWith(".ab1")){
						model.addElement(file);
						ab1 = true;
					}
					else if(file.getName().endsWith(".fastq") || file.getName().endsWith(".fastq.gz")){
						model.addElement(file);
						ngs = true;
					}
					else if(file.isDirectory()){
						fillTable(file);
					}
				}
			}
		}
		else{
			for(File file: chooser.getSelectedFiles()){
				if(file.getName().endsWith(".ab1")){
					model.addElement(file);
					ab1 = true;
				}
				else if(file.getName().endsWith(".fastq") || file.getName().endsWith(".fastq.gz")){
					model.addElement(file);
					ngs = true;
				}
			}
		}
		if(ab1 && ngs) {
			JOptionPane.showMessageDialog(guiFrame, "You cannot select .ab1 files and .fastq (or .fastq.gz) files simultaneously");
		}
		if(ngs) {
			switchToNGS();
		}
		else if(ab1) {
			switchToAB1(false);
		}
	}

	private void switchToAB1(boolean force) {
		if(!force && ab1Perspective) {
			return;
		}
		System.out.println("Switch to AB1! "+force);
		guiFrame.getContentPane().removeAll();
		guiFrame.setVisible(false);
		//switch to AB1
		//make sure the program exits when the frame closes
        guiFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        guiFrame.setTitle("Sanger Sequence Analyzer "+version+" - Tijsterman Lab");
        guiFrame.setSize(1000,600);
      
        //This will center the JFrame in the middle of the screen
        guiFrame.setLocationRelativeTo(null);
        guiFrame.setLayout(null);
        
        //The first JPanel contains a JLabel and JCombobox
        //comboPanel.setLayout(new GridLayout(0,4));
        //JLabel comboLbl = new JLabel("Select subject:");
        JLabel lblSubject = new JLabel();
		lblSubject.setBounds(91, 1, 55, 15);
		lblSubject.setText("Subject");
		guiFrame.add(lblSubject);
		
		JLabel lblQuery = new JLabel();
		lblQuery.setText("Query");
		lblQuery.setBounds(275, 1, 55, 15);
		guiFrame.add(lblQuery);
		
		JLabel lblOptions = new JLabel();
		lblOptions.setBounds(600, 1, 55, 15);
		lblOptions.setText("Options");
		guiFrame.add(lblOptions);
        
        JButton chooseSubject = new JButton("Select subject (.fa)");
        chooseSubject.setActionCommand("chooseSubject");
        chooseSubject.addActionListener(this);
        chooseSubject.setBounds(54, 35, 140, 25);
        guiFrame.add(chooseSubject);
        
        JButton dirChooser = new JButton("Select directory");
        dirChooser.setActionCommand("dirChooser");
        dirChooser.addActionListener(this);
        dirChooser.setBounds(212, 22, 174, 25);
        guiFrame.add(dirChooser);
        
         
        JButton fileChooser = new JButton("Select files");
        fileChooser.setActionCommand("fileChooser");
        fileChooser.addActionListener(this);
        fileChooser.setBounds(212, 53, 174, 25);
        guiFrame.add(fileChooser);
        
        
        
        JLabel leftFlank = new JLabel("leftFlank:");
        leftFlank.setBounds(448, 22, 55, 15);
        left.setBounds(509, 22, 150, 21);
        guiFrame.add(leftFlank);
        guiFrame.add(left);
        JLabel rightFlank = new JLabel("rightFlank:");
        rightFlank.setBounds(448, 53, 55, 15);
        guiFrame.add(rightFlank);
        right.setBounds(509, 53, 150, 21);
        guiFrame.add(right);
        
        maskLowQualityRemove.setBounds(680, 22, 142, 16);
        maskLowQualityRemove.setSelected(true);
        guiFrame.add(maskLowQualityRemove);
        
        removeRemarkRows.setBounds(680,62,200,16);
        removeRemarkRows.addActionListener(this);
        if(pm.getPropertyBoolean("printCorrectColumnsOnly")) {
        	removeRemarkRows.setSelected(pm.getPropertyBoolean("printCorrectColumnsOnly"));
        }
        guiFrame.add(removeRemarkRows);
        
        
        SpinnerModel model = new SpinnerNumberModel(0.05, 0, 1.0, 0.01);
        maxError = new JSpinner(model);
        maxError.setPreferredSize(new Dimension(50,20));
        maxError.setBounds(684, 40, 47, 22);
        guiFrame.add(maxError);
        
        JLabel lblMaxError = new JLabel();
		lblMaxError.setBounds(735, 42, 100, 15);
		lblMaxError.setText("max error (fraction)");
		guiFrame.add(lblMaxError);
		
		jFiles.addMouseListener(this);
		JScrollPane jsFile = new JScrollPane(jFiles);
		jsFile.setBounds(55, 90, 330, 400);
        guiFrame.add(jsFile);
        
        analyzeFiles = new JButton( "Start analysis");
        analyzeFiles.setActionCommand("Start");
        analyzeFiles.addActionListener(this);
        analyzeFiles.setBounds(55, 500, 100, 20);
        guiFrame.add(analyzeFiles);
        
        progressBar = new JProgressBar(0, 500);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setVisible(true);
        progressBar.setBounds(55, 525, 330, 20);
        guiFrame.add(progressBar);
        
        guiFrame.add(createOutputPanel());
        
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
        this.ab1Perspective = true;
	}

	private void switchToNGS() {
		if(!ab1Perspective) {
			return;
		}
		System.out.println("Switch to NGS!");
		//switch to NGS
		guiFrame.getContentPane().removeAll();
		guiFrame.setVisible(false);
		
		JLabel lblQuery = new JLabel();
		lblQuery.setText("Query");
		lblQuery.setBounds(275, 1, 55, 15);
		guiFrame.add(lblQuery);
		
        JButton dirChooser = new JButton("Select directory");
        dirChooser.setActionCommand("dirChooser");
        dirChooser.addActionListener(this);
        dirChooser.setBounds(212, 22, 174, 25);
        guiFrame.add(dirChooser);
         
        JButton fileChooser = new JButton("Select files");
        fileChooser.setActionCommand("fileChooser");
        fileChooser.addActionListener(this);
        fileChooser.setBounds(212, 53, 174, 25);
        guiFrame.add(fileChooser);
        
        addJTableNGS();
        
        JButton validate = new JButton("Validate input");
        validate.setActionCommand("Validate");
        validate.setBounds(20,500,120,20);
        validate.addActionListener(this);
        guiFrame.add(validate);
        
        JButton run = new JButton("Run");
        run.setActionCommand("Run");
        run.setBounds(20,520,120,20);
        run.addActionListener(this);
        guiFrame.add(run);
        
        guiFrame.setVisible(true);
        
        this.ab1Perspective = false;
	}

	private void addJTableNGS() {
		ngsModel = new NGSTableModel();
		ngs = new JTable(ngsModel);
		for (int i =0; i<ngsModel.getColumnCount();i++) {
			ngs.setDefaultRenderer(ngs.getColumnClass(i), new NGSCellRenderer());
	    }
		JScrollPane scrollPane = new JScrollPane(ngs);
		ngs.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
		ngs.setAutoCreateRowSorter(true);
		ngs.setCellSelectionEnabled(true);
		scrollPane.setPreferredSize( new Dimension( 800, 400 ) );
		scrollPane.setBounds(20, 100, 800, 400);
		guiFrame.add(scrollPane);
		ExcelAdapter myAd = new ExcelAdapter(ngs);
		addFilesToNGSModel();
	}

	private void addFilesToNGSModel() {
		for(File f: chooser.getSelectedFiles()) {
			NGS ngs = new NGS(f);
			ngsModel.addNGS(ngs);
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
}