package batch;

import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.Semaphore;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import gui.NGS;
import gui.NGSTableModel;
import utils.KMERLocation;
import utils.SequenceFileThread;
import utils.Subject;

public class SequenceControllerThread implements Runnable{
	private Vector<Thread> vThreads = new Vector<Thread>();
	private boolean includeStartEnd = false;
	private JFrame GUI;
	private int cpus;
	private boolean assembleRequired;
	private boolean readyToRun = false;
	
	public void setNGSfromGUI(Vector<NGS> v, NGSTableModel m, JFrame GUI, int maxReads, int minSupport, double maxError, String flashExec) {
		this.GUI = GUI;
		Vector<NGS> notOK = new Vector<NGS>();
		for(NGS n: v) {
			if(!n.allOK()) {
				notOK.add(n);
			}
		}
		if(notOK.size()>0) {
			//add info message
			JOptionPane.showMessageDialog(null, notOK.size()+" NGS entries have incorrect/incomplete info, please correct the red cells in the table", "A problem was found with your input", JOptionPane.ERROR_MESSAGE);
			return;
		}
		readyToRun = true;
		
		//this can als be a user defined maximum
		int cores = Runtime.getRuntime().availableProcessors();
		if(v.size()<=cores) {
			this.cpus = v.size();
		}
		else {
			this.cpus = cores;
		}
		
		//Thread stuff
		System.out.println("Gonna start "+cpus+" cpus");
		Semaphore mySemaphore = new Semaphore(cpus);
		
		int cpusForAssembly = 1;
		if(v.size()<cores) {
			cpusForAssembly = cores/v.size();
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
			Thread newThread = new Thread(sft);
			vThreads.add(newThread);
		}
	}
	
	
	@Override
	public void run() {
		if(!readyToRun) {
			return;
		}
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
				t.join();
			} catch (InterruptedException e) {
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
		//add button to export stuff to Combined file
	}


	private void enableButtons(boolean b) {
		if(GUI!=null) {
			for(Component c: GUI.getContentPane().getComponents()) {
				if(c instanceof JButton) {
					c.setEnabled(b);
				}
			}
		}
		
	}
	public boolean isAssemblyRequired() {
		return assembleRequired;
	}
}
