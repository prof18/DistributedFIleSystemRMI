package net.actions;



import net.objects.NetNodeImpl;
import net.objects.interfaces.NetNode;

import java.rmi.RemoteException;

public class Create {

    public static void create(String ip, String name) {
        NetNode node = null;
        try {
            node = new NetNodeImpl();
        } catch (RemoteException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        try {
            //modificare questo ip
            node.create(name, ip, name);
        } catch (RemoteException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

}
