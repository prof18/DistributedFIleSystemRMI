package net.objects.interfaces;

import fs.actions.CacheFileWrapper;
import net.objects.NetNodeLocation;
import net.objects.NetNodeWrap;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;

public interface NetNode extends Remote, Serializable {

    String getHost() throws RemoteException;

    int getPort() throws RemoteException;

    HashMap<Integer, NetNodeLocation> join(String ipNode, int port, String name) throws RemoteException;

    int getAndSetNum() throws RemoteException;

    NetNodeWrap add(String ip, int port) throws RemoteException;

    String getHostName() throws RemoteException;

    void setConnectedNodes(HashMap<Integer, NetNodeLocation> connectedNodes) throws RemoteException;

    HashMap<Integer, NetNodeLocation> getHashMap() throws RemoteException;

    CacheFileWrapper getFile(String UFID) throws  RemoteException;

    void checkNodes() throws RemoteException;

    String verify() throws RemoteException;
}
