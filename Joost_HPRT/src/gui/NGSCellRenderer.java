package gui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class NGSCellRenderer extends DefaultTableCellRenderer {
	private Color problem = new Color(204,0,0);
	private Color ok = new Color(102,255,102);
	public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column)
   {
	   NGSTableModel wtm = (NGSTableModel) table.getModel();
      NGS wine = (NGS) wtm.getValueAtRow(row);
      //System.out.println("hier Render!"+row+" c: "+column);
      if (column == 1) {
    	  if(wine.getSubjectOK()) {
    		  setBackground(ok);
    	  }
    	  else {
    		  setBackground(problem);
    	  }
      }
      else if (column == 0) {
    	  if(wine.FileOK()) {
    		  setBackground(ok);
    	  }
    	  else {
    		  setBackground(problem);
    	  }
      }
      else if (column == 2) {
    	  setBackground(ok);
      }
      else if (column == 3) {
    	  if(wine.leftFlankOK()) {
    		  setBackground(ok);
    	  }
    	  else {
    		  setBackground(problem);
    	  }
      }
      else if (column == 4) {
    	  if(wine.rightFlankOK()) {
    		  setBackground(ok);
    	  }
    	  else {
    		  setBackground(problem);
    	  }
      }
      else if (column == 5) {
    	  if(wine.leftPrimerOK()) {
    		  setBackground(ok);
    	  }
    	  else {
    		  setBackground(problem);
    	  }
      }
      else if (column == 6) {
    	  if(wine.rightPrimerOK()) {
    		  setBackground(ok);
    	  }
    	  else {
    		  setBackground(problem);
    	  }
      }
      else if (column == 7) {
    	  if(wine.getMinPassedPrimer()>=0 && wine.getMinPassedPrimer()<=15) {
    		  setBackground(ok);
    	  }
    	  else {
    		  setBackground(problem);
    	  }
      }
      
      else {
         setBackground(Color.white);
         System.out.println("white");
      }
  
      return super.getTableCellRendererComponent(table, value, isSelected,
                                                 hasFocus, row, column);
   }
}
