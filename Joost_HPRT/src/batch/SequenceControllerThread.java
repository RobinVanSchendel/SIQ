package batch;

import java.io.File;
import java.util.Vector;

import javax.swing.JButton;

import gui.NGS;
import gui.NGSTableModel;
import utils.KMERLocation;
import utils.SequenceFileThread;
import utils.Subject;

public class SequenceControllerThread implements Runnable{
	private Vector<Thread> vThreads = new Vector<Thread>();
	private boolean includeStartEnd = false;
	private JButton runButton, excelNGS;
	
	public void setNGSfromGUI(Vector<NGS> v, NGSTableModel m, JButton runButton, JButton excelNGS, int maxReads) {
		this.runButton = runButton;
		this.excelNGS = excelNGS;
		
		for(NGS n: v) {
			Subject subject = n.getSubjectObject();
			System.out.println(subject);
			System.out.println("Primers: " +subject.hasPrimers());
			System.out.println("Flanks: " +subject.hasPrimers());
			//for now no additional sequences to search (final null argument)
			SequenceFileThread sft = new SequenceFileThread(new File(n.getFile()), true, subject, n.getOutput(), n.getOutputStats(), n.getMaxError(), null);
			sft.setMinimalCount(2);
			sft.setNGS(n);
			sft.setCollapseStartEnd(includeStartEnd);
			sft.setMaximumReads(Long.MAX_VALUE);
			sft.setAlias(n.getAlias());
			KMERLocation kmerl = new KMERLocation(subject.getString());
			sft.setKMERLocation(kmerl);
			sft.setAllowJump(false);
			sft.setTableModel(m);
			sft.setMaximumReads(maxReads);
			Thread newThread = new Thread(sft);
			vThreads.add(newThread);
		}
	}
	
	
	@Override
	public void run() {
		runButton.setEnabled(false);
		excelNGS.setEnabled(false);
		int cores = Runtime.getRuntime().availableProcessors();
		int maxFiles = vThreads.size();
		System.out.println("Gonna start "+Math.min(cores, maxFiles)+" cpus");
		
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
		runButton.setEnabled(true);
		excelNGS.setEnabled(true);
		//add button to export stuff to Combined file
		
		
	}
}
