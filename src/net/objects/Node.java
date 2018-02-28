package net.objects;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;

public interface Node extends Remote, Serializable {

    void bind(String ip, String nome,int port) throws RemoteException;

    void create(String arg,String ip, String fsName) throws RemoteException;

    Wrap join(String ipMaster, String name, String ipNode) throws RemoteException;

    int getAndSetNum() throws RemoteException;

    Wrap add(String ip, int port) throws RemoteException;

    void fistAdd(String ip,int port,String name) throws RemoteException;

    void firstJoin(String ip,int port,String path, String fsName) throws RemoteException;

    String saluta() throws RemoteException;

    int getFreePort(String ipNode) throws RemoteException;

    String getHostName()throws RemoteException;

    HashMap<String, NodeLocation> getHashMap()throws RemoteException;

    void updateCoNodes(HashMap<String, NodeLocation> coNodes)throws RemoteException;
}
