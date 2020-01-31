package utils;

import java.util.*;

import dnaanalysis.Blast;

import java.io.*;
class StreamGobbler extends Thread
{
    InputStream is;
    String type;
    HashMap<String, ArrayList<Blast>> blasts = new HashMap<String, ArrayList<Blast>>();
    
    StreamGobbler(InputStream is, String type)
    {
        this.is = is;
        this.type = type;
    }
    
    public void run()
    {
        try
        {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line=null;
            while ( (line = br.readLine()) != null){
            	Blast b = Blast.parseBlast(line);
            	if(blasts.containsKey(b.getId())) {
            		blasts.get(b.getId()).add(b);
            	}
            	else {
            		ArrayList<Blast> bl = new ArrayList<Blast>();
            		bl.add(b);
            		blasts.put(b.getId(), bl);
            	}
            	//System.out.println(line);
                //System.out.println(type + ">" + line);
            }
            } catch (IOException ioe)
              {
                ioe.printStackTrace();  
              }
    }
    public HashMap<String, ArrayList<Blast>> getBlastResult(){
    	return blasts;
    }
}