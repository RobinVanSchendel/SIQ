package gui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class NGSCellRenderer extends DefaultTableCellRenderer {
	public static Color problem = new Color(255,102,102);
	public static Color ok = new Color(102,255,102);
	public static Color optional = new Color(255,204,51);
	public static Color greenFont = new Color(0,102,51);
	public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column)
   {
	  NGSTableModel wtm = (NGSTableModel) table.getModel();
      NGS wine = (NGS) wtm.getValueAtRow(row);
      //System.out.println("hier Render!"+row+" c: "+column);
      //BUG: make sure the info is not changed anymore at this point as it causes disasters
      if(!wtm.isEnabled()) {
    	  return super.getTableCellRendererComponent(table, value, isSelected,
                  hasFocus, row, column);
      }
      //R1
      if (column == 0) {
    	  if(wine.R1equalsR2()) {
    		  setForeground(problem);
    		  this.setBackground(Color.white);
    	  }
    	  else if(wine.R1OK()) {
    		  setForeground(greenFont);
    		  this.setBackground(Color.white);
    	  }
    	  else {
    		  setForeground(optional);
    		  this.setBackground(problem);
    	  }
      }
      //R2
      else if (column == 1) {
    	  this.setBackground(Color.white);
    	  if(wine.R1equalsR2()) {
    		  setForeground(problem);
    	  }
    	  else if(wine.R2OK()) {
    		  setForeground(greenFont);
    	  }
    	  else {
    		  setForeground(optional);
    	  }
      }
      else if (column == 2) {
    	  if(wine.getSubjectOK()) {
    		  setForeground(greenFont);
    		  this.setBackground(Color.white);
    	  }
    	  else {
    		  this.setBackground(problem);
    		  setForeground(problem);
    	  }
      }
      else if (column == 3) {
    	  if(wine.getAlias()==null || wine.getAlias().length()==0) {
    		  this.setBackground(problem);
    	  }
    	  else {
    		  this.setBackground(Color.white);
	    	  setForeground(Color.white);
	    	  setForeground(greenFont);
    	  }
      }
      else if (column == 4) {
    	  //System.out.println("rendering leftFlank "+wine.leftFlankOK());
    	  //System.out.println("rendering leftFlank "+wine.getLeftFlank());
    	  this.setBackground(Color.white);
    	  if(wine.leftFlankOK()) {
    		  setForeground(greenFont);
    		  this.setBackground(Color.white);
    	  }
    	  else {
    		  setForeground(problem);
    		  this.setBackground(problem);
    	  }
      }
      else if (column == 5) {
    	  if(wine.rightFlankOK()) {
    		  setForeground(greenFont);
    		  this.setBackground(Color.white);
    	  }
    	  else {
    		  setForeground(problem);
    		  this.setBackground(problem);
    	  }
      }
      else if (column == 6) {
    	  this.setBackground(Color.white);
    	  if(wine.leftPrimerOK()) {
    		  setForeground(greenFont);
    	  }
    	  else {
    		  setForeground(problem);
    	  }
      }
      else if (column == 7) {
    	  this.setBackground(Color.white);
    	  if(wine.rightPrimerOK()) {
    		  setForeground(greenFont);
    	  }
    	  else {
    		  setForeground(problem);
    	  }
      }
      else if (column == 8) {
    	  if(wine.getMinPassedPrimer()>=0 && wine.getMinPassedPrimer()<=15) {
    		  setForeground(greenFont);
    	  }
    	  else {
    		  setForeground(problem);
    	  }
      }
      else {
         setBackground(Color.white);
      }
  
      return super.getTableCellRendererComponent(table, value, isSelected,
                                                 hasFocus, row, column);
   }
}
