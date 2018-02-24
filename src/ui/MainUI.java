package ui;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;

public class MainUI extends JFrame {

    private LogUI logUI;

    private JLabel fileNameVLabel, typeVLabel, pathVLabel, fileSizeVLabel,
            ownerVLabel, lastEditVLabel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainUI mainUI = new MainUI();
        });
    }

    public MainUI() {
        super("LR18 File System");
        //Create and show the main UI block
        setLocationRelativeTo(null);
        setSize(1000, 700);
        setVisible(true);
        setLocationRelativeTo(null);
        setVisible(true);
        this.setJMenuBar(createMenuBar());

        logUI = new LogUI(this);
        System.out.println("Loading config");

        JPanel rightWrapper = new JPanel(new GridBagLayout());

        //File UI
        JPanel filesUI = new JPanel();
        filesUI.setBackground(Color.BLUE);
        //File Details
        JPanel filesDetail = createDetailsUI();
        filesDetail.setBackground(Color.YELLOW);
        setLayout(new GridBagLayout());

        DefaultMutableTreeNode top =
                new DefaultMutableTreeNode("The Java Series");
        createNodes(top);

        //Create a tree that allows one selection at a time.
        JTree tree = new JTree(top);
        tree.getSelectionModel().setSelectionMode
                (TreeSelectionModel.SINGLE_TREE_SELECTION);

        //Listen for when the selection changes.
        //tree.addTreeSelectionListener(this);

        JScrollPane treeScroll = new JScrollPane(tree);


        // as per trashgod tip
    //    tree.setVisibleRowCount(15);

        /*Dimension preferredSize = treeScroll.getPreferredSize();
        Dimension widePreferred = new Dimension(
                200,
                (int)preferredSize.getHeight());
        treeScroll.setPreferredSize( widePreferred );
*/



        GridBagConstraints c = new GridBagConstraints();
        GridBagConstraints c1 = new GridBagConstraints();

        c1.gridx = 0;
        c1.gridy = 0;
        c1.fill = GridBagConstraints.BOTH;
        rightWrapper.add(filesUI, c1);

        c1.gridx = 0;
        c1.gridy = 1;
        c1.fill = GridBagConstraints.BOTH;
        rightWrapper.add(filesDetail, c1);

        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        add(treeScroll, c);

        c.gridx = 1;
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        add(rightWrapper, c);

      /*  c.gridx = 0;
       // c.weighty = 1;

        c.weightx = 0.2;
    //    c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        add(treeScroll, c);*/

/*      c1.fill = GridBagConstraints.BOTH;
      c1.weighty = 1;
      c1.gridheight = 1;
      add(treeScroll, c1);

        c.weightx = 0.8;
        // Files UI
        c.gridx = 1;
        c.weighty = 0.9;
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        add(filesUI, c);

        //Details UI
        c.gridx = 1;
        c.weighty = 0.1;
        c.gridy = 1;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.WEST;
        add(filesDetail, c);*/

        //TODO: Enable the settings pop-up
/*
        SettingsDialog settingsDialog = new SettingsDialog(this);
        settingsDialog.setVisible(true);
*/
    }

    private JPanel createDetailsUI() {

        JPanel filesDetail = new JPanel(new GridBagLayout());
        GridBagConstraints cs = new GridBagConstraints();
        //cs.fill = GridBagConstraints.VERTICAL;
        cs.insets = new Insets(5, 10, 5, 10);

        //cs.anchor = GridBagConstraints.EAST;

        //File Name
        JLabel fileNameLabel = new JLabel("File Name: ");
        cs.gridx = 0;
        cs.gridy = 0;
        filesDetail.add(fileNameLabel, cs);
        fileNameVLabel = new JLabel("LR Exam Schedule");
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

        //File Size
        JLabel fileSizeLabel = new JLabel("File Size: ");
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

        return filesDetail;

    }





        public JTree TreeDemo() {


            DefaultMutableTreeNode top =
                    new DefaultMutableTreeNode("The Java Series");
            createNodes(top);

            //Create a tree that allows one selection at a time.
            JTree tree = new JTree(top);
            tree.getSelectionModel().setSelectionMode
                    (TreeSelectionModel.SINGLE_TREE_SELECTION);

            //Listen for when the selection changes.
            //tree.addTreeSelectionListener(this);


            JScrollPane treeScroll = new JScrollPane(tree);

            // as per trashgod tip
            tree.setVisibleRowCount(15);

            Dimension preferredSize = treeScroll.getPreferredSize();
            Dimension widePreferred = new Dimension(
                    200,
                    (int)preferredSize.getHeight());
            treeScroll.setPreferredSize( widePreferred );

            //Create the scroll pane and add the tree to it.
            //JScrollPane treeView = new JScrollPane(tree);

 /*           //Create the HTML viewing pane.
            JEditorPane htmlPane = new JEditorPane();
            htmlPane.setEditable(false);
            //initHelp();
            JScrollPane htmlView = new JScrollPane(htmlPane);

            //Add the scroll panes to a split pane.
            JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
            splitPane.setTopComponent(treeView);
            splitPane.setBottomComponent(htmlView);

            Dimension minimumSize = new Dimension(100, 50);
            htmlView.setMinimumSize(minimumSize);
            treeView.setMinimumSize(minimumSize);
            splitPane.setDividerLocation(100);
            splitPane.setPreferredSize(new Dimension(500, 300));
*/
            //Add the split
            return tree;
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

        //File Menu.
        JMenu menu = new JMenu("File");
        //Open
        JMenuItem menuItem = new JMenuItem("Open");
        menuItem.addActionListener((ActionListener) -> {
            System.out.println("Clicked Open");
        });
        menu.add(menuItem);
        //New File
        menuItem = new JMenuItem("New File");
        menuItem.addActionListener((ActionListener) -> {
            System.out.println("Clicked New File");
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
            System.out.println("CLicked Settings");
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

        return menuBar;
    }
}