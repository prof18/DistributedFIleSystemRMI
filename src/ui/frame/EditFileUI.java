package ui.frame;

import fs.actions.interfaces.FileService;
import fs.objects.structure.FSTreeNode;

import javax.swing.*;
import java.awt.*;
import java.io.FileNotFoundException;

/**
 * Generate a window to edit a file
 */
public class EditFileUI extends JFrame {

    private FileService fileService;
    private String fileID;
    private JTextArea textArea;
    private String[] filePath;


    public EditFileUI(MainUI mainUI, String text, FileService fileService, String fileID, String filePath) {
        super("Edit File");
        this.fileService = fileService;
        this.fileID = fileID;
        this.filePath = filePath.split("/");


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
        menuItem.addActionListener((ActionListener) -> {
            System.out.println("Clicked Save");
            byte[] content = textArea.getText().getBytes();
            try {
                String fileDirectoryName = filePath[filePath.length-2];
                System.out.println("File directory name: " + fileDirectoryName);
                fileService.write(fileID, 0, content.length, content, fileDirectoryName);
                dispose();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        });
        menu.add(menuItem);
        //Exit
        menuItem = new JMenuItem("Exit");
        menuItem.addActionListener((ActionListener) -> dispose());
        menu.add(menuItem);

        menuBar.add(menu);

        return menuBar;
    }
}
