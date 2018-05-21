package ui.frame;

import fs.actions.DirectoryServiceImpl;
import fs.actions.FSStructure;
import fs.actions.FileServiceUtil;
import fs.actions.interfaces.DirectoryService;
import fs.actions.interfaces.FileService;
import fs.actions.object.ReadWrapper;
import fs.actions.object.WrapperFileServiceUtil;
import fs.objects.structure.FSTreeNode;
import fs.objects.structure.FileWrapper;
import mediator_fs_net.MediatorFsNet;
import net.objects.NetNodeLocation;
import net.objects.interfaces.NetNode;
import ui.utility.*;
import utils.Constants;
import utils.PropertiesHelper;
import utils.Util;

import javax.swing.*;
import javax.swing.table.TableColumn;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import static javax.swing.JOptionPane.showMessageDialog;

/**
 * Generate the main UI
 */
public class MainUI extends JFrame {

    private JLabel fileNameVLabel, typeVLabel, pathVLabel, fileSizeVLabel, ownerVLabel, lastEditVLabel;

    private JButton navigateUpBtn;
    private static JTable table;
    private static JTree tree;
    private SimpleDateFormat sdf = new SimpleDateFormat("d MMM yyyy HH:mm:ss", getLocale());
    private FSTreeNode currentNode;
    private static FSTreeNode directoryTree;

    private JMenuItem rename, delete;
    private FileViewTableModel model;

    private DirectoryService directoryService;
    private FileService fileService;
    private static FSStructure fsStructure;
    private NetNodeLocation netNodeLocation;
    private NetNode ownNode;
    private JTextArea connectedNodeTextArea;

    private JPanel rightWrapper, rightDownWrapper, filesUI, filesDetail, connectedStatus;

    private JScrollPane treeScroll;

    private boolean isItemCreated = false;

    public void showUI(boolean show) {
        if (show)
            setVisible(true);
        else
            setVisible(false);
    }

    public MainUI() throws NotBoundException, NullPointerException {
        super("Distributed File System");
        //Create and show the main UI block
        setLocationRelativeTo(null);
        setSize(1050, 700);
        setLocationRelativeTo(null);
        this.setJMenuBar(createMenuBar());

        //exit from the program when the X is clicked
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        //A wrapper that contains Files UI box and rightDownWrapper
        rightWrapper = new JPanel(new GridBagLayout());
        //A Wrapper that contains Connected Status and File Details boxes
        rightDownWrapper = new JPanel(new GridBagLayout());
        filesUI = new JPanel(new GridLayout());
        filesDetail = createDetailsUI();
        connectedStatus = createConnectedStatus();
        setLayout(new GridBagLayout());

        //connect the node
        connect();

        //set Window Title
        this.setTitle(this.getTitle() + " - Address: " + netNodeLocation.getIp() + " | Port: " + netNodeLocation.getPort()
                + " | Hostname: " + netNodeLocation.getName() + " | Username: " + PropertiesHelper.getInstance().loadConfig(Constants.USERNAME_CONFIG));

        //Loading file system structure
        System.out.println("Loading structure");
        fsStructure = FSStructure.getInstance();
        directoryService = DirectoryServiceImpl.getInstance();
        directoryService.setFileService(fileService);
        fsStructure.generateTreeStructure();
        //Get the structure of the File System
        directoryTree = fsStructure.getTree();
        currentNode = directoryTree;
        //if root, disable the navigate up button
        if (directoryTree.isRoot())
            navigateUpBtn.setEnabled(false);

        //Generate Tree View
        drawTreeView();

        //Generate Table View
        drawTableView();

        //Finalize UI
        setUIConstraints();
    }

