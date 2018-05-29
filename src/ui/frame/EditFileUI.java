package ui.frame;

import fs.actions.FSStructure;
import fs.actions.interfaces.FileService;
import fs.objects.structure.FSTreeNode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileNotFoundException;

import static javax.swing.JOptionPane.showMessageDialog;

/**
 * Generate a window to edit a file
 */
public class EditFileUI extends JFrame {

    private FileService fileService;
    private String fileID;
    private JTextArea textArea;
    private String[] filePath;
    private boolean canWrite;


    public EditFileUI(MainUI mainUI, String text, FileService fileService, String fileID, String filePath, boolean canWrite) {
        this.fileService = fileService;
        this.fileID = fileID;
        this.filePath = filePath.split("/");
        this.canWrite = canWrite;

        setSize(600, 600);
        setLocationRelativeTo(mainUI);
        setVisible(true);

        setJMenuBar(createMenuBar());

        textArea = new JTextArea();
        textArea.setColumns(20);
        textArea.setLineWrap(true);
        textArea.setRows(5);
        textArea.setWrapStyleWord(true);
        textArea.setMargin(new Insets(10, 10, 10, 10));
        textArea.setText(text);
        JScrollPane jScrollPane1 = new JScrollPane(textArea);

        add(jScrollPane1);

        if (canWrite)
            setTitle("Edit File Mode");
        else {
            setTitle("Read File Mode");

            textArea.setEditable(false);
            textArea.setEnabled(false);

            showMessageDialog(this,
                    "Another user is editing this file. You can't do any modification",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
        }

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                fileService.close(fileID);
            }
        });

    }


    /**
     * Creation of the Menu Bar
     *
     * @return The JMenuBar Object
     */
    private JMenuBar createMenuBar() {
        //Create the menu bar.
        JMenuBar menuBar = new JMenuBar();

        //File Menu.
        JMenu menu = new JMenu("File");
        //Save
        JMenuItem menuItem = new JMenuItem("Save");
        menuItem.addActionListener((ActionListener) -> save());
        menu.add(menuItem);
        if (!canWrite)
            menuItem.setEnabled(false);
        //Exit
        menuItem = new JMenuItem("Exit");
        menuItem.addActionListener((ActionListener) -> dispose());
        menu.add(menuItem);
        menuBar.add(menu);

        //Save Button
        menuBar.add(Box.createHorizontalGlue());
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener((ActionListener) -> save());
        menuBar.add(saveButton);

        if (!canWrite)
            saveButton.setEnabled(false);
        return menuBar;
    }

    private void save() {
        byte[] content = textArea.getText().getBytes();
        try {
            String fileDirectoryName = filePath[filePath.length - 2];
            FSTreeNode root = FSStructure.getInstance().getTree();
            String fileDirectoryUFID = root.getUFID();
            if (fileDirectoryName.compareTo("") != 0) {
                fileDirectoryUFID = root.findNodeByName(root, fileDirectoryName).getUFID();
            }
            fileService.write(fileID, 0, content.length, content, fileDirectoryUFID);
            dispose();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
