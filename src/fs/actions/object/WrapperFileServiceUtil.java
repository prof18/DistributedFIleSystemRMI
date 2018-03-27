package fs.actions.object;

import fs.actions.interfaces.FileService;
import net.objects.NetNodeLocation;

import java.util.HashMap;

public class WrapperFileServiceUtil {
    private NetNodeLocation ownLocation;
    private HashMap<Integer, NetNodeLocation> locationHashMap;
    private FileService service;

    public WrapperFileServiceUtil(NetNodeLocation ownLocation, HashMap<Integer, NetNodeLocation> locationHashMap, FileService service) {
        this.ownLocation = ownLocation;
        this.locationHashMap = locationHashMap;
        this.service = service;
    }

    public FileService getService() {
        return service;
    }

    public NetNodeLocation getOwnLocation() {
        return ownLocation;
    }

    public HashMap<Integer, NetNodeLocation> getLocationHashMap() {
        return locationHashMap;
    }
}
