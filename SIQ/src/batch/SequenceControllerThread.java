package batch;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Vector;
import java.util.concurrent.Semaphore;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.SequenceIterator;
import org.biojavax.bio.seq.RichSequence.IOTools;

import gui.NGS;
import gui.GUI;
import gui.NGSTableModel;
import gui.ReportPanel;
import utils.KMERLocation;
import utils.SequenceFileThread;
import utils.Subject;

public class SequenceControllerThread implements Runnable{
	private Vector<SequenceFileThread> vThreads = new Vector<SequenceFileThread>();
	private boolean includeStartEnd = false;
	private GUI GUI;
	private int cpus;
	private boolean assembleRequired;
	private boolean readyToRun = false;
	private volatile boolean exit = false;
	private File outputDir;
	private Vector<NGS> v;
	private File excelFile;
	private File tsvFile;
	private NGSTableModel m;
	
	public void setNGSfromGUI(Vector<NGS> v, NGSTableModel m, GUI GUI, int maxReads, int minSupport, double maxError, String flashExec, int cpus, int tinsDistValue, boolean remerge, boolean delinsFilter, boolean longread) {
		this.GUI = GUI;
		this.v = v;
		this.m = m;
		readyToRun = true;
		this.cpus = cpus;
		
		//Thread stuff
		System.out.println("Gonna start "+cpus+" cpus");
		System.out.println("Flash exec ["+flashExec+"]");
		Semaphore mySemaphore = new Semaphore(cpus);
		
		int cpusForAssembly = 1;
		if(v.size()<cpus) {
			cpusForAssembly = cpus/v.size();
		}
		//HACK, remove later
		String searchAdditional = "blabla.txt";
		BufferedReader is2;
		HashMap<String, String> hmAdditional = new HashMap<String, String>();
		if(new File(searchAdditional).exists()) {
			try {
				is2 = new BufferedReader(new FileReader(searchAdditional));
				SequenceIterator si2 = IOTools.readFastaDNA(is2, null);
				while(si2.hasNext()){
					Sequence seq = si2.nextSequence();
					hmAdditional.put(seq.getName(),seq.seqString());
				}
				hmAdditional.clear();
			} catch (FileNotFoundException | NoSuchElementException | BioException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		for(NGS n: v) {
			//not really perfect, but this will do
			System.out.println("Setting setMaxError "+maxError);
			n.setMaxError(maxError);
			
			Subject subject = n.getSubjectObject();
			System.out.println(subject);
			subject.swapPrimersIfNeeded();
			System.out.println("Primers: " +subject.hasPrimers());
			System.out.println("Flanks: " +subject.hasLeftRight());
			System.out.println("left: " +subject.getLeftPrimer());
			System.out.println("right: " +subject.getRightPrimer());
			//for now no additional sequences to search (final null argument)
			//SequenceFileThread sft = new SequenceFileThread(n.getR1(), true, subject, n.getOutput(), n.getOutputStats(), n.getMaxError(), null);
			SequenceFileThread sft = null;
			//should we get the assembled
			if(n.assembledOK()) {
				System.out.println("Starting assembled");
				sft = new SequenceFileThread(n.getAssembledFile(), true, subject, n.getOutput(), n.getOutputStats(), n.getMaxError(), hmAdditional, n.getOutputTopStats(), remerge, delinsFilter, longread);
			}
			else {
				this.assembleRequired = true;
				System.out.println("Starting assembled derived");
				sft = new SequenceFileThread(n.getAssembledFileDerived(), true, subject, n.getOutput(), n.getOutputStats(), n.getMaxError(), hmAdditional, n.getOutputTopStats(), remerge, delinsFilter, longread);
			}
			System.out.println("Setting minSupport "+minSupport);
			System.out.println("Setting setMaximumReads "+maxReads);
			
			sft.setMinimalCount(minSupport);
			sft.setNGS(n);
			sft.setCollapseStartEnd(includeStartEnd);
			sft.setAlias(n.getAlias());
			sft.setAllowJump(false);
			sft.setTableModel(m);
			sft.setMaximumReads(maxReads);
			sft.setFlash(flashExec,cpusForAssembly);
			sft.setSemaphore(mySemaphore);
			sft.setTinsDistance(tinsDistValue);
			sft.setHDR(n.getHDR());
			vThreads.add(sft);
		}
	}
	
	
	@Override
	public void run() {
		if(!readyToRun) {
			return;
		}
		GUI.guiFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		enableButtons(false);
		m.resetNumbers();
		ArrayList<Thread> threads = new ArrayList<Thread>();
		while(vThreads.size()>0) {
			Thread t = vThreads.remove(0);
		    t.start();
		    threads.add(t);
		}
		//keep waiting until they are done
		for(Thread t: threads) {
			try {
				while(t.isAlive() && !exit) {
					Thread.sleep(500);
					//System.out.println("sleeping "+t.getName());
				}
				if(exit) {
					//System.out.println("interrupting "+t.getName());
					t.interrupt();
				}
				System.out.println("called joined "+t.getName());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//all threads are done here
		if(excelFile!=null){
			GUI.exportToTSV(tsvFile);
			GUI.exportToExcel(excelFile);
			//Don't go to R anymore
			//ReportPanel.runR(excelFile, false);
			//ReportPanel.runR(excelFile, true);
		}
		if(v.size()>0) {
			File dir = v.get(0).getOutputDir();
			System.out.println(dir);
			try {
				Desktop.getDesktop().open(dir);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		/*
		Vector<Thread> running = new Vector<Thread>();
		Vector<Integer> toBeRemoved = new Vector<Integer>();
		while(vThreads.size()>0 || running.size()>0){
			for(int nr = running.size()-1;nr>=0;nr--){
				if(!running.get(nr).isAlive()){
					toBeRemoved.add(nr);
				}
			}
			for(int i: toBeRemoved){
				running.remove(i);
			}
			toBeRemoved.clear();
			if(running.size()<cores && vThreads.size()>0){
				Thread t = vThreads.remove(0);
				running.add(t);
				t.start();
			}
			try {
				//1 second of sleep... ZzZ...
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		*/
		//while(Thread.)
		enableButtons(true);
		GUI.guiFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		//add button to export stuff to Combined file
	}
	public void stop(){
        exit = true;
    }


	private void enableButtons(boolean b) {
		GUI.ngsModel.setEnabled(b);
		if(GUI!=null) {
			for(Component c: GUI.guiFrame.getContentPane().getComponents()) {
				if(c instanceof JButton) {
					if(!((JButton) c).getActionCommand().contentEquals("stop")) {
						c.setEnabled(b);
					}
				}
			}
		}
		
	}
	public boolean isAssemblyRequired() {
		return assembleRequired;
	}


	public void setExportToExcel(File excelFile) {
		this.excelFile = excelFile;
		//make sure it ends with .xlsx
		if(!this.excelFile.getAbsolutePath().endsWith(".xlsx")) {
			this.excelFile = new File(this.excelFile.getAbsolutePath()+".xlsx");
		}
		//set Tab-delimited file for SIQPlotter
		this.tsvFile = new File(excelFile.getAbsolutePath().replace(".xlsx", ".txt"));
	}


	public static boolean isOK(Vector<NGS> v2) {
		Vector<NGS> notOK = new Vector<NGS>();
		for(NGS n: v2) {
			if(!n.allOK()) {
				notOK.add(n);
			}
		}
		if(notOK.size()>0) {
			//add info message
			JOptionPane.showMessageDialog(null, notOK.size()+" NGS entries have incorrect/incomplete info, please correct the red cells in the table", "A problem was found with your input", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
		
	}
}
