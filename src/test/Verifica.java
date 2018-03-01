package test;

import net.objects.interfaces.NetNode;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

//args[0]-> host
//args[1]-> port
//args[2]-> nome
public class Verifica {
    public static void main(String[] args) throws RemoteException, NotBoundException {
        //System.out.println(path);
        Registry registry= LocateRegistry.getRegistry("localhost",1101);
        String[] lista=registry.list();
        for(String riga:lista){
            System.out.println(riga);
            NetNode netNode =(NetNode)registry.lookup(riga);
            System.out.println(netNode.saluta());
        }
    }
}
