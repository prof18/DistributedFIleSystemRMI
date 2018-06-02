package fs.actions;

import fs.actions.interfaces.FileService;
import fs.actions.object.WrapperFileServiceUtil;
import mediator_fs_net.MediatorFsNet;
import net.objects.JoinWrap;
import net.objects.NetNodeImpl;
import net.objects.NetNodeLocation;
import net.objects.RegistryWrapper;
import net.objects.interfaces.NetNode;
import ui.frame.MainUI;
import utils.Util;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.UnknownHostException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is used by the mainUI:
 * to create a new node;
 * to connect it to the Distributed FileSystem;
 * to notify to all the connected nodes that a new node has entered in the network.
 */

public class FileServiceUtil {
    private static String hostName;

    /**
     * @param path        path to connect
     * @param ownIP       ip of the new node
     * @param locationRet gives the parameters to connect to an another node
     * @param mainUI      instance of the MainUI
     * @return a WrapperFileServiceUtil that contains:
     * the location of this node
     * the hash map of all the nodes in the system and their location
     * the created node
     * the hashMap that has for keys the UFID of the files and values the location of nodes that contain a file
     * @throws NotBoundException
     * @throws UnknownHostException
     */

    public static WrapperFileServiceUtil create(String path, String ownIP, NetNodeLocation locationRet, MainUI mainUI) throws NotBoundException, NullPointerException {
        System.setProperty("java.rmi.server.hostname", ownIP);
        HashMap<Integer, NetNodeLocation> ret = new HashMap<>();
        MediatorFsNet mediatorFsNet = MediatorFsNet.getInstance();
        RegistryWrapper rw = Util.getNextFreePort();
        Registry registry = rw.getRegistry();
        int port = rw.getPort();
        NetNode node = null;
        try {
            node = new NetNodeImpl(path, ownIP, port, mediatorFsNet, mainUI);
            hostName = node.getHostName();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        FileService service = new FileServiceImpl(path);
        mediatorFsNet.addNetService(node);
        mediatorFsNet.addService(service);
        try {
            String connectPath = "rmi://" + ownIP + ":" + port + "/" + hostName;
            registry.bind(connectPath, node);
        } catch (RemoteException e) {
            System.out.println("Problem with the creation of the node");
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            System.out.println("The chosen port is already occupied");
            e.printStackTrace();
        }

        boolean merge = false;
        try {
            if (node.getJson() != null) {
                node.beginFileNodeList();
                merge = true;
            }
        } catch (RemoteException e) {
            System.out.println("Communication problem with beginFileNodeList method");
            e.printStackTrace();
        }

        if (locationRet != null) {
            String recPat = locationRet.toUrl();
            try {
                Registry registryRec = LocateRegistry.getRegistry(locationRet.getIp(), locationRet.getPort());
                NetNode node1 = (NetNode) registryRec.lookup(recPat);

                System.out.println("[Updating Nodes]");

                JoinWrap jWrap = node1.join(ownIP, port, hostName);
                HashMap<Integer, NetNodeLocation> retMap = jWrap.getCoNodesJoin();
                node.setNameLocation(jWrap.getNameJoin());
                node.setFileNodeList(jWrap.getFileNodeList(), merge);

                System.out.println();
                Util.plot(retMap);
                node.setConnectedNodes(retMap);
                mainUI.updateConnectedNode(retMap);


                if ((retMap.size() != 2)) {
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

                String node1Gson = node1.getJson();
                node.connectionMergeJson(node1Gson);
                mainUI.updateConnectedNode(node.getHashMap());


            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        WrapperFileServiceUtil wfsu = null;
        try {
            wfsu = new WrapperFileServiceUtil(new NetNodeLocation(ownIP, port, hostName), node.getHashMap(), service, node);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return wfsu;
    }
}