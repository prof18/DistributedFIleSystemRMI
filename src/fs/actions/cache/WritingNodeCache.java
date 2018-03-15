package fs.actions.cache;

import fs.actions.object.WritingCacheFileWrapper;
import net.objects.NetNodeLocation;
import net.objects.interfaces.NetNode;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

public class WritingNodeCache {
    private ArrayList<WritingCacheFileWrapper> fileList;
    private final int replacedTimer = 20000; //ms


    public WritingNodeCache() {
        fileList = new ArrayList<>();
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
               //TODO delegare al MEDIATOR questa cosa
            }
        }
    }
}


