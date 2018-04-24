package fs.actions.cache;

import fs.actions.object.WritingCacheFileWrapper;
import mediator_fs_net.MediatorFsNet;
import net.objects.NetNodeLocation;
import net.objects.interfaces.NetNode;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

//TODO: questa classe non serve più
public class WritingNodeCache {
    private ArrayList<WritingCacheFileWrapper> fileList;
    private final int replacedTimer = 60000; //ms
    private MediatorFsNet mediatorFsNet;

    public WritingNodeCache(MediatorFsNet mediatorFsNet) {
        fileList = new ArrayList<>();
        this.mediatorFsNet = mediatorFsNet;
        Timer timer = new Timer();
        timer.schedule(new ReplacerFile(), 0, replacedTimer);

    }


    public void add(WritingCacheFileWrapper writingCacheFileWrapper) {
        synchronized (fileList) {
            fileList.add(writingCacheFileWrapper);
        }
    }

    private class ReplacerFile extends TimerTask {
        public void run() {
            synchronized (fileList) {
                System.out.println("AVVIO PULIZIA CACHE");
                mediatorFsNet.replaceFile(fileList);
                fileList.clear();
                System.out.println("FINE PULIZIA CACHE");
            }
        }
    }
}


