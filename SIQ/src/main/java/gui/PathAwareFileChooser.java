package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

/**
 * JFileChooser with an editable path bar at the top.
 * Users can type or paste any absolute path and press Enter to navigate there.
 */
public class PathAwareFileChooser extends JFileChooser {

    private final JTextField pathField = new JTextField();

    public PathAwareFileChooser() {
        setup();
    }

    public PathAwareFileChooser(File dir) {
        super(dir);
        setup();
    }

    private void setup() {
        // keep the path field in sync when the user browses normally
        addPropertyChangeListener(DIRECTORY_CHANGED_PROPERTY, e -> {
            File dir = getCurrentDirectory();
            if (dir != null) pathField.setText(dir.getAbsolutePath());
        });
        addPropertyChangeListener(SELECTED_FILE_CHANGED_PROPERTY, e -> {
            File f = getSelectedFile();
            if (f != null) pathField.setText(f.getAbsolutePath());
        });

        pathField.setToolTipText("Type or paste a path and press Enter");
        pathField.setFont(pathField.getFont().deriveFont(12f));

        pathField.addActionListener(e -> navigateTo(pathField.getText().trim()));

        // also navigate when focus leaves the field
        pathField.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) {
                navigateTo(pathField.getText().trim());
            }
        });

        JLabel lbl = new JLabel("Path: ");
        lbl.setFont(lbl.getFont().deriveFont(12f));

        JPanel bar = new JPanel(new BorderLayout(4, 0));
        bar.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        bar.add(lbl, BorderLayout.WEST);
        bar.add(pathField, BorderLayout.CENTER);

        add(bar, BorderLayout.NORTH);
    }

    private void navigateTo(String text) {
        if (text.isEmpty()) return;
        File f = new File(text);
        if (f.isDirectory()) {
            setCurrentDirectory(f);
        } else if (f.isFile()) {
            setCurrentDirectory(f.getParentFile());
            setSelectedFile(f);
        }
        // if neither, leave the chooser where it is
    }

    @Override
    public void approveSelection() {
        File f = getSelectedFile();
        if (f != null && f.exists() && getDialogType() == SAVE_DIALOG) {
            int result = JOptionPane.showConfirmDialog(this,
                    "The file exists, overwrite?", "Existing file",
                    JOptionPane.YES_NO_CANCEL_OPTION);
            switch (result) {
                case JOptionPane.YES_OPTION: super.approveSelection(); return;
                case JOptionPane.NO_OPTION:  return;
                default: cancelSelection(); return;
            }
        }
        super.approveSelection();
    }
}
