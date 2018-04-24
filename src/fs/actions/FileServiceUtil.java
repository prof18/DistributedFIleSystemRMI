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

    public class FileServiceUtil {
        private static String hostName;

        public static WrapperFileServiceUtil create(String path, String ownIP, NetNodeLocation locationRet, MainUI mainUI) throws NotBoundException, UnknownHostException {
            System.setProperty("java.rmi.server.hostname", ownIP);
            System.out.println("locationRet = " + locationRet);
            HashMap<Integer, NetNodeLocation> ret = null;
            MediatorFsNet mediatorFsNet = new MediatorFsNet();
            RegistryWrapper rw = Util.getNextFreePort();
            Registry registry = rw.getRegistry();
            int port = rw.getPort();
            NetNode node = null;
            try {
                //node = new NetNodeImpl(path, ownIP,port, nameService,mediatorFsNet);
                node = new NetNodeImpl(path, ownIP, port, mediatorFsNet, mainUI);
                hostName = node.getHostName();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            FileService service = new FileServiceImpl(path, mediatorFsNet);
            mediatorFsNet.addNetService(node);
            mediatorFsNet.addService(service);
            try {
                String connectPath = "rmi://" + ownIP + ":" + port + "/" + hostName;
                System.out.println("connectPath = " + connectPath);

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
                    System.out.println("recPat = " + recPat);
                    NetNode node1 = (NetNode) registryRec.lookup(recPat);

                    System.out.println("[AGGIORNAMENTO NODI]");

                    //Modifiche per il nome Host random
                    JoinWrap jWrap = node1.join(ownIP, port, hostName);
                    HashMap<Integer, NetNodeLocation> retMap = jWrap.getCoNodesJoin();
                    node.setNameLocation(jWrap.getNameJoin());

                    System.out.println();
                    System.out.println("[MAPPA RITORNATA]");
                    System.out.println();
                    Util.plot(retMap);
                    node.setConnectedNodes(retMap);
                    mainUI.updateConnectedNode(retMap);
                    ret = retMap;

                    //Se i nodi sono solo 2 le Map saranno già aggiornate
                    if ((retMap.size() != 2)) {
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

                    mainUI.updateConnectedNode(node.getHashMap());


                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            WrapperFileServiceUtil wfsu = new WrapperFileServiceUtil(new NetNodeLocation(ownIP, port, hostName), ret, service, node);
            mediatorFsNet.setWrapperFileServiceUtil(wfsu);

            return wfsu;


        }
    }