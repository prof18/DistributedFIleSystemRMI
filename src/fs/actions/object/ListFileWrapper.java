package fs.actions.object;

import net.objects.NetNodeLocation;

import java.io.Serializable;
import java.util.ArrayList;

public class ListFileWrapper implements Serializable {
    private ArrayList<NetNodeLocation> locations;
    private boolean writable;

    public ListFileWrapper(ArrayList<NetNodeLocation> locations) {
        this.locations = locations;
        writable = true;
    }

    public ArrayList<NetNodeLocation> getLocations() {
        return locations;
    }

    public boolean isWritable() {
        return writable;
    }

    public void setWritable(boolean writable) {
        this.writable = writable;
    }
}

