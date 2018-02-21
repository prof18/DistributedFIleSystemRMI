import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

public class NodeImpl extends UnicastRemoteObject implements Node {

    private int num = 0;
    private HashMap<String, String> connectedNodes;

    public NodeImpl() throws RemoteException {
        super();
        connectedNodes = new HashMap<>();
    }

    public void create(String args) {
        System.setProperty("java.rmi.server.hostname", "localhost");

        String ip = "localhost";
        int port = 1099;
        Node node=null;
        try {
            node = new NodeImpl();
        } catch (RemoteException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        Registry registry = null;
        try {
            registry = LocateRegistry.createRegistry(port);
        } catch (RemoteException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        String connectPath = "rmi://" + ip + "/" + "file";
        System.out.println(connectPath);
        System.out.println(registry);
        try {
            String host = "host" + node.getAndSetNum();
            connectedNodes.put(host, ip);
            System.out.println("created distributed fileSystem : " + args + " by host : " + host);
            registry.rebind(connectPath, node);

        } catch (RemoteException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public int getAndSetNum() {
        int temp = num;
        num++;
        return temp;
    }

    @Override
    public void join(String ipMaster,String name,String ipNode) throws RemoteException {
        String path="rmi://"+ipMaster+"/"+name;
        System.out.println(path);
        Registry registry = LocateRegistry.getRegistry(ipMaster);
        Node master=null;
        try {
            master = (Node) registry.lookup(path);
        } catch (NotBoundException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        master.add(ipNode);
        System.out.println("connesso");
    }

    public void add(String ip) throws RemoteException{
        String host = "host" + this.getAndSetNum();
        connectedNodes.put(host, ip);
        System.out.println("ho aggiunto il nodo : "+ip+" con host : "+host);
    }
}
