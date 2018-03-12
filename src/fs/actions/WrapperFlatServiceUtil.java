package fs.actions;

import net.objects.NetNodeLocation;

import java.util.HashMap;

public class WrapperFlatServiceUtil {
    private NetNodeLocation ownLocation;
    private HashMap<Integer, NetNodeLocation> locationHashMap;

    public WrapperFlatServiceUtil(NetNodeLocation ownLocation, HashMap<Integer, NetNodeLocation> locationHashMap) {
        this.ownLocation = ownLocation;
        this.locationHashMap = locationHashMap;
    }

    public NetNodeLocation getOwnLocation() {
        return ownLocation;
    }

    public HashMap<Integer, NetNodeLocation> getLocationHashMap() {
        return locationHashMap;
    }
}
