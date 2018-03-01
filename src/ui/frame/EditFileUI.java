package ui.frame;

import ui.MainUI;

import javax.swing.*;
import java.awt.*;

public class EditFileUI extends JFrame {

    public EditFileUI(MainUI mainUI) {
        super("Edit File");

        setSize(600, 600);
        setLocationRelativeTo(mainUI);
        setVisible(true);

        setJMenuBar(createMenuBar());

        JTextArea textArea = new JTextArea();
        textArea.setColumns(20);
        textArea.setLineWrap(true);
        textArea.setRows(5);
        textArea.setWrapStyleWord(true);
        textArea.setMargin(new Insets(10, 10, 10, 10));
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
        });
        menu.add(menuItem);
        //Exit
        menuItem = new JMenuItem("Exit");
        menuItem.addActionListener((ActionListener) -> {
            dispose();
        });
        menu.add(menuItem);

        menuBar.add(menu);

        return menuBar;
    }
}
