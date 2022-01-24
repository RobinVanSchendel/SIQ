package gui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JTable;
import javax.swing.TransferHandler;

final class FileDropHandler extends TransferHandler {
    @Override
    public boolean canImport(TransferHandler.TransferSupport support) {
        for (DataFlavor flavor : support.getDataFlavors()) {
            if (flavor.isFlavorJavaFileListType()) {
                return true;
            }
        }
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean importData(TransferHandler.TransferSupport support) {
        if (!this.canImport(support))
            return false;

        List<File> files;
        try {
            files = (List<File>) support.getTransferable()
                    .getTransferData(DataFlavor.javaFileListFlavor);
        } catch (UnsupportedFlavorException | IOException ex) {
            // should never happen (or JDK is buggy)
            return false;
        }
        JTable.DropLocation dl = (JTable.DropLocation) support.getDropLocation();
        int row = dl.getRow();
        int column = dl.getColumn();
        JTable jt = (JTable)support.getComponent();
        NGSTableModel ngsmodel = (NGSTableModel)jt.getModel();
        for(File f: files) {
        	//add row if required
        	if(row>=ngsmodel.getRowCount()) {
        		NGS ngs = new NGS();
        		ngsmodel.addNGS(ngs);
        	}
        	ngsmodel.setValueAt( f.getAbsolutePath(), row, column);
        	row++;
        }
        return true;
    }
}