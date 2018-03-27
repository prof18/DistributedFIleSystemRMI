package net.actions;

import net.objects.interfaces.NetNode;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;


public class GarbageService extends UnicastRemoteObject implements Runnable {
    private String hostname;
    private String ipNode;
    private int portNode;

    public GarbageService(String ip, String host, int port) throws RemoteException {

        hostname = host;
        ipNode = ip;
        portNode = port;

    }


    @Override
    public void run() {
        System.out.println("THREAD RUN");
        System.out.println(" ");
        int i = 0;
        while (true) {
            String path = null;
            try {
                Thread.sleep(60000);

                Registry registry = null;
                registry = LocateRegistry.getRegistry(ipNode, portNode);
                path = "rmi://" + ipNode + ":" + portNode + "/" + hostname;

//                System.out.println("ipNode "+ipNode);
//                System.out.println("portNode "+portNode);
//                System.out.println("host "+hostname);
//                System.out.println(" ");

//                String[] service = registry.list();
//                for (String tmp : service) {
//                    System.out.println(tmp);
//                }

                NetNode node = (NetNode) registry.lookup(path);
                // Chiamo il metodo check solamente se sono presenti altri nodi
                if (!(node.getHashMap().size() == 1)) {
                    System.out.println("-CHECK NODES-" + i);
                    i++;
                    node.checkNodes();
                }


            } catch (InterruptedException e) {
                System.out.println("InterruptedException Verify Thread");
                e.printStackTrace();

            } catch (RemoteException e) {
                System.out.println("RemoteException Verify Thread");
                e.printStackTrace();

            } catch (NotBoundException e) {
                System.out.println("NotBoundException Verify Thread " + path);
                e.printStackTrace();
            }


        }

    }

}