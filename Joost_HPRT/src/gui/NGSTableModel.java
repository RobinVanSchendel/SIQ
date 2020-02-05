package gui;

import java.io.File;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

public class NGSTableModel extends AbstractTableModel {
	// holds the strings to be displayed in the column headers of our table
	   final String[] columnNames = {"File", "Subject", "Alias", "leftFlank", "rightFlank", "leftPrimer", "rightPrimer", "minPassedPrimer","Status", "#Reads", "#Correct","%Correct"};
	  
	   // holds the data types for all our columns
	   Class[] columnClasses = {String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, Float.class, Integer.class, Integer.class, Float.class};
	  
	   // holds our data
	   Vector<NGS> data = new Vector<NGS>();
	   
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
	  
	   public Class<?> getColumnClass(int c) {
	      return columnClasses[c];
	   }
	  
	   public Object getValueAt(int row, int col) {
	      NGS wine = (NGS) data.elementAt(row);
	      if (col == 0)      return wine.getFile();
	      else if (col == 1) return wine.getSubject();
	      else if (col == 2) return wine.getAlias();
	      else if (col == 3) return wine.getLeftFlank();
	      else if (col == 4) return wine.getRightFlank();
	      else if (col == 5) return wine.getLeftPrimer();
	      else if (col == 6) return wine.getRightPrimer();
	      else if (col == 7) return wine.getMinPassedPrimer();
	      else if (col == 8) return wine.getStatus();
	      else if (col == 9) return wine.getTotalReads();
	      else if (col == 10) return wine.getCorrectReads();
	      else if (col == 11) return wine.getCorrectPercentage();
	      else return null;
	   }
	  
	   public Object getValueAtRow(int row) {
		  NGS wine = (NGS) data.elementAt(row);
	      return wine;
	      
	   }
	   @Override
	   public boolean isCellEditable(int row, int col) {
		   if(col>=8) {
			   return false;
		   }
		   return true;
	   }
	   
	   public Vector<NGS> getData(){
		   return data;
	   }
	   
	   @Override
	   public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		   NGS wine = (NGS) data.elementAt(rowIndex);
		   if(columnIndex == 0) {
			   wine.setFile((String)aValue);
		   }
		   else if(columnIndex == 1) {
			   wine.setSubject((String)aValue);
		   }
		   else if(columnIndex == 2) {
			   wine.setAlias((String)aValue);
		   }
		   else if(columnIndex == 3) {
			   wine.setLeftFlank((String)aValue);
		   }
		   else if(columnIndex == 4) {
			   wine.setRightFlank((String)aValue);
		   }
		   else if(columnIndex == 5) {
			   wine.setLeftPrimer((String)aValue);
		   }
		   else if(columnIndex == 6) {
			   wine.setRightPrimer((String)aValue);
		   }
		   else if(columnIndex == 7) {
			   try {
				   int passed = Integer.parseInt((String)aValue);
				   wine.setMinPassedPrimer(passed);
			   }
			   catch(Exception e) {
				   JOptionPane.showMessageDialog(null, "minPassedPrimer requires a number as input", "Error", JOptionPane.ERROR_MESSAGE);
			   }
		   }
		   else if(columnIndex == 8) {
			   float status = ((Float)aValue);
			   wine.setStatus(status);
		   }
		   else if(columnIndex == 9) {
			   int total = ((Integer)aValue);
			   wine.setTotalReads(total);
		   }
		   else if(columnIndex == 10) {
			   int correct = ((Integer)aValue);
			   wine.setCorrectReads(correct);
		   }
		   else if(columnIndex == 11) {
			   float correct = ((Float)aValue);
			   wine.setPercentage(correct);
		   }
		   fireTableCellUpdated(rowIndex, columnIndex);
	   }
	   
	   protected void updateStatus(int rowIndex, float progress) {
		   int progressBarColumn = 8;
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
		int progressBarColumn = 9;
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
		int progressBarColumn = 10;
		NGS wine = (NGS) data.elementAt(rowIndex);
        if (wine != null) {
            setValueAt(correct, rowIndex, progressBarColumn);
            fireTableCellUpdated(rowIndex, progressBarColumn);
        }		
	}

	public void setPercentage(NGS ngs, float i) {
		updatePercentage(ngs.getRowNumber(),i);
	}

	private void updatePercentage(int rowNumber, float i) {
		int progressBarColumn = 11;
		NGS wine = (NGS) data.elementAt(rowNumber);
        if (wine != null) {
            setValueAt(i, rowNumber, progressBarColumn);
            fireTableCellUpdated(rowNumber, progressBarColumn);
        }	
		
	}
}
