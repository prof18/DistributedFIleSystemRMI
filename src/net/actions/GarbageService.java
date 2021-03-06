package net.actions;

import net.objects.interfaces.NetNode;
import utils.Constants;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * This class implements a Thread with the aim of calling the checkNodes method from NetNodeImpl every
 * t second.
 */
public class GarbageService extends UnicastRemoteObject implements Runnable {

    private String hostname;
    private String ipNode;
    private int portNode;
    private long timeInterval;

    /**
     * Is the constructor of the class
     *
     * @param ip   is the ip address of the node
     * @param host is the name of the service
     * @param port is the port where is located the service
     * @throws RemoteException because is an extension of UnicastRemoteObject
     */
    public GarbageService(String ip, String host, int port) throws RemoteException {

        hostname = host;
        ipNode = ip;
        portNode = port;
        timeInterval = Constants.INTERVAL_GARBAGE_COLLECTOR;

    }

    @SuppressWarnings("InfiniteLoopStatement")
    @Override
    public void run() {
        while (true) {
            String path = null;
            try {
                Thread.sleep(timeInterval);

                Registry registry;
                registry = LocateRegistry.getRegistry(ipNode, portNode);
                path = "rmi://" + ipNode + ":" + portNode + "/" + hostname;
                NetNode node = (NetNode) registry.lookup(path);

                //the check methods is called if only there are other nodes in the list
                if (!(node.getHashMap().size() == 1)) {
                    node.checkNodesAndReplica();
                }

            } catch (InterruptedException e) {
                System.out.println("[GARBAGE-SERVICE] InterruptedException Verify Thread");
                e.printStackTrace();

            } catch (RemoteException e) {
                System.out.println("[GARBAGE-SERVICE] RemoteException Verify Thread");
                e.printStackTrace();

            } catch (NotBoundException e) {
                System.out.println("[GARBAGE-SERVICE] NotBoundException Verify Thread " + path);
                e.printStackTrace();
            }


        }

    }

}