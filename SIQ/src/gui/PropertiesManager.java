package gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JOptionPane;

import utils.CompareSequence;

public class PropertiesManager {
	private Properties props;
	public PropertiesManager() {
		readPropFile();
	}
	private void readPropFile() {
		String name = getPropFileName();
		File f = new File(name);
		//create one if it does not exist
		if(!f.exists()) {
			System.out.println("Created properties file "+f.getName());
			try {
				f.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			System.out.println("Found properties file "+f.getName());
		}
		//now test if it is writable
		if(f.exists() && f.canWrite()) {
			System.out.println("current directory is writable");
		} else {
			File curDir = new File("");
			System.out.println("current directory is NOT writable "+curDir.getAbsolutePath());
			JOptionPane.showMessageDialog(null, "current directory is NOT writable: \n"+curDir.getAbsolutePath()+"\n"
					+ "Please start SIQ from another directory", "Please run SIQ from another location", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		props = new Properties();
		try {
			props.load(new FileInputStream(name));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(Object key: props.keySet()) {
			//System.out.println(key+" "+props.getProperty((String) key));
		}
	}
	private String getPropFileName() {
		String userName = System.getProperty("user.name");
		String propFile = userName+".properties";
		return propFile;
	}
	public String getProperty(String key) {
		return props.getProperty(key);
	}
	public boolean getPropertyBoolean(String key) {
		if(props.containsKey(key) && props.getProperty(key).equals("true")) {
			return true;
		}
		return false;
	}
	public void setProperty(String key, String value) {
		props.setProperty(key, value);
		//System.out.println(key+" "+value);
		writePropFile();
	}
	public void writePropFile() {
		try {
			props.store(new FileOutputStream(getPropFileName()), null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public boolean[] getOutputColumns() {
		String[] columns = CompareSequence.getOneLineHeaderArray();
		boolean[] include = new boolean[columns.length];
		for(int i = 0;i<include.length;i++) {
			String s = columns[i];
			if(getPropertyBoolean(s)) {
				include[i] = true;
				//System.out.println(s+" true");
			}
			else {
				include[i] = false;
				//System.out.println(s+" false");
			}
		}
		return include;
	}
}
