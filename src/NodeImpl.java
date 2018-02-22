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
    private HashMap<String, NodeLocation> connectedNodes;

    public NodeImpl() throws RemoteException {
        super();
        connectedNodes = new HashMap<>();
    }

    //in caso di porta occupata viene creato il registro nella porta successiva
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
        boolean portNotFound=true;
        while(portNotFound){
            try {
                registry = LocateRegistry.createRegistry(port);
                portNotFound=false;
            } catch (RemoteException e) {
                System.out.println(port+" porta occupata");
                port++;
            }
        }
        String connectPath = "rmi://" + ip + ":" + port + "/" + "file";
        System.out.println(connectPath);
        System.out.println(registry);
        try {
            String host = "host" + node.getAndSetNum();
            connectedNodes.put(host, new NodeLocation(ip, port));
            System.out.println("created distributed fileSystem : " + args + " by host : " + host);
            System.out.println("with port : "+port+" and address "+ip);
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
        int portNode = this.getFreePort(ipNode);
        Wrap ret = master.add(ipNode, portNode);
        this.bind(ipNode, ret.getOwnNode(), portNode);
        System.out.println("stampa dei nodi");
        for (String riga : ret.getNodes()) {
            String[] value = riga.split("     ");
            System.out.println(value[0] + " " + value[1] + " " + value[2]);
            connectedNodes.put(value[0], new NodeLocation(value[1], Integer.parseInt(value[2])));
        }
        System.out.println("connesso");
        return ret;
    }

    public Wrap add(String ip, int port) throws RemoteException {
        String host = "host" + this.getAndSetNum();
        connectedNodes.put(host, new NodeLocation(ip, port));
        String[] ret = new String[connectedNodes.size()];
        int i = 0;
        for (Map.Entry<String, NodeLocation> entry : connectedNodes.entrySet()) {
            ret[i] = entry.getKey() + "     " + entry.getValue().getIp() + "     " + entry.getValue().getPort();
            System.out.println(ret[i]);
            i++;
        }
        System.out.println("ho aggiunto il nodo : " + ip + " con host : " + host);
        return new Wrap(ret, host);
    }

    public void bind(String ip, String nome, int port) {
        Registry registry = null;
        boolean notfound = true;
        while (notfound) {
            try {
                registry = LocateRegistry.createRegistry(port);
                notfound = false;
            } catch (RemoteException e) {
                System.out.println("porta occupata");
                System.out.println("molto strano");
                System.exit(-1);
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
        System.out.println("numero di amici" + connectedNodes.size());
        for (Map.Entry<String, NodeLocation> entry : connectedNodes.entrySet()) {
            if (!entry.getKey().equals(myName)) {
                String path = "rmi://" + entry.getValue() + "/" + entry.getKey();
                System.out.println("amico : " + path);
                System.out.println(path);
                Registry registry = null;
                try {
                    registry = LocateRegistry.getRegistry(entry.getValue().getIp());
                } catch (RemoteException e) {
                    e.printStackTrace();
                    System.exit(-1);
                }
                try {
                    String[] list = registry.list();
                    System.out.println("lista dei servizi");
                    System.out.println("numero di servizi : " + list.length);
                    for (String tmp : list) {
                        System.out.println(tmp);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                    System.exit(-1);
                }
                Node master = null;
                try {
                    master = (Node) registry.lookup(path);
                } catch (NotBoundException e) {
                    System.out.println("non ho trovato nessun oggetto : " + entry.getKey());
                    System.out.println(registry.toString());
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

    public String saluta() {
        return "ciao bello, noi possiamo comunicare";
    }

    public int getFreePort(String ipNode) {
        System.out.println("Stampa dei servizi all'indirizzo : " + ipNode);
        try {
            Registry registry = LocateRegistry.getRegistry(ipNode);
            String[] lista = registry.list();
            for (String riga : lista) {
                System.out.println(riga);
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
