package net.actions;

import net.objects.NetNodeImpl;
import net.objects.interfaces.NetNode;

import java.rmi.RemoteException;

public class Create {

    public static void create(String ip, String name) {
        NetNode netNode = null;
        try {
            netNode = new NetNodeImpl();
        } catch (RemoteException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        try {
            //modificare questo ip
            netNode.create(name,ip, name);
        } catch (RemoteException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

}
