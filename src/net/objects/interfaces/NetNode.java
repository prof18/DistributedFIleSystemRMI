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

    String getHost() throws RemoteException;

    int getPort() throws RemoteException;

    public MediatorFsNet getMediator() throws RemoteException;

    JoinWrap join(String ipNode, int port, String name) throws RemoteException;

 //   int getAndSetNum() throws RemoteException;

    CacheFileWrapper getFileOtherHost(String UFID) throws RemoteException;

    void replaceFileFromFS(ArrayList<WritingCacheFileWrapper> fileWrappers) throws RemoteException;

    //NetNodeWrap add(String ip, int port) throws RemoteException;

    String replaceFile(CacheFileWrapper newFile, long lastModified, String UFID) throws RemoteException;

    String getHostName() throws RemoteException;

    void setConnectedNodes(HashMap<Integer, NetNodeLocation> connectedNodes) throws RemoteException;

    HashMap<Integer, NetNodeLocation> getHashMap() throws RemoteException;

    CacheFileWrapper getFile(String UFID) throws  RemoteException;

    void checkNodes() throws RemoteException;

    String verify() throws RemoteException;

    String checkHostName(String name) throws RemoteException;

    boolean saveFileReplica(ReplicationWrapper rw) throws RemoteException;

    void setNameLocation(String name) throws RemoteException;

    boolean updateFileList(String fileID, ArrayList<NetNodeLocation> nodeList) throws RemoteException;
}
