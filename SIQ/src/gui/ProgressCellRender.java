package gui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class ProgressCellRender extends JProgressBar implements TableCellRenderer {
	public ProgressCellRender() {
		//add the percentage
		super.setStringPainted(true);
	}
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        int progress = 0;
        if (value instanceof Float) {
            progress = Math.round(((Float) value) * 100f);
        } else if (value instanceof Integer) {
            progress = (int) value;
        }
       	setValue(progress);
        
        //disable for now as text color is not changeable per progressbar
        if(progress < 0) {
        	this.setForeground(Color.orange);
        	//setValue(50);
        	//this.set
        }
        else {
        	this.setForeground(Color.BLUE);
        }
        
        return this;
    }

}
