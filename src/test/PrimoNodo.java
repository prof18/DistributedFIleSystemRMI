package test;

import net.objects.NetNodeImpl;
import net.objects.interfaces.NetNode;

import java.rmi.RemoteException;

public class PrimoNodo {
    public static void main(String[] args) {
        NetNode netNode = null;
        try {
            netNode = new NetNodeImpl();
        } catch (RemoteException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        try {
            //modificare questo ip
            netNode.create("file", "10.8.0.4", "LR18");
        } catch (RemoteException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
