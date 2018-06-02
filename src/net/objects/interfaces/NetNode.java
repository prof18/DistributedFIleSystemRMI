package net.objects.interfaces;

import fs.actions.ReplicationWrapper;
import fs.actions.object.CacheFileWrapper;
import fs.actions.object.ListFileWrapper;
import fs.objects.structure.FSTreeNode;
import mediator_fs_net.MediatorFsNet;
import net.objects.JoinWrap;
import net.objects.NetNodeLocation;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

//import net.objects.NetNodeWrap;

/**
 * This interface is dedicated in order to manage all the operation related to communication
 */

public interface NetNode extends Remote, Serializable {

    /**
     * This method is used to connect to a node
     *
     * @param ipNode Ip address to connect
     * @param port   of the service to connect
     * @param name   of the service to connect
     * @return return a wrapper that contains the assigned name and the list of nodes
     * @throws RemoteException if there are problems in the RMI communication
     */

    JoinWrap join(String ipNode, int port, String name) throws RemoteException;

    HashMap<String, ListFileWrapper> getFileNodeList() throws RemoteException;

    void setFileNodeList(HashMap<String, ListFileWrapper> receivedFNL, boolean typeSet) throws RemoteException;

    void modifyFileNodeList(HashMap<String, ListFileWrapper> toModify) throws RemoteException;

    void beginFileNodeList()throws RemoteException;

    void updateAllFileNodeList(HashMap<String, ListFileWrapper> fileNodeList) throws RemoteException;

    /**
     * This method change the updated list associated with the file
     * @param UFID file identifier
     * @param list list of node with the file saved in local
     * @throws RemoteException
     */
    void updateWritePermissionMap(String UFID, ListFileWrapper list) throws RemoteException;

    /**
     * This method update the list of nodes where a specific file is saved
     * @param UFID file identifier
     * @param netNode node to add in the list
     * @throws RemoteException
     */
    void nodeFileAssociation(String UFID, NetNodeLocation netNode) throws RemoteException;

    /**
     * This method delete local file in the node and update the total occupied space.
     * @param UFID file identifier
     * @param treeFileDirectoryUFID directory identifier
     * @param fileSize the file size
     * @return true if the file and it's attributes are deleted, false otherwise
     * @throws RemoteException
     */
    boolean deleteFile(String UFID, String treeFileDirectoryUFID, long fileSize) throws RemoteException;

    NetNodeLocation getOwnLocation() throws RemoteException;

    void updateUI(FSTreeNode treeRoot) throws RemoteException;

    /**
     * This method is used to get the own ip address
     *
     * @return own Ip
     * @throws RemoteException
     */

    String getOwnIp() throws RemoteException;

    /**
     * This method is used to get the own port number
     *
     * @return own port number
     * @throws RemoteException
     */

    int getOwnPort() throws RemoteException;

    /**
     * This method returns the instance of the related mediator
     *
     * @return
     * @throws RemoteException
     */

    MediatorFsNet getMediator() throws RemoteException;


    /**
     * This method is used to get a specific file in the distributed filesystem
     *
     * @param UFID is the file name
     * @return return a Wrapper class that contains the requested file and its attribute
     * @throws RemoteException if there are problems in the RMI communication
     */

    CacheFileWrapper getFileOtherHost(String UFID) throws RemoteException;


    /**
     * This method return the name of the node
     *
     * @return a string with the name of the node
     * @throws RemoteException if there are problems in the RMI communication
     */
    String getHostName() throws RemoteException;

    /**
     * This method is used to access to the list of connected nodes
     *
     * @return the list of connected nodes
     * @throws RemoteException if there are problems in the RMI communication
     */

    HashMap<Integer, NetNodeLocation> getHashMap() throws RemoteException;

    /**
     * This method is used to get a specific file
     *
     * @param UFID is the file name
     * @return return a Wrapper class that contains the requested file and its attribute
     * @throws RemoteException if there are problems in the RMI communication
     */

    CacheFileWrapper getFile(String UFID) throws RemoteException;

    /**
     * When a node connects to a the distributed FS , the own is checked and is changed if it
     * is already used
     *
     * @param name is the selected name
     * @return a string that contains the accepted name or the new name
     * @throws RemoteException if there are problems in the RMI communication
     */

    String checkHostName(String name) throws RemoteException;

    /**
     * This method is used to check if the nodes of the connected list are reachable yet, if there are
     * some unreachable nodes, they are removed from the list
     *
     * @throws RemoteException if there are problems in the RMI communication
     */

    void checkNodesAndReplica() throws RemoteException;

    boolean checkSecReplica(NetNodeLocation e, String fileName) throws RemoteException;

    boolean verifyFile(String fileName) throws RemoteException;

    boolean callSaveFile(NetNodeLocation e, CacheFileWrapper cacheFileWrapper) throws RemoteException;

    boolean saveFile(CacheFileWrapper e) throws RemoteException;

    /**
     * When a node connects to a distributed FS , update the list of connected nodes with the more
     * recent , that is received from the connected node
     *
     * @param connectedNodes is the list of connected nodes
     * @throws RemoteException if there are problems in the RMI communication
     */


    void setConnectedNodes(HashMap<Integer, NetNodeLocation> connectedNodes) throws RemoteException;

    /**
     * When a node connects to a FS, if the selected name is already used the new name is set with
     * method
     *
     * @param name is the new selected name for the node
     * @throws RemoteException if there are problems in the RMI communication
     */

    void setNameLocation(String name) throws RemoteException;

    /**
     * This method is used to replace a specific file in the distributed filesystem after an edit
     *
     * @param fileWrappers is an object that wraps the edited file and its new attribute
     * @throws RemoteException if there are problems in the RMI communication
     */


    void replaceFileFromFS(ArrayList<CacheFileWrapper> fileWrappers) throws RemoteException;


    /**
     * This method is called to modify a file in the own node and if there is not this file call through
     * the mediator replaceFileFromFS
     *
     * @param newFile      is an instance of an object that contain the edit file and its attribute
     * @param lastModified is the time of the lastModified
     * @param UFID         is the unique name of the file
     * @throws RemoteException if there are problems in the RMI communication
     */


    void replaceFile(CacheFileWrapper newFile, long lastModified, String UFID) throws RemoteException;

    NetNodeLocation callSaveFileReplica(CacheFileWrapper cacheFileWrapper, String UFID) throws RemoteException;

    /**
     * This method is used to implement the replication
     *
     * @param rw is a wrapper that contains all the needed about the file
     * @return a boolean that indicates the success of the operation
     * @throws RemoteException
     */

    boolean saveFileReplica(ReplicationWrapper rw) throws RemoteException;


    /**
     * Is used to verify if a nodes is reachable
     *
     * @return a string with a message
     * @throws RemoteException if there are problems in the RMI communication
     */

    String verify() throws RemoteException;

    String getJson() throws RemoteException;

    void setJson(String json, boolean up) throws RemoteException;

    void connectionMergeJson(String gson) throws RemoteException;

    void callUpdateAllJson(String json) throws RemoteException;


}
