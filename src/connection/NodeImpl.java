package connection;

import objects.Node;


import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NodeImpl extends UnicastRemoteObject implements Node {

    private int num = 0;
    //<host,ip>
    private HashMap<String, NodeLocation> connectedNodes;

    public NodeImpl() throws RemoteException {
        super();
        connectedNodes = new HashMap<>();
        System.out.println(connectedNodes);
    }

    //in caso di porta occupata viene creato il registro nella porta successiva
    public void create(String args,String ip, String fsName) {
        System.setProperty("java.rmi.server.hostname",ip);
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
        String connectPath = "rmi://" + ip + ":" + port + "/" + fsName;
        System.out.println(connectPath);
        System.out.println(registry);
        try {
            String host = "host" + node.getAndSetNum();
            //connectedNodes.put(host, new connection.NodeLocation(ip, port));
            System.out.println("created distributed fileSystem : " + args + " by host : " + host);
            System.out.println("with port : "+port+" and address "+ip);
            registry.rebind(connectPath, node);

        } catch (RemoteException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        firstJoin(ip,port,connectPath, fsName);
    }

    public void firstJoin(String ip,int port,String path, String fsName){
        Registry registry=null;
        try {
            registry=LocateRegistry.getRegistry(ip,port);
        } catch (RemoteException e) {
            System.out.println("[First Join] : there is not node in this position");
            System.exit(-1);
        }
        try {
            Node node=(Node)registry.lookup(path);
            node.fistAdd(ip,port,fsName);
        } catch (RemoteException e) {
            e.printStackTrace();
            System.exit(-1);
        } catch (NotBoundException e) {
            e.printStackTrace();
            System.exit(-1);
        }



    }

    public void fistAdd(String ip,int port,String name){
        connectedNodes.put(name,new NodeLocation(ip,port));
        System.out.println("Fatto il primo Add");
        Util.plot(connectedNodes);
    }

    public int getAndSetNum() {
        int temp = num;
        num++;
        return temp;
    }

    @Override
    public Wrap join(String ipMaster, String name, String ipNode) throws RemoteException {
        String path = "rmi://" + ipMaster + ":"+1099+"/" + name;
        System.out.println(path);
        Registry registry = LocateRegistry.getRegistry(ipMaster);
        Node master = null;
        try {
            master = (Node) registry.lookup(path);
        } catch (NotBoundException e) {
            System.out.println("there is not node at this address : "+ipMaster+" with this name : "+name);
            System.exit(-1);
        }
        int portNode = this.getFreePort(ipNode);
        Wrap ret = master.add(ipNode, portNode);
        this.bind(ipNode, ret.getOwnNode(), portNode);
        System.out.println("ho fatto il binding al nodo: "+ipNode+" con nome : "+ret.getOwnNode()+" "+portNode);
        System.out.println("stampa dei nodi");
        System.out.println("[JOIN] -> plot connected nodes");
        Util.plot(connectedNodes);
        System.out.println("connesso");
        return ret;
    }

    public Wrap add(String ip, int port) throws RemoteException {
        System.out.println("[ADD]");
        String host = "host" + this.getAndSetNum();
        System.out.println(this.connectedNodes);
        Util.plot(this.connectedNodes);
        this.connectedNodes.put(host, new NodeLocation(ip, port));
        System.out.println("[ADD] : plot connected nodes");
        Util.plot(this.connectedNodes);
        System.out.println("ho aggiunto il nodo : " + ip + " con host : " + host);
        return new Wrap(this.connectedNodes, host);
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
        String connectPath = "rmi://" + ip +":"+port+ "/" + nome;
        try {
            registry.bind(connectPath, this);
        } catch (RemoteException e) {
            e.printStackTrace();
            System.exit(-1);
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        System.out.println("Bind: binding eseguito : "+connectPath);

    }



    public String saluta() {
        return "ciao bello, noi possiamo comunicare";
    }

    public int getFreePort(String ipNode) {
        int port = 1099;
        System.out.println("Stampa dei servizi all'indirizzo : " + ipNode);
        boolean haveEnought = true;
        while (haveEnought) {
            try {
                Registry registry = LocateRegistry.getRegistry(ipNode, port);
                String[] lista = registry.list();
                for (String riga : lista) {
                    System.out.println(riga);
                    String expr = "^(([^:/?#]+):)?(//([^:/?#]*):([\\d]*))?";
                    Matcher matcher = Pattern.compile(expr)
                            .matcher(riga);
                    if (matcher.find()) {
                        port = Integer.parseInt(matcher.group(5));
                        System.out.println(port);
                    }
                }
                if (lista.length == 0){
                    haveEnought=false;
                }
                else {
                    port++;
                }

            } catch (RemoteException e) {
                haveEnought=false;
            }

            System.out.println("la prima porta libera è : " + port);

        }
        return port;
    }
}
