package batch;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.Semaphore;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

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
	
	public void setNGSfromGUI(Vector<NGS> v, NGSTableModel m, GUI GUI, int maxReads, int minSupport, double maxError, String flashExec, int cpus, int tinsDistValue) {
		this.GUI = GUI;
		this.v = v;
		readyToRun = true;
		this.cpus = cpus;
		
		//this can als be a user defined maximum
		//now it is
		/*
		int cores = Runtime.getRuntime().availableProcessors();
		if(v.size()<=cores) {
			this.cpus = v.size();
		}
		else {
			this.cpus = cores;
		}
		*/
		
		//Thread stuff
		System.out.println("Gonna start "+cpus+" cpus");
		Semaphore mySemaphore = new Semaphore(cpus);
		
		int cpusForAssembly = 1;
		if(v.size()<cpus) {
			cpusForAssembly = cpus/v.size();
		}
		for(NGS n: v) {
			//not really perfect, but this will do
			System.out.println("Setting setMaxError "+maxError);
			n.setMaxError(maxError);
			
			Subject subject = n.getSubjectObject();
			System.out.println(subject);
			System.out.println("Primers: " +subject.hasPrimers());
			System.out.println("Flanks: " +subject.hasLeftRight());
			//for now no additional sequences to search (final null argument)
			//SequenceFileThread sft = new SequenceFileThread(n.getR1(), true, subject, n.getOutput(), n.getOutputStats(), n.getMaxError(), null);
			SequenceFileThread sft = null;
			//should we get the assembled
			if(n.assembledOK()) {
				System.out.println("Starting assembled");
				sft = new SequenceFileThread(n.getAssembledFile(), true, subject, n.getOutput(), n.getOutputStats(), n.getMaxError(), null);
			}
			else {
				this.assembleRequired = true;
				System.out.println("Starting assembled derived");
				sft = new SequenceFileThread(n.getAssembledFileDerived(), true, subject, n.getOutput(), n.getOutputStats(), n.getMaxError(), null);
			}
			//leave out for now
			/*
			if(n.unAssembledFOK()) {
				sft.setFileF(n.getUnassembledFileF());
			}
			else {
				sft.setFileF(n.getUnassembledFFileDerived());
			}
			if(n.unAssembledROK()) {
				sft.setFileR(n.getUnassembledFileR());
			}
			else {
				sft.setFileR(n.getUnassembledRFileDerived());
			}
			*/
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
			GUI.exportToExcel(excelFile);
			ReportPanel.runR(excelFile, false);
			ReportPanel.runR(excelFile, true);
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
