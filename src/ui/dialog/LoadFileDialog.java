package ui.dialog;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Dialog used to load an external file into our File System
 */
//TODO: implement this
public class LoadFileDialog extends JDialog {


    private String filePath, fileName;

    public LoadFileDialog() {

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints cs = new GridBagConstraints();
        //padding
        cs.insets = new Insets(8, 15, 8, 15);
        cs.fill = GridBagConstraints.HORIZONTAL;

        //folder chooser
        JLabel fileChooserLabel = new JLabel("Load a file:");
        cs.gridx = 0;
        cs.gridy = 0;
        cs.gridwidth = 1;
        panel.add(fileChooserLabel, cs);

        JTextField fileChooserTF = new JTextField(20);
        fileChooserTF.setEditable(false);
        cs.gridx = 1;
        cs.gridy = 0;
        cs.gridwidth = 2;
        panel.add(fileChooserTF, cs);
        panel.setBorder(new LineBorder(Color.GRAY));

        JButton chooseFileBtn = new JButton("Choose");
        cs.gridx = 3;
        cs.gridy = 0;
        cs.gridwidth = 2;
        panel.add(chooseFileBtn, cs);
        panel.setBorder(new LineBorder(Color.GRAY));
        chooseFileBtn.addActionListener((ActionListener) -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new java.io.File("."));
            chooser.setDialogTitle("Select the working directory");
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setAcceptAllFileFilterUsed(false);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                this.filePath = chooser.getSelectedFile().toString();
                this.fileName = chooser.getSelectedFile().getName();
                System.out.println("Selected Directory : " + chooser.getSelectedFile());
                fileChooserTF.setText(filePath);
            } else {
                System.out.println("No Directory selected");
            }
        });

        //confirm button
        JButton confirmBtn = new JButton("Confirm");
        confirmBtn.addActionListener((ActionListener) -> {

            this.filePath = fileChooserTF.getText();
            if (filePath.equals("") || fileName.equals(""))
                showErrorMessage();
            else {
                System.out.println("Selected File = " + fileName + " from: " + filePath);

                //Write file to FS
                System.out.println("Wrote file: " + fileName);

                //launch the main ui
                dispose();

            }

        });

        JPanel bp = new JPanel();
        bp.add(confirmBtn);

        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(bp, BorderLayout.PAGE_END);

        pack();
        setVisible(true);
        setResizable(false);
        setLocationRelativeTo(null);

    }

    private void showErrorMessage() {
        JOptionPane.showMessageDialog(this,
                "You have to provide some info to go forward",
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }
}
