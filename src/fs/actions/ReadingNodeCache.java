package fs.actions;

import java.util.HashMap;

public class ReadingNodeCache {
    private final long timeInterval = 3000;   //in ms
    HashMap<String, CacheFileWrapper> cache;

    public ReadingNodeCache() {
        cache = new HashMap<>();
    }

    public void put(String UFID, CacheFileWrapper file) {
        cache.put(UFID, file);
    }

    public CacheFileWrapper get(String UFID) {
        CacheFileWrapper ret = cache.get(UFID);
        return ret;
    }

    public long getTimeInterval() {
        return timeInterval;
    }
}
