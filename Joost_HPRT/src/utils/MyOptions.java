package utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
		double maxError = -1.0;
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
		try {
			return (Long) cmd.getParsedOptionValue("m");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}
	public String getOutput() {
		return cmd.getOptionValue('o');
	}
	public String getOutputPostfix() {
		return cmd.getOptionValue('p');
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
	public boolean printTemplate() {
		return cmd.hasOption('x');
	}
	public boolean collapseEvents() {
		return cmd.hasOption('c');
	}
	public String printParameters() {
		Class<?> c = this.getClass();
        Method[] methods = c.getDeclaredMethods();
        StringBuffer sb = new StringBuffer();
        for(Method m: methods) {
       		if(!m.getName().equals("printParameters") && !m.getName().equals("getClass")) {
       			if(sb.length()>0) {
            		sb.append("\n");
            	}
       			try {
       				if(m.invoke(this) != null){
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
}
