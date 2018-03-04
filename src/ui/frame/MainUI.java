package ui.frame;

import fs.actions.FSStructure;
import fs.objects.json.JsonFolder;
import fs.objects.structure.FSTreeNode;
import fs.objects.structure.FileWrapper;
import ui.dialog.SettingsDialog;
import ui.utility.DelegateAction;
import ui.utility.FileViewTableModel;
import ui.utility.TableItem;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;

public class MainUI extends JFrame {

    private LogUI logUI;

    private JLabel fileNameVLabel, typeVLabel, pathVLabel, fileSizeVLabel, ownerVLabel, lastEditVLabel;
    private JLabel info1VLabel, info2VLabel, info3VLabel, info4VLabel, info5VLabel, info6VLabel;

    String[] files = {"Folder1", "Folder2", "File1"};

    HashMap<String, JsonFolder> foldersMap;
    private FSStructure fsStructure;
    private FSTreeNode directoryTree;

    private JButton navigateUpBtn;

    private JTable table;

    public MainUI() {
        super("LR18 File System");

        //Create and show the main UI block
        setLocationRelativeTo(null);
        setSize(1050, 700);
        setVisible(true);
        setLocationRelativeTo(null);
        setVisible(true);
        this.setJMenuBar(createMenuBar());

        //Loading file system structure
        System.out.println("Loading structure");
        fsStructure = FSStructure.getInstance();
        fsStructure.generateTreeStructure();
        FSTreeNode directoryTree = fsStructure.getTree();
        System.out.println(directoryTree.printTree());
        //if root, disable the navigate up button
        if (directoryTree.isRoot())
            navigateUpBtn.setEnabled(false);

        //Generate Tree View

        //Generate details view

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
        this.table = table;
        table.setFillsViewportHeight(true);
        table.setTableHeader(null);
        table.setModel(model);
        table.setRowHeight(table.getRowHeight() + 8);
        model.setNode(directoryTree);
        table.setShowGrid(false);
        //Set width of the first column
        TableColumn tableColumn = table.getColumnModel().getColumn(0);
        tableColumn.setPreferredWidth(25);
        tableColumn.setMaxWidth(25);
        tableColumn.setMinWidth(25);
        JScrollPane tableScroll = new JScrollPane(table);
        filesUI.add(tableScroll);
        //table listener with enter
        KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        table.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(enter, "enter");
        table.getActionMap().put("enter", new DelegateAction(action -> changeTableView(false)));
        //table listener with double click
        table.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent mouseEvent) {
                JTable table = (JTable) mouseEvent.getSource();
                Point point = mouseEvent.getPoint();
                int row = table.rowAtPoint(point);
                if (mouseEvent.getClickCount() == 2 && row != -1) {
                    // your valueChanged overridden method
                    System.out.println("double click");
                    changeTableView(false);
                }
            }
        });
        table.getSelectionModel().addListSelectionListener((ListSelectionListener) -> {
            System.out.println("Selected item");
            int row = table.getSelectedRow();
            TableItem item = model.getItems().get(row);
        });

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

    private void changeTableView(boolean goingUp) {
        System.out.println("Enter clicked");
        FileViewTableModel model = (FileViewTableModel) table.getModel();
        if (!goingUp) {
            int row = table.getSelectedRow();
            TableItem item = model.getItems().get(row);
            if (item.isFile()) {
                //open the file
                openFile(item.getFileWrapper());
            } else {
                //update the table with the new directory
                FSTreeNode node = item.getTreeNode();
                if (!node.isRoot())
                    navigateUpBtn.setEnabled(true);
                model.setNode(node);
            }
        } else {
            FSTreeNode node = model.getCurrentTreeNode();
            if (node.getParent().isRoot())
                navigateUpBtn.setEnabled(false);
            model.setNode(node.getParent());
        }
    }

    private void openFile(FileWrapper fileWrapper) {

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

    private JPanel createDetailsUI() {

        JPanel filesDetail = new JPanel(new GridBagLayout());
        GridBagConstraints cs = new GridBagConstraints();
        cs.insets = new Insets(5, 10, 5, 70);

        //FileWrapper Name
        JLabel fileNameLabel = new JLabel("Name: ");
        fileNameLabel.setOpaque(true);
        cs.gridx = 0;
        cs.gridy = 0;
        filesDetail.add(fileNameLabel, cs);
        fileNameVLabel = new JLabel("-");
        //cs.fill = GridBagConstraints.BOTH;
        cs.gridx = 1;
        cs.gridy = 0;
        filesDetail.add(fileNameVLabel, cs);

        //Type
        JLabel typeLabel = new JLabel("Type: ");
        cs.gridx = 0;
        cs.gridy = 1;
        filesDetail.add(typeLabel, cs);
        typeVLabel = new JLabel("-");
        cs.gridx = 1;
        cs.gridy = 1;
        filesDetail.add(typeVLabel, cs);

        //Path
        JLabel pathLabel = new JLabel("Path: ");
        cs.gridx = 0;
        cs.gridy = 2;
        filesDetail.add(pathLabel, cs);
        pathVLabel = new JLabel("-");
        cs.gridx = 1;
        cs.gridy = 2;
        filesDetail.add(pathVLabel, cs);

        //FileWrapper Size
        JLabel fileSizeLabel = new JLabel("Size: ");
        cs.gridx = 0;
        cs.gridy = 3;
        filesDetail.add(fileSizeLabel, cs);
        fileSizeVLabel = new JLabel("-");
        cs.gridx = 1;
        cs.gridy = 3;
        filesDetail.add(fileSizeVLabel, cs);

        //Owner
        JLabel ownerLabel = new JLabel("Owner: ");
        cs.gridx = 0;
        cs.gridy = 4;
        filesDetail.add(ownerLabel, cs);
        ownerVLabel = new JLabel("-");
        cs.gridx = 1;
        cs.gridy = 4;
        filesDetail.add(ownerVLabel, cs);

        //Last Edit
        JLabel lastEditLabel = new JLabel("Last Edit: ");
        cs.gridx = 0;
        cs.gridy = 5;
        filesDetail.add(lastEditLabel, cs);
        lastEditVLabel = new JLabel("-");
        cs.gridx = 1;
        cs.gridy = 5;
        filesDetail.add(lastEditVLabel, cs);

        //Second Column

        //FileWrapper Name
        JLabel info1Label = new JLabel("Info1: ");
        cs.gridx = 2;
        cs.gridy = 0;
        filesDetail.add(info1Label, cs);
        info1VLabel = new JLabel("-");
        cs.gridx = 3;
        cs.gridy = 0;
        filesDetail.add(info1VLabel, cs);

        //Type
        JLabel info2Label = new JLabel("Info2: ");
        cs.gridx = 2;
        cs.gridy = 1;
        filesDetail.add(info2Label, cs);
        info2VLabel = new JLabel("-");
        cs.gridx = 3;
        cs.gridy = 1;
        filesDetail.add(info2VLabel, cs);

        //Path
        JLabel info3Label = new JLabel("Info3: ");
        cs.gridx = 2;
        cs.gridy = 2;
        filesDetail.add(info3Label, cs);
        info3VLabel = new JLabel("-");
        cs.gridx = 3;
        cs.gridy = 2;
        filesDetail.add(info3VLabel, cs);

        //FileWrapper Size
        JLabel info4Label = new JLabel("Info4: ");
        cs.gridx = 2;
        cs.gridy = 3;
        filesDetail.add(info4Label, cs);
        info4VLabel = new JLabel("-");
        cs.gridx = 3;
        cs.gridy = 3;
        filesDetail.add(info4VLabel, cs);

        //Owner
        JLabel info5Label = new JLabel("Info5: ");
        cs.gridx = 2;
        cs.gridy = 4;
        filesDetail.add(info5Label, cs);
        info5VLabel = new JLabel("-");
        cs.gridx = 3;
        cs.gridy = 4;
        filesDetail.add(info5VLabel, cs);

        //Last Edit
        JLabel info6Label = new JLabel("Info6: ");
        cs.gridx = 2;
        cs.gridy = 5;
        filesDetail.add(info6Label, cs);
        info6VLabel = new JLabel("-");
        cs.gridx = 3;
        cs.gridy = 5;
        filesDetail.add(info6VLabel, cs);

        return filesDetail;

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
        JMenu menu = new JMenu("File");
        //Open
        JMenuItem menuItem = new JMenuItem("Open");
        menuItem.addActionListener((ActionListener) -> {
            System.out.println("Clicked Open");
        });
        menu.add(menuItem);
        //New FileWrapper
        menuItem = new JMenuItem("New File");
        menuItem.addActionListener((ActionListener) -> {
            System.out.println("Clicked New FileWrapper");
        });
        menu.add(menuItem);
        //New JsonFolder
        menuItem = new JMenuItem("New Folder");
        menuItem.addActionListener((ActionListener) -> {
            System.out.println("Clicked New JsonFolder");
        });
        menu.add(menuItem);
        menu.addSeparator();
        //Settings
        menuItem = new JMenuItem("Settings");
        menuItem.addActionListener((ActionListener) -> {
            SettingsDialog settingsDialog = new SettingsDialog(false);
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

        //Navigate JsonFolder Up
        menuBar.add(Box.createHorizontalGlue());
        navigateUpBtn = new JButton("Navigate Up");
        navigateUpBtn.addActionListener((ActionListener) -> {
            System.out.println("Clicked Action Up");
            //navigate up
            changeTableView(true);
        });
        menuBar.add(navigateUpBtn);

        return menuBar;
    }


}


