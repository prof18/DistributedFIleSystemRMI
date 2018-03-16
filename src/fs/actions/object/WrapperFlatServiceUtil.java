package fs.actions.object;

import fs.actions.FlatServiceImpl;
import fs.actions.interfaces.FlatService;
import net.objects.NetNodeLocation;

import java.util.HashMap;

public class WrapperFlatServiceUtil {
    private NetNodeLocation ownLocation;
    private HashMap<Integer, NetNodeLocation> locationHashMap;
    private FlatService service;

    public WrapperFlatServiceUtil(NetNodeLocation ownLocation, HashMap<Integer, NetNodeLocation> locationHashMap, FlatService service) {
        this.ownLocation = ownLocation;
        this.locationHashMap = locationHashMap;
        this.service = service;
    }

    public FlatService getService() {
        return service;
    }

    public NetNodeLocation getOwnLocation() {
        return ownLocation;
    }

    public HashMap<Integer, NetNodeLocation> getLocationHashMap() {
        return locationHashMap;
    }
}
