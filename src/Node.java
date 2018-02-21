
import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Node extends Remote, Serializable {

    void create(String arg) throws RemoteException;
    void join(String ipMaster,String name,String ipNode) throws RemoteException;
    int getAndSetNum() throws RemoteException;
    void add(String ip) throws RemoteException;
}