    //UI Constraints for the main UI
    private void setUIConstraints() {

        GridBagConstraints globalCS = new GridBagConstraints();
        GridBagConstraints rwCS = new GridBagConstraints();
        GridBagConstraints rdwCS = new GridBagConstraints();

        rwCS.weightx = 1;
        rwCS.weighty = 0.95;
        rwCS.gridx = 0;
        rwCS.gridy = 0;
        rwCS.fill = GridBagConstraints.BOTH;
        rightWrapper.add(filesUI, rwCS);

        rdwCS.weightx = 0.45;
        rdwCS.weighty = 1;
        rdwCS.gridx = 0;
        rdwCS.gridy = 0;
        rdwCS.fill = GridBagConstraints.BOTH;
        rightDownWrapper.add(filesDetail, rdwCS);

        rdwCS.weightx = 0.55;
        rdwCS.weighty = 1;
        rdwCS.gridx = 1;
        rdwCS.gridy = 0;
        rdwCS.fill = GridBagConstraints.BOTH;
        rightDownWrapper.add(connectedStatus, rdwCS);

        rwCS.weightx = 1;
        rwCS.weighty = 0.05;
        rwCS.gridx = 0;
        rwCS.gridy = 1;
        rwCS.fill = GridBagConstraints.BOTH;
        rightWrapper.add(rightDownWrapper, rwCS);

        globalCS.weighty = 1;
        globalCS.weightx = 0.15;
        globalCS.gridx = 0;
        globalCS.gridy = 0;
        globalCS.fill = GridBagConstraints.BOTH;
        add(treeScroll, globalCS);

        globalCS.weighty = 1;
        globalCS.weightx = 0.85;
        globalCS.gridx = 1;
        globalCS.gridy = 0;
        globalCS.fill = GridBagConstraints.BOTH;
        add(rightWrapper, globalCS);

        GridBagConstraints cs = new GridBagConstraints();
        cs.fill = GridBagConstraints.BOTH;
    }

    // Handles the connection of the File System
    private void connect() throws NotBoundException, NullPointerException {
        String ipHost = PropertiesHelper.getInstance().loadConfig(Constants.IP_HOST_CONFIG);
        String nameServiceHost = PropertiesHelper.getInstance().loadConfig(Constants.HOST_NAME_CONFIG);
        String ipRet = PropertiesHelper.getInstance().loadConfig(Constants.IP_FS_CONFIG);
        String path = PropertiesHelper.getInstance().loadConfig(Constants.WORKING_DIR_CONFIG);
        String portRetConfig = PropertiesHelper.getInstance().loadConfig(Constants.PORT_RET_CONFIG);

        System.out.println("checkValue connect()");
        System.out.println("ipHost = " + ipHost);
        System.out.println("nameServiceHost = " + nameServiceHost);
        System.out.println("ipRet = " + ipRet);
        System.out.println("path = " + path);
        System.out.println("portRetConfig = " + portRetConfig);
        System.out.println();
        System.out.println();
        int portRet = -1;
        if (portRetConfig != null && !portRetConfig.equals("")) {
            portRet = Integer.parseInt(portRetConfig);
        }
        NetNodeLocation location;
        if (portRet == -1) {
            System.out.println("The first node doesn't connect to anyone");
            location = null;
        } else {
            location = new NetNodeLocation(ipRet, portRet, nameServiceHost);
            System.out.println("[MAIN] Connection to location = " + location);
        }
        WrapperFileServiceUtil wrapperFS = FileServiceUtil.create(path, ipHost, location, this);
        fileService = wrapperFS.getService();
        netNodeLocation = wrapperFS.getOwnLocation();
        ownNode = wrapperFS.getNetNode();
    }

    // Draws the File Tree View
    private void drawTreeView() {
        FileViewTreeModel treeModel = new FileViewTreeModel(directoryTree);
        tree = new JTree();
        tree.setModel(treeModel);
        //Avoid multiple selections
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener((TreeSelectionListener) -> {
            if (!isItemCreated) {
                //The listener is triggered only when a object is not created
                clearInfo();
                Object o = tree.getLastSelectedPathComponent();
                if (o != null) {
                    if (o instanceof FileWrapper) {
                        //its a file
                        FileWrapper fileWrapper = (FileWrapper) o;
                        setFileInfo(fileWrapper);
                    } else {
                        //its a folder
                        FSTreeNode node = (FSTreeNode) o;
                        TableItem item = new TableItem();
                        item.setTreeNode(node);
                        item.setFile(false);
                        System.out.println("MainUI.drawTreeView ITEM : " + item);
                        changeTableView(false, item);
                        setFolderInfo(node);
                    }
                }
            } else {
                isItemCreated = false;
            }
        });

        tree.setCellRenderer(new

                TreeCellRenderer());

        tree.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        treeScroll = new

                JScrollPane(tree);

    }

