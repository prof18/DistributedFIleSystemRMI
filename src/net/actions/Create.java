package net.actions;

import net.objects.Node;
import net.objects.NodeImpl;

import java.rmi.RemoteException;

public class Create {

    public static void create(String ip, String name) {
        Node node = null;
        try {
            node = new NodeImpl();
        } catch (RemoteException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        try {
            //modificare questo ip
            node.create(name,ip, name);
        } catch (RemoteException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

}
