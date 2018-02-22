import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Node extends Remote, Serializable {

    void bind(String ip, String nome,int port) throws RemoteException;

    void create(String arg) throws RemoteException;

    Wrap join(String ipMaster, String name, String ipNode) throws RemoteException;

    int getAndSetNum() throws RemoteException;

    Wrap add(String ip,int port) throws RemoteException;

    void fistAdd(String ip,int port,String name) throws RemoteException;

    void firstJoin(String ip,int port,String path) throws RemoteException;

    String saluta() throws RemoteException;

    int getFreePort(String ipNode) throws RemoteException;
}
