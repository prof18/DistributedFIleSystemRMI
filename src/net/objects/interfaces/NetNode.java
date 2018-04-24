package net.objects.interfaces;

import fs.actions.ReplicationWrapper;
import fs.actions.object.CacheFileWrapper;
import fs.actions.object.WritingCacheFileWrapper;
import mediator_fs_net.MediatorFsNet;
import net.objects.JoinWrap;
import net.objects.NetNodeLocation;
//import net.objects.NetNodeWrap;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

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
     * This method is used to
     * @return
     * @throws RemoteException
     */

    String getOwnIp() throws RemoteException;

    /**
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

    void checkNodes() throws RemoteException;

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


    void replaceFileFromFS(ArrayList<WritingCacheFileWrapper> fileWrappers) throws RemoteException;


    /**
     * This method is called to modify a file in the own node and if there is not this file call through
     * the mediator replaceFileFromFS
     *
     * @param newFile      is an instance of an object that contain the edit file and its attribute
     * @param lastModified is the time of the lastModified
     * @param UFID         is the unique name of the file
     * @return a string that represent the successful or not of the replace file
     * @throws RemoteException if there are problems in the RMI communication
     */


    String replaceFile(CacheFileWrapper newFile, long lastModified, String UFID) throws RemoteException;

    /**
     *
     * @param rw
     * @return
     * @throws RemoteException
     */

    boolean saveFileReplica(ReplicationWrapper rw) throws RemoteException;


    /**
     *
     * @param fileID
     * @param nodeList
     * @return
     * @throws RemoteException
     */
    boolean updateFileList(String fileID, ArrayList<NetNodeLocation> nodeList) throws RemoteException;

    /**
     * Is used to verify if a nodes is reachable
     *
     * @return a string with a message
     * @throws RemoteException if there are problems in the RMI communication
     */

    String verify() throws RemoteException;

}
