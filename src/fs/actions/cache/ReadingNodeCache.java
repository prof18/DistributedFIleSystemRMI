package fs.actions.cache;

import fs.actions.object.CacheFileWrapper;

import java.util.HashMap;


public class ReadingNodeCache {
    private HashMap<String, CacheFileWrapper> cache;

    public ReadingNodeCache() {
        cache = new HashMap<>();
    }

    public void put(String UFID, CacheFileWrapper file) {
        cache.put(UFID, file);
    }

    public CacheFileWrapper get(String UFID) {
        return cache.get(UFID);
    }

    public long getTimeInterval() {
        long timeInterval = 100000;
        return timeInterval;
    }
}
