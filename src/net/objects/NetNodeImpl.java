package net.objects;

import net.objects.interfaces.NetNode;
import utils.Util;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NetNodeImpl extends UnicastRemoteObject implements NetNode {

    private int num = 0;

    private String hostName = " --num host not update --";

    //<host,ip>
    private HashMap<String, NetNodeLocation> connectedNodes;

    public NetNodeImpl() throws RemoteException {
        super();
        connectedNodes = new HashMap<>();
        System.out.println(connectedNodes);
    }

    //in caso di porta occupata viene creato il registro nella porta successiva
    public void create(String args, String ip, String fsName) {
        System.setProperty("java.rmi.server.hostname", ip);
        int port = 1099;
        NetNode netNode = null;
        try {
            netNode = new NetNodeImpl();
        } catch (RemoteException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        Registry registry = null;
        boolean portNotFound = true;
        while (portNotFound) {
            try {
                registry = LocateRegistry.createRegistry(port);
                portNotFound = false;
            } catch (RemoteException e) {
                System.out.println(port + " porta occupata");
                port++;
            }
        }
        String connectPath = "rmi://" + ip + ":" + port + "/" + fsName;
        System.out.println(connectPath);
        System.out.println(registry);
        try {
            String host = "host" + netNode.getAndSetNum();
            //connectedNodes.put(host, new net.objects.NetNodeLocation(ip, port));
            System.out.println("created distributed fs : " + args + " by host : " + host);
            System.out.println("with port : " + port + " and address " + ip);
            registry.rebind(connectPath, netNode);

        } catch (RemoteException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        firstJoin(ip, port, connectPath, fsName);
    }

    public void firstJoin(String ip, int port, String path, String fsName) {
        Registry registry = null;
        try {
            registry = LocateRegistry.getRegistry(ip, port);
        } catch (RemoteException e) {
            System.out.println("[First Join] : there is not node in this position");
            System.exit(-1);
        }
        try {
            NetNode netNode = (NetNode) registry.lookup(path);
            netNode.fistAdd(ip, port, fsName);
        } catch (RemoteException e) {
            e.printStackTrace();
            System.exit(-1);
        } catch (NotBoundException e) {
            e.printStackTrace();
            System.exit(-1);
        }


    }

    public void fistAdd(String ip, int port, String name) {
        connectedNodes.put(name, new NetNodeLocation(ip, port));
        System.out.println("Fatto il primo Add");
        Util.plot(connectedNodes);
    }

    public int getAndSetNum() {
        int temp = num;
        num++;
        return temp;
    }


    @Override
    public NetNodeWrap join(String ipMaster, String name, String ipNode) throws RemoteException {

        // TODO: modificare la porta

        String path = "rmi://" + ipMaster + ":" + 1099 + "/" + name;
        System.out.println(path);

        // TODO: getregistry aggiungere porta
        Registry registry = LocateRegistry.getRegistry(ipMaster);
        NetNode master = null;
        try {
            master = (NetNode) registry.lookup(path);
        } catch (NotBoundException e) {
            System.out.println("there is not node at this address : " + ipMaster + " with this name : " + name);
            System.exit(-1);
        }
        int portNode = this.getFreePort(ipNode);
        NetNodeWrap ret = master.add(ipNode, portNode);

        // aggiorna hostName?

        this.hostName = ret.getOwnNode();

        this.bind(ipNode, ret.getOwnNode(), portNode);
        System.out.println("ho fatto il binding al nodo: " + ipNode + " con nome : " + ret.getOwnNode() + " " + portNode);
        System.out.println("stampa dei nodi");
        System.out.println("[JOIN] -> plot connected nodes");
        Util.plot(connectedNodes);
        System.out.println("connesso");
        return ret;
    }

    public NetNodeWrap add(String ip, int port) throws RemoteException {
        System.out.println("[ADD]");
        String host = "host" + this.getAndSetNum();
        System.out.println(this.connectedNodes);
        Util.plot(this.connectedNodes);
        this.connectedNodes.put(host, new NetNodeLocation(ip, port));
        System.out.println("[ADD] : plot connected nodes");
        Util.plot(this.connectedNodes);
        System.out.println("ho aggiunto il nodo : " + ip + " con host : " + host);
        return new NetNodeWrap(this.connectedNodes, host);
    }

    public void bind(String ip, String nome, int port) {
        System.setProperty("java.rmi.server.hostname", ip);
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
        String connectPath = "rmi://" + ip + ":" + port + "/" + nome;
        try {
            registry.bind(connectPath, this);
        } catch (RemoteException e) {
            e.printStackTrace();
            System.exit(-1);
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        System.out.println("Bind: binding eseguito : " + connectPath);

    }


    public String saluta() {
        System.out.println("ho salutato");
        return "ciao bello, noi possiamo comunicare";
    }

    public HashMap<String, NetNodeLocation> getHashMap() {
        return connectedNodes;
    }

    public String getHostName() {
        return hostName;
    }


    public void updateCoNodes(HashMap<String, NetNodeLocation> coNodes) {

        for (Map.Entry<String, NetNodeLocation> entry : coNodes.entrySet()) {

            for (Map.Entry<String, NetNodeLocation> entry2 : connectedNodes.entrySet()) {

                if (!entry.getKey().equals(entry2.getKey())) {

                    System.out.println("Aggiungo il nodo : " + entry.getKey().toString() + "ai connectedNodes di : " + hostName);

                    connectedNodes.put(entry.getKey(), entry.getValue());

                    System.out.println("Comunicazione con : " + entry.getValue().toString());
                    Registry registry = null;
                    try {
                        registry = LocateRegistry.getRegistry(entry.getValue().getIp(), entry.getValue().getPort());
                        String path = entry.getValue().toUrl() + entry.getKey();
                        System.out.println(path);
                        NetNode netNodeTemp = (NetNode) registry.lookup(path);
                        System.out.println(hostName + " saluta : " + netNodeTemp.getHostName() + "--" + netNodeTemp.saluta());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        System.out.println("problema strano2");

                    } catch (NotBoundException e) {
                        e.printStackTrace();
                        System.out.println("non trovato errore2");

                    }

                }


            }

        }


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
                if (lista.length == 0) {
                    haveEnought = false;
                } else {
                    port++;
                }

            } catch (RemoteException e) {
                haveEnought = false;
            }

            System.out.println("la prima porta libera è : " + port);

        }
        return port;
    }
}
