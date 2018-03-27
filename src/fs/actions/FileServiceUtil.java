package fs.actions;

import fs.actions.interfaces.FileService;
import fs.actions.object.WrapperFlatServiceUtil;
import mediator_fs_net.MediatorFsNet;
import net.objects.JoinWrap;
import net.objects.NetNodeImpl;
import net.objects.NetNodeLocation;
import net.objects.RegistryWrapper;
import net.objects.interfaces.NetNode;
import utils.Util;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;

public class FileServiceUtil {
    public static WrapperFlatServiceUtil create(String path, String ownIP, String nameService, NetNodeLocation locationRet) {
        System.setProperty("java.rmi.server.hostname", ownIP);
        HashMap<Integer, NetNodeLocation> ret = null;
        MediatorFsNet mediatorFsNet = new MediatorFsNet();
        RegistryWrapper rw = Util.getNextFreePort();
        Registry registry = rw.getRegistry();
        int port = rw.getPort();
        NetNode node = null;
        try {
            //node = new NetNodeImpl(path, ownIP,port, nameService,mediatorFsNet);
            node = new NetNodeImpl(path, ownIP, port, mediatorFsNet);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        FileService service = new FileServiceImpl(path, mediatorFsNet);
        mediatorFsNet.addNetService(node);
        mediatorFsNet.addService(service);
        String connectPath = "rmi://" + ownIP + ":" + port + "/" + nameService;
        System.out.println("connectPath = " + connectPath);
        try {
            registry.bind(connectPath, node);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
        }
        if (locationRet != null) {
            String recPat = locationRet.toUrl();
            try {
                Registry registryRec = LocateRegistry.getRegistry(locationRet.getIp(), locationRet.getPort());
                NetNode node1 = (NetNode) registryRec.lookup(recPat);

                System.out.println("[AGGIORNAMENTO NODI]");

                //Modifiche per il nome Host random
                JoinWrap jWrap = node1.join(ownIP, port, locationRet.getName());
                HashMap<Integer, NetNodeLocation> retMap = jWrap.getCoNodesJoin();
                node.setNameLocation(jWrap.getNameJoin());

                System.out.println();
                System.out.println("[MAPPA RITORNATA]");
                System.out.println();
                Util.plot(retMap);
                node.setConnectedNodes(retMap);
                ret = retMap;

                //Se i nodi sono solo 2 le Map saranno già aggiornate
                if (!(retMap.size() == 2)) {
                    System.out.println();
                    System.out.println("[AGGIORNAMENTO NODI CONNESSI SU TERZI]");
                    System.out.println();
                    for (Map.Entry<Integer, NetNodeLocation> entry : node.getHashMap().entrySet()) {

                        if (!((ownIP + port).hashCode() == entry.getKey() || (locationRet.getIp() + locationRet.getPort()).hashCode() == entry.getKey())) {

                            NetNodeLocation tmp = entry.getValue();
                            String tmpPath = "rmi://" + tmp.getIp() + ":" + tmp.getPort() + "/" + tmp.getName();

                            Registry tmpRegistry = LocateRegistry.getRegistry(tmp.getIp(), tmp.getPort());
                            NetNode tmpNode = (NetNode) tmpRegistry.lookup(tmpPath);
                            tmpNode.setConnectedNodes(node.getHashMap());
                            ret = node.getHashMap();
                        }
                    }
                }


            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (NotBoundException e) {
                e.printStackTrace();
            }
        }
        return new WrapperFlatServiceUtil(new NetNodeLocation(ownIP, port, nameService), ret, service);

    }
}