    // Draws the File Table View
    private void drawTableView() {
        model = new FileViewTableModel();
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
        table.getActionMap().put("enter", new DelegateAction(action -> changeTableView(false, null)));
        //table listener with double click
        table.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent mouseEvent) {
                JTable table = (JTable) mouseEvent.getSource();
                Point point = mouseEvent.getPoint();
                int row = table.rowAtPoint(point);
                if (row != -1) {
                    //enable rename
                    rename.setEnabled(true);
                    //enable delete
                    delete.setEnabled(true);
                    if (mouseEvent.getClickCount() == 2) {
                        rename.setEnabled(false);
                        delete.setEnabled(false);
                        changeTableView(false, null);
                    }
                }
            }
        });
        table.getSelectionModel().addListSelectionListener((ListSelectionListener) -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                TableItem item = model.getItems().get(row);
                if (item.isFile()) {
                    setFileInfo(item.getFileWrapper());
                } else {
                    setFolderInfo(item.getTreeNode());
                }
            }
        });
    }

    // Shows the info of a file in the File Details Box
    private void setFileInfo(FileWrapper fileWrapper) {
        if (fileWrapper != null) { //secondo me funziona solo se presente il file attribute in locale
            String fileID = fileWrapper.getUFID();
            String filePath = fileWrapper.getPath();
            FSTreeNode root = FSStructure.getInstance().getTree();
            String[] path = filePath.split("/");
            FileWrapper fw;
            if (path.length > 2) {
                String directoryName = path[path.length - 2];
                fw = root.findNodeByName(root, directoryName).getFile(fileID);
            } else {
                fw = root.getFile(fileID);
            }

            if (fw == null) {
                fw = fileWrapper;
            }

            fileNameVLabel.setText(fw.getFileName());
            typeVLabel.setText(fw.getAttribute().getType());
            pathVLabel.setText(fw.getPath());
            fileSizeVLabel.setText(String.valueOf(fw.getAttribute().getFileLength()));
            ownerVLabel.setText(fw.getAttribute().getOwner());
            if (fw.getAttribute().getLastModifiedTime() != null)
                lastEditVLabel.setText(sdf.format(fileWrapper.getAttribute().getLastModifiedTime()));
        }

    }

    // Shows the info of a folder in the File Details Box
    private void setFolderInfo(FSTreeNode node) {
        if (node != null) {
            fileNameVLabel.setText(node.getNameNode());
            typeVLabel.setText("folder");
            pathVLabel.setText(node.getPath());
            fileSizeVLabel.setText(node.getFolderSize());
            lastEditVLabel.setText(sdf.format(node.getLastEditTime()));
        }
    }

    // Clear the File Detail Box
    private void clearInfo() {
        fileNameVLabel.setText("-");
        typeVLabel.setText("-");
        fileSizeVLabel.setText("-");
        pathVLabel.setText("-");
        ownerVLabel.setText("-");
        lastEditVLabel.setText("-");
    }

    // Update the Connected Status Box
    public void updateConnectedNode(HashMap<Integer, NetNodeLocation> connectedNodes) {
        Util.plot(connectedNodes);
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, NetNodeLocation> entry : connectedNodes.entrySet()) {
            NetNodeLocation node = entry.getValue();
            sb.append(node.toString());
            sb.append('\n');
        }
        connectedNodeTextArea.setText(sb.toString());
    }

    // Create the Connected Status Box
    private JPanel createConnectedStatus() {
        JPanel panel = new JPanel();

        connectedNodeTextArea = new JTextArea();
        JLabel label = new JLabel("Node in the system");
        connectedNodeTextArea.setEditable(false);

        JScrollPane pane = new JScrollPane(connectedNodeTextArea);
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weighty = 0.1;
        gbc.weightx = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        connectedNodeTextArea.setMargin(new Insets(10, 10, 10, 10));
        panel.add(label, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weighty = 0.9;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(pane, gbc);
        return panel;
    }

    private void changeTableView(boolean goingUp, TableItem item) {
        clearInfo();
        FileViewTableModel model = (FileViewTableModel) table.getModel();
        System.out.println("MainUI.changeTableView");
        if (!goingUp) {
            int row = table.getSelectedRow();
            //selected table item
            if (item == null)
                item = model.getItems().get(row);
            if (item.isFile()) {
                //open the file
                openFile(item.getFileWrapper());
            } else {
                //update the table with the new directory
                System.out.println("MainUI.changeTableView  ELSE");
                FSTreeNode node = item.getTreeNode();
                System.out.println("MainUI.changeTableView  currentNode : " + currentNode);
                System.out.println("MainUI.changeTableView  Node : " + node);
                currentNode = node;
                if (!node.isRoot())
                    navigateUpBtn.setEnabled(true);
                model.setNode(node);
            }
        } else {
            FSTreeNode node = model.getCurrentTreeNode();
            currentNode = node;
            if (node.getParent().isRoot())
                navigateUpBtn.setEnabled(false);
            model.setNode(node.getParent());
        }


    }

    private JPanel createDetailsUI() {

        JPanel filesDetail = new JPanel(new GridBagLayout());
        GridBagConstraints cs = new GridBagConstraints();
        cs.insets = new Insets(5, 10, 5, 10);

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

        return filesDetail;
    }

    /**
     * This method updates the UI when the File System changes
     *
     * @param treeNode The update FSTreeNode Object
     */
    public static void updateModels(FSTreeNode treeNode, boolean local) {
        System.out.println("MainUI updateModels");

        FileViewTableModel model = (FileViewTableModel) table.getModel();
        model.setNode(treeNode);

        TreePath path = tree.getSelectionPath();
        FileViewTreeModel treeModel = (FileViewTreeModel) tree.getModel();
        System.out.println("DEBUG");
        FSTreeNode root = (FSTreeNode) treeModel.getRoot();
        System.out.println("la radice : " + root.toString());
        for (FSTreeNode figli : root.getChildren()) {
            System.out.println("figlio : " + figli.toString());
        }
        System.out.println("END DEBUG");
        //problema in questo punto
        tree.setModel(null);
        System.out.println("setted model to null");
        treeModel.setNode(treeNode.findRoot());
        System.out.println("setted root ");
        tree.setModel(treeModel);
        System.out.println("setted model");
        tree.setSelectionPath(path);
        System.out.println("setted selection path");
        tree.expandPath(path);
        System.out.println("setted expand path");

        System.out.println("GENERATION JSON");
        if (local) {
            System.out.println("local generation");
            fsStructure.generateJson(directoryTree);
        } else {
            System.out.println("remote generation");
            String gson = treeNode.getGson();
            PropertiesHelper.getInstance().writeConfig(Constants.FOLDERS_CONFIG, gson);
            FSStructure.getInstance().generateTreeStructure();
            System.out.println("ended remote generation");
        }
        System.out.println("ENDED UPDATE MODELS");

    }

    private boolean openFile(FileWrapper fileWrapper) {
        boolean isOpen = true;

        try {
            String id = fileWrapper.getUFID();
            ReadWrapper readWrapper = fileService.read(id, 0);
            byte[] content = readWrapper.getContent();
            String contentS = new String(content);
            //TODO: pass the boolean
            new EditFileUI(this, contentS, fileService, id, fileWrapper.getPath(), readWrapper.isWritable());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            isOpen = false;
        }

        return isOpen;
    }

    private boolean newFile(String fileName) {
        boolean isCreated = true;

        try {
            String ufid = fileService.create(netNodeLocation.getName(), currentNode, fileName);
            directoryService.addName(currentNode, fileName, ufid, node -> {
                isItemCreated = true;
                updateModels(node, true);
            });

        } catch (IOException e) {
            e.printStackTrace();
            isCreated = false;
        }

        return isCreated;
    }

    private void newFolder(String folderName) {
        directoryService.createDirectory(currentNode, folderName, node -> {
            isItemCreated = true;
            updateModels(node, true);
        });
    }

    private void renameFolder(FSTreeNode node, String newName) {

        directoryService.renameDirectory(node, newName, fsNode -> {
            isItemCreated = true;
            updateModels(currentNode.findRoot(), true);
        });
    }

    private void deleteFile(FileWrapper fileWrapper) {
        fileService.delete(fileWrapper.getUFID(), currentNode, node -> {
            isItemCreated = true;
            updateModels(node.findRoot(), true);
        });
    }

    private void deleteFolder(FSTreeNode node) {
        directoryService.deleteDirectory(node, treeNode -> {
            isItemCreated = true;
            updateModels(treeNode.findRoot(), true);
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

        //FileWrapper Menu.
        JMenu menu = new JMenu("File");
        //Open
        JMenuItem menuItem = new JMenuItem("Open");
        menuItem.addActionListener((ActionListener) -> {
            int row = table.getSelectedRow();
            TableItem item = model.getItems().get(row);
            if (item.isFile()) {
                if (!openFile(item.getFileWrapper()))
                    showMessageDialog(null, "An error has occurred during file opening!");
            }
        });
        menu.add(menuItem);
        //New File
        menuItem = new JMenuItem("New File");
        menuItem.addActionListener((ActionListener) -> {
            String fileName = JOptionPane.showInputDialog("New File Name: ");
            if (fileName != null && !fileName.equals("")) {
                if (!newFile(fileName))
                    showMessageDialog(null, "File not created. An error has occurred");
            }

        });
        menu.add(menuItem);
        //New Folder
        menuItem = new JMenuItem("New Folder");
        menuItem.addActionListener((ActionListener) -> {
            String folderName = JOptionPane.showInputDialog("New Folder Name: ");
            if (folderName != null && !folderName.equals(""))
                newFolder(folderName);
        });
        menu.add(menuItem);
        menu.addSeparator();
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
        rename = new JMenuItem("Rename");
        rename.setEnabled(false);
        rename.addActionListener((ActionListener) -> {
            int row = table.getSelectedRow();
            TableItem item = model.getItems().get(row);
            if (!item.isFile()) {
                String newName = JOptionPane.showInputDialog("New Folder Name: ", item.getTreeNode().getNameNode());
                if (newName != null && !newName.equals("")) {
                    renameFolder(item.getTreeNode(), newName);
                    fsStructure.generateJson(directoryTree);
                    /*try {
                        ownNode.callUpdateAllJson(PropertiesHelper.getInstance().loadConfig(Constants.FOLDERS_CONFIG));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }*/

                }
            } else {
                String newName = JOptionPane.showInputDialog("New File Name: ", item.getFileWrapper().getFileName());
                if (newName != null && !newName.equals("")) {
                    item.getFileWrapper().setFileName(newName);
                    updateModels(currentNode, true);
                    fsStructure.generateJson(directoryTree);
                   /* try {
                        ownNode.callUpdateAllJson(PropertiesHelper.getInstance().loadConfig(Constants.FOLDERS_CONFIG));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }*/
                }
            }

            FSTreeNode root = currentNode.findRoot();
            root.setGson(PropertiesHelper.getInstance().loadConfig(Constants.FOLDERS_CONFIG));
            MediatorFsNet.getInstance().jsonReplicaton(root);

        });
        menu.add(rename);
        //Delete
        delete = new JMenuItem("Delete");
        delete.setEnabled(false);
        delete.addActionListener((ActionListener) -> {
            int row = table.getSelectedRow();
            TableItem item = model.getItems().get(row);
            if (!item.isFile()) {
                deleteFolder(item.getTreeNode());
            } else {
                deleteFile(item.getFileWrapper());
            }
        });
        menu.add(delete);
        menuBar.add(menu);

        //Navigate Folder Up
        menuBar.add(Box.createHorizontalGlue());
        navigateUpBtn = new JButton("Navigate Up");
        navigateUpBtn.addActionListener((ActionListener) -> {
            //navigate up
            changeTableView(true, null);
        });
        menuBar.add(navigateUpBtn);

        return menuBar;
    }
}

