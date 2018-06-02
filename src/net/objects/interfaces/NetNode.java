package net.objects.interfaces;

import fs.actions.ReplicationWrapper;
import fs.actions.object.CacheFileWrapper;
import fs.actions.object.ListFileWrapper;
import fs.objects.structure.FSTreeNode;
import net.objects.JoinWrap;
import net.objects.NetNodeLocation;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;

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

    /**
     * @return the fileNodeList of the node
     * @throws RemoteException
     */

    HashMap<String, ListFileWrapper> getFileNodeList() throws RemoteException;

    /**
     * Is used to set or merge the fileNodeList of new connected node
     *
     * @param receivedFNL received fileNodeList
     * @param merge       is used to decide if a merge is necessary or not
     * @throws RemoteException
     */

    void setFileNodeList(HashMap<String, ListFileWrapper> receivedFNL, boolean merge) throws RemoteException;

    /**
     * Is used to update a fileNodeList with an entry is modified by another nodes
     *
     * @param entriesFileNodelist entries that must be modified
     * @throws RemoteException
     */

    void modifyFileNodeList(HashMap<String, ListFileWrapper> entriesFileNodelist) throws RemoteException;

    /**
     * Is used to create the fileNodeList of the node if it contains already a json
     * and also to update the json with only the existing files
     *
     * @throws RemoteException
     */

    void beginFileNodeList() throws RemoteException;

    /**
     * Is used to update the fileNodeList through the network
     *
     * @param fileNodeList
     * @throws RemoteException
     */

    void updateAllFileNodeList(HashMap<String, ListFileWrapper> fileNodeList) throws RemoteException;

    void updateWritePermissionMap(String UFID, ListFileWrapper list) throws RemoteException;

    void nodeFileAssociation(String UFID, NetNodeLocation netNode) throws RemoteException;

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

    /**
     * Is used to call verifyFile on a node
     *
     * @param location where the replica is
     * @param UFID is the filename
     * @return true if verified
     * @throws RemoteException
     */

    boolean checkSecReplica(NetNodeLocation location, String UFID) throws RemoteException;

    /**
     * Is used to verify if a replica exist
     *
     * @param UFID is the filename
     * @return true if it exists
     * @throws RemoteException
     */

    boolean verifyFile(String UFID) throws RemoteException;


    /**
     * Is used to call saveFile in another node
     *
     * @param location where the file must be save
     * @param cacheFileWrapper is a wrap of a file and its attributes
     * @throws RemoteException
     */

    void callSaveFile(NetNodeLocation location, CacheFileWrapper cacheFileWrapper) throws RemoteException;


    /**
     * Is used to save a file replica that has been removed manually
     *
     * @param e is a wrap of a file and its attributes
     * @return false if the routine fails
     * @throws RemoteException
     */

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
     * Is used to call saveFileReplica in another node
     *
     * @param cacheFileWrapper is a wrap of a file and its attributes
     * @param UFID is the filename
     * @return the location where the replica is saved
     * @throws RemoteException
     */

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

    /**
     * Is used in order to merge json when a new node joins the network and it contains already a json and some file
     *
     * @param json json from the network
     * @throws RemoteException
     */

    void connectionMergeJson(String json) throws RemoteException;

    /**
     * Is used to send the update json through the network
     *
     * @param json to send
     * @throws RemoteException
     */

    void callUpdateAllJson(String json) throws RemoteException;


}
