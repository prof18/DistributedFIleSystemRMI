package net.actions;

import net.objects.NetNodeImpl;
import net.objects.NetNodeLocation;
import net.objects.interfaces.NetNode;
import utils.Util;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Scanner;

public class ExecuteNode {
    private static String ownIP="localhost";
    private static String nameService="host";
    public static void main(String[] args){
        System.setProperty("java.rmi.server.hostname", ownIP);
        NetNode node=null;
        Registry registry = null;
        int port = 1099;
        boolean notFound = true;
        while (notFound) {
            try {
                registry = LocateRegistry.createRegistry(port);
                notFound = false;
            } catch (RemoteException e) {
                System.out.println("porta occupata");
                port++;
            }
        }
        try {
            node = new NetNodeImpl(ownIP,port,nameService);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        String connectPath = "rmi://" + ownIP + ":" + port + "/" + nameService;
        System.out.println(connectPath);
        System.out.println(registry);
        try {
            registry.bind(connectPath, node);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
        }
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("indirizzo a cui connettersi ");
            String ipRec = scanner.next();
            System.out.println("porta a cui è salvato ");
            int porta = scanner.nextInt();
            System.out.println("nome del servizio ");
            String nome = scanner.next();
            String recPat = "rmi://" + ipRec + ":" + porta + "/" + nome;
            try {
                Registry registryRec = LocateRegistry.getRegistry(ipRec, porta);
                NetNode node1 = (NetNode) registryRec.lookup(recPat);
                System.out.println("[AGGIORNAMENTO NODI]");
                HashMap<Integer,NetNodeLocation> retMap= node1.join(ownIP,port,nome);
                System.out.println();
                System.out.println("[MAPPA RITORNATA]");
                System.out.println();
                Util.plot(retMap);
                node.setConnectedNodes(retMap);
                System.out.println("[MAPPA AGGIORNATA]");
                Util.plot(node.getHashMap());
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (NotBoundException e) {
                e.printStackTrace();
            }

        }
    }
}
