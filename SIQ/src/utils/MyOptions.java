package utils;

import java.io.File;
import java.lang.reflect.Method;
import java.net.UnknownHostException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

public class MyOptions {
	//private String File; //in
	//private double maxError; //e
	//private int minNumber; //m
	//private String output; //o
	//private String output_postfix; //p
	//private int threads; //t
	//private boolean printTemplate; //x
	private CommandLine cmd;
	
	public MyOptions(CommandLine cmd) {
		this.cmd = cmd;
	}
	public String getFile() {
		return cmd.getOptionValue("in");
	}
	public double getMaxError() {
		//changed to 0.08
		double maxError = 0.08;
		try {
			if(cmd.getParsedOptionValue("e") != null) {
				Double number = (Double) cmd.getParsedOptionValue("e");
				if(number>=0 && number <=1.0) {
					maxError = number;
				}
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return maxError;
	}
	public long getMinNumber() {
		if(cmd.hasOption("m")) {
			try {
				return (Long) cmd.getParsedOptionValue("m");
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return 5;
	}
	public String getOutput() {
		if(cmd.hasOption('o')) {
			return cmd.getOptionValue('o');
		}
		return null;
	}
	public long getThreads() {
		if(cmd.hasOption('t')) {
			try {
				return (Long) cmd.getParsedOptionValue("t");
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return 1;
	}
	public boolean overwrite() {
		return cmd.hasOption('f');
	}
	public String printParameters() {
		if(cmd!= null) {
			Class<?> c = this.getClass();
	        Method[] methods = c.getDeclaredMethods();
	        StringBuffer sb = new StringBuffer();
	        for(Method m: methods) {
	       		if(!m.getName().equals("printParameters") && !m.getName().equals("getClass") 
	       				&& !m.getName().equals("hasOptions") && !m.getName().equals("testLeftRight")) {
	       			try {
	       				if(m.invoke(this) != null){
	       					if(sb.length()>0) {
	    	            		sb.append("\n");
	    	            	}
		       				String str = m.invoke(this).toString();
		       				String name = m.getName();
		       				name = name.replace("get","");
							sb.append(name+": "+str);//+":"+m.invoke(this));
	       				}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
					//sb.append(m);//+":"+m.invoke(this));
	       		}
	        }
	        return sb.toString();
		}
		return null;
	}
	public String getHostName() {
		java.net.InetAddress localMachine;
		try {
			localMachine = java.net.InetAddress.getLocalHost();
			return localMachine.getHostName();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	public boolean getHelp() {
		return cmd.hasOption('h');
	}
	public boolean hasOptions() {
		return cmd.getOptions().length != 0;
	}
	public boolean startGUI() {
		return cmd.hasOption('g');
	}
	public long getMaxReads() {
		if(cmd.hasOption('r')) {
			try {
				return (Long) cmd.getParsedOptionValue("r");
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return Long.MAX_VALUE;
	}
	public String getSingleFile() {
		return cmd.getOptionValue("infile");
	}
	public File getSingleFileF() {
		if(cmd.hasOption("infileF")) {
			return new File(cmd.getOptionValue("infileF"));
		}
		return null;
	}
	public File getSingleFileR() {
		if(cmd.hasOption("infileR")) {
			return new File(cmd.getOptionValue("infileR"));
		}
		return null;
	}
	public String getSubject() {
		return cmd.getOptionValue("subject");
	}
	public String getLeftFlank() {
		return cmd.getOptionValue("left");
	}
	public String getRightFlank() {
		return cmd.getOptionValue("right");
	}
	public File getSearchAdditional() {
		if(cmd.hasOption("additionalSearch")) {
			if(cmd.getOptionValue("additionalSearch").length()>0) {
				return new File(cmd.getOptionValue("additionalSearch"));
			}
		}
		return null;
	}
	public String getLeftPrimer() {
		String ret = cmd.getOptionValue("leftPrimer");
		if(ret!=null) {
			return ret.toLowerCase();
		}
		return null;
	}
	public String getRightPrimer() {
		String ret = cmd.getOptionValue("rightPrimer");
		if(ret!=null) {
			return ret.toLowerCase();
		}
		return null;
	}
	public long getMinPassedPrimer() {
		if(cmd.hasOption("minPassedPrimer")) {
			try {
				return (Long) cmd.getParsedOptionValue("minPassedPrimer");
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return 5;
	}
	public String getAlias() {
		return cmd.getOptionValue("alias");
	}
	//at the moment we don't allow jumps for NGS analysis
	public boolean allowJump() {
		return false;
	}
	public File getHDR() {
		if(cmd.hasOption("hdr")) {
			String hdr = cmd.getOptionValue("hdr");
			//can be that the option was given, but no file is there (a cromwell issue)
			if(hdr.length()>0) {
				return new File(hdr);
			}
		}
		return null;
	}
	public long getSearchSpace() {
		if(cmd.hasOption("search")) {
			try {
				return (Long) cmd.getParsedOptionValue("search");
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return -1;
	}
}
