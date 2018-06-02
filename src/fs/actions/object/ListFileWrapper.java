package fs.actions.object;

import net.objects.NetNodeLocation;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * This class is used to contain all the
 * information about nodes that keeps a specific file
 */
public class ListFileWrapper implements Serializable {
    private ArrayList<NetNodeLocation> locations;
    private boolean writable;

    public ListFileWrapper(ArrayList<NetNodeLocation> locations) {
        this.locations = locations;
        writable=true;
    }

    public ArrayList<NetNodeLocation> getLocations() {
        return locations;
    }

    public void setLocations(ArrayList<NetNodeLocation> locations) {
        this.locations = locations;
    }

    public boolean isWritable() {
        return writable;
    }

    public void setWritable(boolean writable) {
        this.writable = writable;
    }
}

