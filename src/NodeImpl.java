import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

public class NodeImpl extends UnicastRemoteObject implements Node {

    private int num = 0;
    //<host,ip>
    private HashMap<String, String> connectedNodes;

    public NodeImpl() throws RemoteException {
        super();
        connectedNodes = new HashMap<>();
    }

    public void create(String args) {
        System.setProperty("java.rmi.server.hostname", "localhost");

        String ip = "localhost";
        int port = 1099;
        Node node = null;
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
    public Wrap join(String ipMaster, String name, String ipNode) throws RemoteException {
        String path = "rmi://" + ipMaster + "/" + name;
        System.out.println(path);
        Registry registry = LocateRegistry.getRegistry(ipMaster);
        Node master = null;
        try {
            master = (Node) registry.lookup(path);
        } catch (NotBoundException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        Wrap ret = master.add(ipNode);
        System.out.println("stampa dei nodi");
        for (String riga:ret.getNodes()){
            String[] value=riga.split("     ");
            System.out.println(value[0]+" "+value[1]);
            connectedNodes.put(value[0],value[1]);
        }
        System.out.println("connesso");
        return ret;
    }

    public Wrap add(String ip) throws RemoteException {
        String host = "host" + this.getAndSetNum();
        connectedNodes.put(host, ip);
        String[] ret = new String[connectedNodes.size()];
        int i = 0;
        for (Map.Entry<String, String> entry : connectedNodes.entrySet()) {
            ret[i] = entry.getKey() + "     " + entry.getValue();
            System.out.println(ret[i]);
            i++;
        }
        System.out.println("ho aggiunto il nodo : " + ip + " con host : " + host);
        return new Wrap(ret, host);
    }

    public void bind(String ip, String nome) {
        int port = 1099;
        Registry registry = null;
        boolean notfound = true;
        while (notfound) {
            try {
                registry = LocateRegistry.createRegistry(port);
                notfound = false;
            } catch (RemoteException e) {
                System.out.println("porta occupata");
                port++;
            }
        }
        String connectPath = "rmi://" + ip + "/" + nome;
        try {
            registry.bind(connectPath, this);
        } catch (RemoteException e) {
            e.printStackTrace();
            System.exit(-1);
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
            System.exit(-1);
        }

    }

    public void sayHello(String myName) {
        System.out.println("numero di amici" +connectedNodes.size());
        for (Map.Entry<String, String> entry : connectedNodes.entrySet()) {
            if (!entry.getKey().equals(myName)) {
                String path = "rmi://" + entry.getValue() + "/" + entry.getKey();
                System.out.println("amico : " + path);
                System.out.println(path);
                Registry registry = null;
                try {
                    registry = LocateRegistry.getRegistry(entry.getValue());
                } catch (RemoteException e) {
                    e.printStackTrace();
                    System.exit(-1);
                }
                try {
                    String[] list=registry.list();
                    System.out.println("lista dei servizi");
                    System.out.println("numero di servizi : "+list.length);
                    for (String tmp:list){
                        System.out.println(tmp);
                    }
                }
                catch (RemoteException e){e.printStackTrace();
                System.exit(-1);}
                Node master = null;
                try {
                    master = (Node) registry.lookup(path);
                } catch (NotBoundException e) {
                    e.printStackTrace();
                    System.exit(-1);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    System.exit(-1);
                }
                try {
                    System.out.println(master.saluta());
                } catch (RemoteException e) {
                    e.printStackTrace();
                    System.exit(-1);
                }

            }
        }

    }

    public String saluta(){
        return "ciao bello, noi possiamo comunicare";
    }
}
