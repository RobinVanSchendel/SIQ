package gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import utils.CompareSequence;
import utils.StreamGobbler;

public class ReportPanel extends JFrame implements ActionListener {
	protected int remarkColumn = 0;
	private JTable jt;
	private ArrayList<CompareSequence> result;
	private JFileChooser jfc;
	private String leftFlank;
	private String rightFlank;
	private boolean masked;
	private double maxError;
	private boolean[] removeColumns;
	private boolean split;
	private File lastFileSaved = null;
	private final static String RscriptCommand = "C:\\Program Files\\R\\R-3.6.0\\bin\\Rscript.exe"; 
	private final static String Rscript = "E:\\R_working_directory\\r-plots-ngs\\SIQPlotter.R";
			
	
	public ReportPanel(String title) {
		this.setSize(1200, 800);
		this.setMinimumSize(this.getSize());
		this.setLayout(null);
		this.setTitle(title);
		this.pack();
		
	}
	public void setup(ArrayList<CompareSequence> result) {
		this.result = result;
		statusPanelInitiate();
		tablePanelInitiate();
		
		JButton export = new JButton("Export to Excel");
		export.setBounds(20, 710, 120, 20);
		export.addActionListener(this);
		this.add(export);
		
		this.setVisible(true);
		
	}
	private void statusPanelInitiate() {
		//statusPanel.setPreferredSize(new Dimension(300,100));
		JLabel lblTotal = new JLabel("Total files:");
		lblTotal.setBounds(20, 20, 100, 20);
		this.add(lblTotal);

		JLabel lbtTotalSeqs = new JLabel(getUniqueNrFiles()+"");
		lbtTotalSeqs.setBounds(120, 20, 100, 20);
		this.add(lbtTotalSeqs);
		
		JLabel lblTotalOk = new JLabel("OK:");
		lblTotalOk.setBounds(20, 40, 100, 20);
		this.add(lblTotalOk);

		JLabel lbtTotalSeqsOk = new JLabel(getResultsWithoutRemark()+"");
		lbtTotalSeqsOk.setBounds(120, 40, 100, 20);
		this.add(lbtTotalSeqsOk);
		
		JLabel lblTotalNotOk = new JLabel("Not OK:");
		lblTotalNotOk.setBounds(20, 60, 100, 20);
		this.add(lblTotalNotOk);

		JLabel lbtTotalSeqsNotOk = new JLabel(getResultsWithRemark()+"");
		lbtTotalSeqsNotOk.setBounds(120, 60, 100, 20);
		this.add(lbtTotalSeqsNotOk);
		
		JLabel leftFlankText = new JLabel("leftFlank:");
		JLabel rightFlankText = new JLabel("rightFlank:");
		leftFlankText.setBounds(220, 20, 100, 20);
		rightFlankText.setBounds(220, 40, 100, 20);
		this.add(leftFlankText);
		this.add(rightFlankText);
		
		JTextField leftFlankT = new JTextField(leftFlank);
		leftFlankT.setEnabled(false);
		JTextField rightFlankT = new JTextField(rightFlank);
		rightFlankT.setEnabled(false);
		leftFlankT.setBounds(320, 20, 200, 20);
		rightFlankT.setBounds(320, 40, 200, 20);
		this.add(leftFlankT);
		this.add(rightFlankT);
		
		JCheckBox mask = new JCheckBox("maskLowQualityRemove");
		mask.setEnabled(false);
		mask.setSelected(this.masked);
		mask.setBounds(530, 20, 100, 20);
		this.add(mask);
		
		JLabel error = new JLabel("Max error: "+this.maxError);
		error.setBounds(534, 40, 100, 20);
		this.add(error);
		
		JCheckBox split = new JCheckBox("Split reads");
		split.setEnabled(false);
		split.setSelected(this.split);
		split.setBounds(534, 60, 100, 20);
		this.add(split);
		
	}
	private int getUniqueNrFiles() {
		ArrayList<File> files = new ArrayList<File>();
		for(CompareSequence cs: result) {
			if(!files.contains(cs.getFile())){
				files.add(cs.getFile());
			}
		}
		return files.size();
	}
	private int getResultsWithRemark() {
		int ok = 0;
		for(CompareSequence cs: result) {
			if(cs.getRemarks().length()>0) {
				ok++;
			}
		}
		return ok;
	}
	private int getResultsWithoutRemark() {
		int notOk = 0;
		for(CompareSequence cs: result) {
			if(cs.getRemarks().length()==0) {
				notOk++;
			}
		}
		return notOk;
	}
	private void tablePanelInitiate() {
		Vector<Object> header = getHeader();
		//set the remark column
		int index = 0;
		for(Object o: header) {
			if(o.toString().equals("Remarks")) {
				remarkColumn = index;
				break;
			}
			index++;
		}
		
		jt = new JTable(getResultVector(),header);
		JScrollPane scrollPane = new JScrollPane(jt);
		jt.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
		jt.setAutoCreateRowSorter(true);
		scrollPane.setPreferredSize( new Dimension( 1150, 600 ) );
		scrollPane.setBounds(20, 100, 1150, 600);
		this.add(scrollPane);
		jt.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
		    @Override
		    public Component getTableCellRendererComponent(JTable table,
		            Object value, boolean isSelected, boolean hasFocus, int row, int col) {

		        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

		        String status = (String)table.getModel().getValueAt(row, remarkColumn);
		        if (status.length()>0) {
		            setBackground(Color.RED);
		            setForeground(Color.BLACK);
		        } else {
		            setBackground(table.getBackground());
		            setForeground(table.getForeground());
		        }       
		        return this;
		    }   
		});
	}
	private Vector<Vector<Object>> getResultVector() {
		Vector<Vector<Object>> v = new Vector<Vector<Object>>();
		for(CompareSequence cs: result) {
			Vector<Object> w = new Vector<Object>();
			for(String s: cs.toStringOneLine("").split("\t")) {
				try{
					if(s.contentEquals("true") || s.contentEquals("false")) {
						w.add(Boolean.parseBoolean(s));
					}
					else {
						int nr = Integer.parseInt(s);
						w.add(nr);
					}
				}
				catch(Exception e) {
					w.add(s);
				}
				
			}
			v.add(w);
		}
		return v;
	}
	private Vector<Object> getHeader() {
		String[] test = CompareSequence.getOneLineHeaderArray();
		Vector<Object> v = new Vector<Object>();
		for(String t: test) {
			v.add(t);
		}
		return v;
	}
	@Override
	public void actionPerformed(ActionEvent arg0) {
		if(arg0.getActionCommand().contentEquals("Export to Excel")) {
			jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			jfc.setMultiSelectionEnabled(false);
			if(jfc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION){
				File f = jfc.getSelectedFile();
				//make sure it is an .xlsx file
				System.out.println("File "+f);
				if(!f.getName().endsWith(".xlsx")) {
					f = new File(f.getAbsolutePath()+".xlsx");
				}
				if(f.exists() && !f.renameTo(f)) {
					JOptionPane.showMessageDialog(this,"Excel file is in use, please close it and try again!");
					return;
				}
				
				
				exportToExcel(f);
				this.lastFileSaved = f;
				
				//open it in Excel
				try {
					Desktop.getDesktop().open(f);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}
	}
	public static void runR(File f, boolean inverse) {
		String[] commands = getRCommand(f, inverse);
		Process p;
		try {
			p = Runtime.getRuntime().exec(commands);
			
			// any error message?get
            StreamGobbler errorGobbler = new 
                StreamGobbler(p.getErrorStream(), "ERROR", false);            
            
            // any output?
            StreamGobbler outputGobbler = new 
                StreamGobbler(p.getInputStream(), "OUTPUT", false);
                
            // kick them off
            errorGobbler.start();
            outputGobbler.start();
            //wait until completed
			p.waitFor();
		} catch (InterruptedException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	private static String[] getRCommand(File f, boolean inverse) {
		if(inverse) {
			String[] command = {RscriptCommand, Rscript, f.getAbsolutePath(), "true"}; 
			return command;
		}
		else {
			String[] command = {RscriptCommand, Rscript, f.getAbsolutePath()}; 
			return command;
		}
	}
	private void exportToExcel(File file) {
		XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("rawData");
        CellStyle backgroundStyle = workbook.createCellStyle();
        CellStyle remarkStyle = workbook.createCellStyle();
        remarkStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        remarkStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
        
        Vector<Object> v = getHeader();
        int rowCount = 0;
        Row row = sheet.createRow(rowCount++);
    	int columnCount = 0;
    	int remarkColumn = -1;
    	int fileColumn = -1;
    	int counter = 0;
        for(Object o: v) {
        	//get the indexes
        	if(o instanceof String) {
        		if(o.toString().contentEquals("Remarks")) {
        			remarkColumn = counter;
        		}
        		else if(o.toString().contentEquals("File")) {
        			fileColumn = counter;
        		}
        	}
        	//only place header if needed
        	if(removeColumns[counter]) {
	        	Cell cell = row.createCell(columnCount++);
	        	if(o instanceof String) {
	        		cell.setCellValue((String)o);
	        	}
	        	else if(o instanceof Integer) {
	        		cell.setCellValue((Integer)o);
	        	}
        	}
        	counter++;
        }
        Vector<Vector<Object>> w = this.getResultVector();
        //System.out.println("remarkColumn "+remarkColumn);
        for(Vector<Object> rowV :w) {
        	row = sheet.createRow(rowCount++);
        	columnCount = 0;
        	boolean remarkRow = rowV.get(remarkColumn).toString().length()>0;
        	counter = 0;
        	for(Object o: rowV){
        		if(removeColumns[counter]) {
	        		Cell cell = row.createCell(columnCount);
	        		if(counter == fileColumn) {
	        			Hyperlink href = workbook.getCreationHelper().createHyperlink(HyperlinkType.FILE);
	        			File f = new File((String)o);
	        			href.setAddress(f.toURI().toString());
	        			cell.setHyperlink(href);
	        			cell.setCellValue(f.getName());
	        		}
	        		else {
		            	if(o instanceof String) {
		            		String s = (String)o;
		            		if(s.length()>32767) {
		            			s = s.substring(0, 32767);
		            		}
		            		cell.setCellValue(s);
		            	}
		            	else if(o instanceof Integer) {
		            		cell.setCellValue((Integer)o);
		            	}
		            	else if(o instanceof Boolean) {
		            		cell.setCellValue((Boolean)o);
		            	}
	        		}
	        		if(remarkRow) {
	            		cell.setCellStyle(remarkStyle);
	            	}
	            	else {
	            		cell.setCellStyle(backgroundStyle);
	            	}
	            	columnCount++;
        		}
        		counter++;
        	}
        }
        
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            workbook.write(outputStream);
        } catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        try {
			workbook.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void setFileChooser(JFileChooser jfc) {
		this.jfc = jfc;
	}
	public void setLeftFlank(String left) {
		this.leftFlank = left;
		
	}
	public void setRightFlank(String right) {
		this.rightFlank = right;
		
	}
	public void setMasked(boolean maskLowQualityRemove) {
		this.masked = maskLowQualityRemove;
	}
	public void setErrorRate(double error) {
		this.maxError = error;
	}
	public void removeColumns(boolean[] outputColumns) {
		this.removeColumns = outputColumns;
		TableColumnModel tcm = jt.getColumnModel();
		for(int col=0;col<outputColumns.length;col++) {
			//bug sometimets the columns in the model are fewer than the outputColumns
			if(!outputColumns[col] && tcm.getColumnCount()>col) {
				tcm.removeColumn(tcm.getColumn(col));
			}
		}
	}
	public void setSplit(boolean split) {
		this.split = split;
	}
}
