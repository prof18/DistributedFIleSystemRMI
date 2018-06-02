package fs.actions.cache;

import fs.actions.object.CacheFileWrapper;

import java.util.HashMap;

/**
 * This class implements the reading cache used by the nodes to
 * improved the performance and reduces the information exchange
 */

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

    @SuppressWarnings("UnusedReturnValue")
    public boolean remove(String UFID) {
        CacheFileWrapper ret = cache.remove(UFID);
        return ret != null;
    }

    public long getTimeInterval() {
        return 100000L;
    }
}
