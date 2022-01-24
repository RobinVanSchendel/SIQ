package gui;

import java.io.File;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

public class NGSTableModel extends AbstractTableModel {
	// holds the strings to be displayed in the column headers of our table
	   final String[] columnNames = {"R1 file", "R2 file", "reference", "alias", "left flank", "right flank", "left primer", "right primer","HDR", "<html>#bases past<br>primer</html>","%Complete", "#Reads", "#Correct","%Correct","Status"};
	  
	   // holds the data types for all our columns
	   Class[] columnClasses = {File.class, File.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, Float.class, Integer.class, Integer.class, Float.class, String.class};
	  
	   // holds our data
	   Vector<NGS> data = new Vector<NGS>();

	   private boolean enabled = true;
	   
	   public void removeAll() {
		   data.removeAllElements();
		   this.fireTableDataChanged();
	   }
	   // adds a row
	   public void addNGS(NGS w) {
	      data.addElement(w);
	      w.setRowNumber(data.size()-1);
	      fireTableRowsInserted(data.size()-1, data.size()-1);
	   }
	  
	   public int getColumnCount() {
	      return columnNames.length;
	   }
	          
	   public int getRowCount() {
	      return data.size();
	   }
	  
	   public String getColumnName(int col) {
	      return columnNames[col];
	   }
	   public Class<?> getColumnClass(String str) {
		   int i = 0;
		   for(String col: columnNames) {
			   if(col.equals(str)) {
				   return columnClasses[i];
			   }
			   i++;
		   }
		   return null;
	   }
	   public int getColumnNr(String str) {
		   int i = 0;
		   for(String col: columnNames) {
			   if(col.equals(str)) {
				   return i;
			   }
			   i++;
		   }
		   return -1;
	   }
	  
	   public Class<?> getColumnClass(int c) {
	      return columnClasses[c];
	   }
	  
	   public Object getValueAt(int row, int col) {
	      NGS wine = (NGS) data.elementAt(row);
	      if (col == 0)      return wine.getR1();
	      else if (col == 1)      return wine.getR2();
	      else if (col == 2) return wine.getSubject();
	      else if (col == 3) return wine.getAlias();
	      else if (col == 4) return wine.getLeftFlank();
	      else if (col == 5) return wine.getRightFlank();
	      else if (col == 6) return wine.getLeftPrimer();
	      else if (col == 7) return wine.getRightPrimer();
	      else if (col == 8) return wine.getHDR();
	      else if (col == 9) return wine.getMinPassedPrimer();
	      else if (col == 10) return wine.getStatus();
	      else if (col == 11) return wine.getTotalReads();
	      else if (col == 12) return wine.getCorrectReads();
	      else if (col == 13) return wine.getCorrectPercentage();
	      else if (col == 14) return wine.getTextStatus();
	      
	      else return null;
	   }
	  
	   public Object getValueAtRow(int row) {
		  NGS wine = (NGS) data.elementAt(row);
	      return wine;
	      
	   }
	   @Override
	   public boolean isCellEditable(int row, int col) {
		   if(!enabled) {
			   return false;
		   }
		   if(col>=12) {
			   return false;
		   }
		   return true;
	   }
	   
	   public Vector<NGS> getData(){
		   return data;
	   }
	   
	   @Override
	   public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		   if(aValue instanceof String) {
			   //don't put these
			   if(((String)aValue).length()==0) {
				   return;
			   }
		   }
		   NGS wine = (NGS) data.elementAt(rowIndex);
		   if(columnIndex == 0) {
			   if(aValue== null) {
				   wine.setR1(null);
			   }
			   if(aValue instanceof File) {
				   wine.setR1((File)aValue);
			   }
			   else if(aValue instanceof String) {
				   wine.setR1(new File((String)aValue));
			   }
		   }
		   else if(columnIndex == 1) {
			   if(aValue== null) {
				   wine.setR2(null);
			   }
			   if(aValue instanceof File)
				   wine.setR2((File)aValue);
			   else if(aValue instanceof String) 
				   wine.setR2(new File((String)aValue));
		   }
		   else if(columnIndex == 2) {
			   wine.setSubject((String)aValue);
		   }
		   else if(columnIndex == 3) {
			   wine.setAlias((String)aValue);
		   }
		   else if(columnIndex == 4) {
			   wine.setLeftFlank((String)aValue);
		   }
		   else if(columnIndex == 5) {
			   wine.setRightFlank((String)aValue);
		   }
		   else if(columnIndex == 6) {
			   wine.setLeftPrimer((String)aValue);
		   }
		   else if(columnIndex == 7) {
			   wine.setRightPrimer((String)aValue);
		   }
		   else if(columnIndex == 8) {
			   wine.setHDR((String)aValue);
		   }
		   
