package net.actions;

import net.objects.Node;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;


public class verifyThread extends UnicastRemoteObject implements Runnable
{
    private String hostname;
    private String ipNode;
    private int portNode;

    public verifyThread(String ip, String host, int port) throws RemoteException{

        hostname = host;
        ipNode = ip;
        portNode = port;

    }


    @Override
    public void run() {

        while (true) {
            String path = null;
            try {
                Thread.sleep(10000);

                Registry registry = null;
                registry = LocateRegistry.getRegistry(ipNode);
                path = "rmi://" + ipNode + ":" + portNode + "/" + hostname;
                //System.out.println(path);
                Node node = (Node) registry.lookup(path);

                node.checkNodes();

            } catch (InterruptedException e) {
                System.out.println("InterruptedException Verify Thread");
                e.printStackTrace();

            } catch (RemoteException e) {
                System.out.println("RemoteException Verify Thread");
                e.printStackTrace();

            } catch(NotBoundException e){
                System.out.println("NotBoundException Verify Thread "+ path);
                e.printStackTrace();
            }


        }

    }

}
