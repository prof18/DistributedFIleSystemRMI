package fs.actions;

import fs.actions.CacheFileWrapper;
import net.objects.NetNodeLocation;
import net.objects.interfaces.NetNode;
import utils.Util;

import javax.swing.text.html.parser.Entity;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

public class WritingNodeCache {
    private ArrayList<WritingCacheFileWrapper> fileList;
    private ArrayList<NetNodeLocation> nodeLocations;
    private final int replacedTimer = 20000; //ms
    private NetNodeLocation netNodeLocation;

    public WritingNodeCache(NetNodeLocation netNodeLocation) {
        fileList = new ArrayList<>();
        nodeLocations = new ArrayList<>();
        Timer timer = new Timer();
        timer.schedule(new ReplacerFile(), 0, replacedTimer);
        this.netNodeLocation = netNodeLocation;
    }

    //devo aggiornare la lista ogni tot
    public void setNodeLocations(HashMap<Integer, NetNodeLocation> newNodeLocations) {
        synchronized (this.nodeLocations) {
            System.out.println("iniziato aggiornamento dei nodi");
            nodeLocations.clear();
            System.out.println("size : " + nodeLocations.size());
            if (nodeLocations.size() != 0) {
                for (Map.Entry<Integer, NetNodeLocation> entry : newNodeLocations.entrySet()) {
                    nodeLocations.add(entry.getValue());
                }
            }
            System.out.println("concluso aggiornamento dei nodi");
        }
    }

    public void add(WritingCacheFileWrapper writingCacheFileWrapper) {
        synchronized (fileList) {
            fileList.add(writingCacheFileWrapper);
        }
    }

    private class ReplacerFile extends TimerTask {
        public void run() {
            synchronized (fileList) {
                System.out.println("[PULIZIA CACHE] : Iniziata la pulizia della cache");
                System.out.println("dimensione della cache : "+fileList.size());
                HashMap<Integer, NetNodeLocation> locashions = null;
                try {
                    Registry registry = LocateRegistry.getRegistry(netNodeLocation.getIp(), netNodeLocation.getPort());
                    NetNode node = (NetNode) registry.lookup(netNodeLocation.toUrl());
                    locashions = node.getHashMap();
                    node.getHashMap();
                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (NotBoundException e) {
                    e.printStackTrace();
                }
                Iterator<WritingCacheFileWrapper> iterator = fileList.iterator();



                while (iterator.hasNext()) {
                    WritingCacheFileWrapper cacheFileWrapper = iterator.next();
                    //devo trovare tutte le posizioni in cui è salvato il file e modificarlo
                    for (Map.Entry<Integer, NetNodeLocation> entry : locashions.entrySet()) {
                        System.out.println(entry.getValue().toString());
                        if (!entry.getValue().equals(netNodeLocation)) {
                            try {
                                System.out.println("entrato per vedere se contiene il file in");
                                System.out.println(entry.getValue().toString());
                                Registry registry = LocateRegistry.getRegistry(entry.getValue().getIp(), entry.getValue().getPort());
                                NetNode node = (NetNode) registry.lookup(entry.getValue().toUrl());
                                System.out.println(node.replaceFile(cacheFileWrapper, cacheFileWrapper.getLastModifiedBeforeDownload(), cacheFileWrapper.getFile().getName()));

                            } catch (RemoteException e) {
                                e.printStackTrace();
                            } catch (NotBoundException e) {
                                e.printStackTrace();
                            }
                        }
                        else {
                            System.out.println("si tratta del nodo stesso");
                        }

                    }
                }
                fileList.clear();
                System.out.println("dimensione della cache : "+fileList.size());
                System.out.println("[PULIZIA CACHE] : Terminata la pulizia della cache");
            }
        }
    }
}


