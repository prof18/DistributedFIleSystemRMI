package fs.actions;

import fs.actions.CacheFileWrapper;
import net.objects.NetNodeLocation;
import net.objects.interfaces.NetNode;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class WritingNodeCache {
    private ArrayList<WritingCacheFileWrapper> fileList;
    private ArrayList<NetNodeLocation> nodeLocations;
    private final int replacedTimer=6000; //ms
    public WritingNodeCache() {
        fileList = new ArrayList<>();
        nodeLocations = new ArrayList<>();
        Timer timer=new Timer();
        timer.schedule(new ReplacerFile(),0,replacedTimer);
    }

    //devo aggiornare la lista ogni tot
    public void setNodeLocations(ArrayList<NetNodeLocation> nodeLocations) {
        synchronized (this.nodeLocations) {
            this.nodeLocations = nodeLocations;
        }
    }

    private class ReplacerFile extends TimerTask {
        public void run() {
            synchronized (fileList) {
                System.out.println("Iniziata la pulizia della cache");
                for (WritingCacheFileWrapper cacheFileWrapper : fileList) {
                    //devo trovare tutte le posizioni in cui è salvato il file e modificarlo
                    for (NetNodeLocation netNodeLocation : nodeLocations) {
                        try {
                            Registry registry = LocateRegistry.getRegistry(netNodeLocation.getIp(), netNodeLocation.getPort());
                            NetNode node = (NetNode) registry.lookup(netNodeLocation.toUrl());
                            System.out.println(node.replaceFile(cacheFileWrapper, cacheFileWrapper.getLastModifiedBeforeDownload(), cacheFileWrapper.getFile().getName()));

                        } catch (RemoteException e) {
                            e.printStackTrace();
                        } catch (NotBoundException e) {
                            e.printStackTrace();
                        }
                    }
                    fileList.remove(cacheFileWrapper);
                }
                System.out.println("Terminata la pulizia della cache");
            }
        }
    }
}


