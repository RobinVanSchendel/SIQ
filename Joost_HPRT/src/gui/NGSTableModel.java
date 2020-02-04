package gui;

import java.util.Vector;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

public class NGSTableModel extends AbstractTableModel {
	// holds the strings to be displayed in the column headers of our table
	   final String[] columnNames = {"File", "Subject", "Alias", "leftFlank", "rightFlank", "leftPrimer", "rightPrimer", "minPassedPrimer"};
	  
	   // holds the data types for all our columns
	   Class[] columnClasses = {String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class};
	  
	   // holds our data
	   Vector<NGS> data = new Vector<NGS>();
	   
	   // adds a row
	   public void addNGS(NGS w) {
	      data.addElement(w);
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
	      else return null;
	   }
	  
	   public Object getValueAtRow(int row) {
		  NGS wine = (NGS) data.elementAt(row);
	      return wine;
	      
	   }
	  
	   public boolean isCellEditable(int row, int col) {
	      return true;
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
				   
			   }
		   }
		   fireTableCellUpdated(rowIndex, columnIndex);
	   }
	   
	   
}