		   else if(columnIndex == 9) {
			   try {
				   if(aValue == null) {
					   wine.setMinPassedPrimer(5);
				   }
				   else {
					   int passed = Integer.parseInt((String)aValue);
					   wine.setMinPassedPrimer(passed);
				   }
			   }
			   catch(Exception e) {
				   JOptionPane.showMessageDialog(null, "minPassedPrimer requires a number as input", "Error", JOptionPane.ERROR_MESSAGE);
			   }
		   }
		   else if(columnIndex == 10) {
			   if(aValue !=null && aValue instanceof Float) {
				   float status = ((Float)aValue);
				   wine.setStatus(status);
			   }
		   }
		   else if(columnIndex == 11) {
			   if(aValue !=null && aValue instanceof Integer) {
					   int total = ((Integer)aValue);
					   wine.setTotalReads(total);
			   }
		   }
		   else if(columnIndex == 12) {
			   if(aValue !=null && aValue instanceof Integer) {
					   int correct = ((Integer)aValue);
					   wine.setCorrectReads(correct);
			   }
		   }
		   else if(columnIndex == 13) {
			   if(aValue !=null && aValue instanceof Float) {
				   float correct = ((Float)aValue);
				   wine.setPercentage(correct);
			   }
		   }
		   else if(columnIndex == 14) {
			   if(aValue !=null && aValue instanceof String) {
				   wine.setTextStatus((String)aValue);
			   }
		   }
		   this.fireTableRowsUpdated(rowIndex, rowIndex);
		   //fireTableCellUpdated(rowIndex, columnIndex);
	   }
	   
	   protected void updateStatus(int rowIndex, float progress) {
		   int progressBarColumn = getColumnNr("%Complete");
		   NGS wine = (NGS) data.elementAt(rowIndex);
           if (wine != null) {
               setValueAt(progress, rowIndex, progressBarColumn);
               fireTableCellUpdated(rowIndex, progressBarColumn);
           }
       }

	public void setStatus(NGS n, float perc) {
		updateStatus(n.getRowNumber(), perc);
	}

	public void setTotal(NGS ngs, int i) {
		updateTotal(ngs.getRowNumber(), i);
	}

	private void updateTotal(int rowIndex, int total) {
		int progressBarColumn = getColumnNr("#Reads");
		NGS wine = (NGS) data.elementAt(rowIndex);
        if (wine != null) {
            setValueAt(total, rowIndex, progressBarColumn);
            fireTableCellUpdated(rowIndex, progressBarColumn);
        }
	}

	public void setCorrect(NGS ngs, int i) {
		updateCorrect(ngs.getRowNumber(), i);
	}

	private void updateCorrect(int rowIndex, int correct) {
		int progressBarColumn = getColumnNr("#Correct");
		NGS wine = (NGS) data.elementAt(rowIndex);
        if (wine != null) {
            setValueAt(correct, rowIndex, progressBarColumn);
            fireTableCellUpdated(rowIndex, progressBarColumn);
        }		
	}

	public void setPercentage(NGS ngs, float i) {
		updatePercentage(ngs.getRowNumber(),i);
	}
	
	public void setTextStatus(NGS ngs, String string) {
		int textStatusColumn = getColumnNr("Status");
		int rowNumber = ngs.getRowNumber();
		NGS wine = (NGS) data.elementAt(rowNumber);
        if (wine != null) {
        	setValueAt(string, rowNumber, textStatusColumn);
        	fireTableCellUpdated(rowNumber, textStatusColumn);
        }
	}

	private void updatePercentage(int rowNumber, float i) {
		int progressBarColumn = getColumnNr("%Correct");
		NGS wine = (NGS) data.elementAt(rowNumber);
        if (wine != null) {
            setValueAt(i, rowNumber, progressBarColumn);
            fireTableCellUpdated(rowNumber, progressBarColumn);
        }	
		
	}
	public void setEnabled(boolean enable) {
		System.out.println("JTAble enabled: "+enable);
		this.enabled = enable;
	}
	public boolean isEnabled() {
		return this.enabled;
	}
	public void resetNumbers() {
		for(NGS n: this.data) {
			this.setCorrect(n, 0);
			this.setPercentage(n, 0);
			this.setTotal(n, 0);
			this.setStatus(n, 0);
			this.setTextStatus(n,"");
		}
	}
}
