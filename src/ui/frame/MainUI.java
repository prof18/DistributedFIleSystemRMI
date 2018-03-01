package ui;

import ui.dialog.SettingsDialog;
import ui.utils.FileViewTableModel;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.BorderUIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;

public class MainUI extends JFrame {

    private LogUI logUI;

    private JLabel fileNameVLabel, typeVLabel, pathVLabel, fileSizeVLabel,
            ownerVLabel, lastEditVLabel;

    String[] files = {"Folder1", "Folder2", "File1"};

    public MainUI() {
        super("LR18 FileWrapper System");


        //"Creare" cartella file system se non è presente
        //Guardare se ci sono già le configurazioni nel file system

        //Se non è presente nessuna configurazione, eseguire primo nodo

        //Se è presente

        //Create and show the main UI block
        setLocationRelativeTo(null);
        setSize(1000, 700);
        setVisible(true);
        setLocationRelativeTo(null);
        setVisible(true);
        this.setJMenuBar(createMenuBar());


        System.out.println("Loading config");

        JPanel rightWrapper = new JPanel(new GridBagLayout());

        //FileWrapper UI
        JPanel filesUI = new JPanel(new GridLayout());
        //FileWrapper Details
        JPanel filesDetail = createDetailsUI();
        setLayout(new GridBagLayout());

        //Tree View
        DefaultMutableTreeNode top = new DefaultMutableTreeNode("The Java Series");
        createNodes(top);
        JTree tree = new JTree(top);
        //Only One Selection
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener((TreeSelectionListener) -> {
            System.out.println("Item Selected");
        });
        tree.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane treeScroll = new JScrollPane(tree);

        //Table UI
        FileViewTableModel model = new FileViewTableModel();
        final JTable table = new JTable();
        table.setFillsViewportHeight(true);
        table.setTableHeader(null);
        table.setModel(model);
        table.setRowHeight(table.getRowHeight() + 8);
        model.setColumnData(files);
        table.setShowGrid(false);
         //Set width of the first column
        TableColumn tableColumn = table.getColumnModel().getColumn(0);
        tableColumn.setPreferredWidth(25);
        tableColumn.setMaxWidth(25);
        tableColumn.setMinWidth(25);
        JScrollPane tableScroll = new JScrollPane(table);
        filesUI.add(tableScroll);

        //Constraints for the main UI
        GridBagConstraints globalCS = new GridBagConstraints();
        GridBagConstraints rwCS = new GridBagConstraints();

        rwCS.weightx = 1;
        rwCS.weighty = 0.95;
        rwCS.gridx = 0;
        rwCS.gridy = 0;
        rwCS.fill = GridBagConstraints.BOTH;
        rightWrapper.add(filesUI, rwCS);

        rwCS.weightx = 1;
        rwCS.weighty = 0.05;
        rwCS.gridx = 0;
        rwCS.gridy = 1;
        rwCS.fill = GridBagConstraints.BOTH;
        rightWrapper.add(filesDetail, rwCS);

        globalCS.weighty = 1;
        globalCS.weightx = 0.05;
        globalCS.gridx = 0;
        globalCS.gridy = 0;
        globalCS.fill = GridBagConstraints.BOTH;
        add(treeScroll, globalCS);

        globalCS.weighty = 1;
        globalCS.weightx = 0.95;
        globalCS.gridx = 1;
        globalCS.gridy = 0;
        globalCS.fill = GridBagConstraints.BOTH;
        add(rightWrapper, globalCS);

        GridBagConstraints cs = new GridBagConstraints();
        cs.fill = GridBagConstraints.BOTH;
    }

    private JPanel createDetailsUI() {

        JPanel filesDetail = new JPanel(new GridBagLayout());
        GridBagConstraints cs = new GridBagConstraints();
        cs.insets = new Insets(5, 10, 5, 70);

        //FileWrapper Name
        JLabel fileNameLabel = new JLabel("FileWrapper Name: ");
        fileNameLabel.setOpaque(true);
        cs.gridx = 0;
        cs.gridy = 0;
        filesDetail.add(fileNameLabel, cs);
        fileNameVLabel = new JLabel("LR Exam Schedule");
        //cs.fill = GridBagConstraints.BOTH;
        cs.gridx = 1;
        cs.gridy = 0;
        filesDetail.add(fileNameVLabel, cs);

        //Type
        JLabel typeLabel = new JLabel("Type: ");
        cs.gridx = 0;
        cs.gridy = 1;
        filesDetail.add(typeLabel, cs);
        typeVLabel = new JLabel("file");
        cs.gridx = 1;
        cs.gridy = 1;
        filesDetail.add(typeVLabel, cs);

        //Path
        JLabel pathLabel = new JLabel("Path: ");
        cs.gridx = 0;
        cs.gridy = 2;
        filesDetail.add(pathLabel, cs);
        pathVLabel = new JLabel("/home/luca/ansia/");
        cs.gridx = 1;
        cs.gridy = 2;
        filesDetail.add(pathVLabel, cs);

        //FileWrapper Size
        JLabel fileSizeLabel = new JLabel("FileWrapper Size: ");
        cs.gridx = 0;
        cs.gridy = 3;
        filesDetail.add(fileSizeLabel, cs);
        fileSizeVLabel = new JLabel("35kb");
        cs.gridx = 1;
        cs.gridy = 3;
        filesDetail.add(fileSizeVLabel, cs);

        //Owner
        JLabel ownerLabel = new JLabel("Owner: ");
        cs.gridx = 0;
        cs.gridy = 4;
        filesDetail.add(ownerLabel, cs);
        ownerVLabel = new JLabel("LR18");
        cs.gridx = 1;
        cs.gridy = 4;
        filesDetail.add(ownerVLabel, cs);

        //Last Edit
        JLabel lastEditLabel = new JLabel("Last Edit: ");
        cs.gridx = 0;
        cs.gridy = 5;
        filesDetail.add(lastEditLabel, cs);
        lastEditVLabel = new JLabel("Thu Feb 24 17:34:55");
        cs.gridx = 1;
        cs.gridy = 5;
        filesDetail.add(lastEditVLabel, cs);

        //Second Column

        //FileWrapper Name
        JLabel fileNameLabel1 = new JLabel("FileWrapper Name: ");
        cs.gridx = 2;
        cs.gridy = 0;
        filesDetail.add(fileNameLabel1, cs);
        fileNameVLabel = new JLabel("LR Exam Schedule");
        cs.gridx = 3;
        cs.gridy = 0;
        filesDetail.add(fileNameVLabel, cs);

        //Type
        JLabel typeLabel1 = new JLabel("Type: ");
        cs.gridx = 2;
        cs.gridy = 1;
        filesDetail.add(typeLabel1, cs);
        typeVLabel = new JLabel("file");
        cs.gridx = 3;
        cs.gridy = 1;
        filesDetail.add(typeVLabel, cs);

        //Path
        JLabel pathLabel1 = new JLabel("Path: ");
        cs.gridx = 2;
        cs.gridy = 2;
        filesDetail.add(pathLabel1, cs);
        pathVLabel = new JLabel("/home/luca/ansia/");
        cs.gridx = 3;
        cs.gridy = 2;
        filesDetail.add(pathVLabel, cs);

        //FileWrapper Size
        JLabel fileSizeLabel1 = new JLabel("FileWrapper Size: ");
        cs.gridx = 2;
        cs.gridy = 3;
        filesDetail.add(fileSizeLabel1, cs);
        fileSizeVLabel = new JLabel("35kb");
        cs.gridx = 3;
        cs.gridy = 3;
        filesDetail.add(fileSizeVLabel, cs);

        //Owner
        JLabel ownerLabel1 = new JLabel("Owner: ");
        cs.gridx = 2;
        cs.gridy = 4;
        filesDetail.add(ownerLabel1, cs);
        ownerVLabel = new JLabel("LR18");
        cs.gridx = 3;
        cs.gridy = 4;
        filesDetail.add(ownerVLabel, cs);

        //Last Edit
        JLabel lastEditLabel1 = new JLabel("Last Edit: ");
        cs.gridx = 2;
        cs.gridy = 5;
        filesDetail.add(lastEditLabel1, cs);
        lastEditVLabel = new JLabel("Thu Feb 24 17:34:55");
        cs.gridx = 3;
        cs.gridy = 5;
        filesDetail.add(lastEditVLabel, cs);

        return filesDetail;

    }

    private void createNodes(DefaultMutableTreeNode top) {
        DefaultMutableTreeNode category = null;
        DefaultMutableTreeNode book = null;

        category = new DefaultMutableTreeNode("Books for Java Programmers");
        top.add(category);

        //original Tutorial
        book = new DefaultMutableTreeNode("The Java Tutorial: A Short Course on the Basics");
        category.add(book);

        //Tutorial Continued
        book = new DefaultMutableTreeNode("The Java Tutorial Continued: The Rest of the JDK");
        category.add(book);

        //JFC Swing Tutorial
        book = new DefaultMutableTreeNode("The JFC Swing Tutorial: A Guide to Constructing GUIs");
        category.add(book);

        //Bloch
        book = new DefaultMutableTreeNode("Effective Java Programming Language Guide");
        category.add(book);

        //Arnold/Gosling
        book = new DefaultMutableTreeNode("The Java Programming Language");
        category.add(book);

        //Chan
        book = new DefaultMutableTreeNode("The Java Developers Almanac");
        category.add(book);

        category = new DefaultMutableTreeNode("Books for Java Implementers");
        top.add(category);

        //VM
        book = new DefaultMutableTreeNode("The Java Virtual Machine Specification");
        category.add(book);
    }

    /**
     * Creation of the Menu Bar
     *
     * @return The JMenuBar Object
     */
    private JMenuBar createMenuBar() {
        //Create the menu bar.
        JMenuBar menuBar = new JMenuBar();

        //FileWrapper Menu.
        JMenu menu = new JMenu("FileWrapper");
        //Open
        JMenuItem menuItem = new JMenuItem("Open");
        menuItem.addActionListener((ActionListener) -> {
            System.out.println("Clicked Open");
        });
        menu.add(menuItem);
        //New FileWrapper
        menuItem = new JMenuItem("New FileWrapper");
        menuItem.addActionListener((ActionListener) -> {
            System.out.println("Clicked New FileWrapper");
        });
        menu.add(menuItem);
        //New Folder
        menuItem = new JMenuItem("New Folder");
        menuItem.addActionListener((ActionListener) -> {
            System.out.println("Clicked New Folder");
        });
        menu.add(menuItem);
        menu.addSeparator();
        //Settings
        menuItem = new JMenuItem("Settings");
        menuItem.addActionListener((ActionListener) -> {
            SettingsDialog settingsDialog = new SettingsDialog();
            settingsDialog.setVisible(true);

        });
        menu.add(menuItem);
        //About
        menuItem = new JMenuItem("About");
        menuItem.addActionListener((ActionListener) -> {
            System.out.println("Clicked About");
        });
        menu.add(menuItem);
        menuBar.add(menu);

        //Edit Menu
        menu = new JMenu("Edit");
        //Rename
        menuItem = new JMenuItem("Rename");
        menuItem.addActionListener((ActionListener) -> {
            System.out.println("Clicked Rename");
        });
        menu.add(menuItem);
        //Delete
        menuItem = new JMenuItem("Delete");
        menuItem.addActionListener((ActionListener) -> {
            System.out.println("Clicked Delete");
        });
        menu.add(menuItem);
        //Move
        menuItem = new JMenuItem("Move");
        menuItem.addActionListener((ActionListener) -> {
            System.out.println("Clicked Move");
        });
        menu.add(menuItem);
        menuBar.add(menu);

        //Tools Menu
        menu = new JMenu("Tools");
        //Show Log
        JCheckBoxMenuItem cbMenuItem = new JCheckBoxMenuItem("Show Log");
        cbMenuItem.setState(true);
        cbMenuItem.addActionListener((ActionListener) -> {
            if (cbMenuItem.getState()) {
                logUI.setVisible(true);
                System.out.println("Log enabled");
            } else {
                logUI.setVisible(false);
                System.out.println("Log disabled");
            }
            System.out.println("Clicked Show Log");
        });
        menu.add(cbMenuItem);
        //Say Hello
        menuItem = new JMenuItem("Say Hello");
        menuItem.addActionListener((ActionListener) -> {
            System.out.println("Clicked Say Hello");
        });
        menu.add(menuItem);
        menuBar.add(menu);

        //Navigate Folder Up
        menuBar.add(Box.createHorizontalGlue());
        JButton navigateUp = new JButton("Navigate Up");
        navigateUp.addActionListener((ActionListener) -> {
            System.out.println("Clicked Action Up");
        });
        menuBar.add(navigateUp);

        return menuBar;
    }


}

