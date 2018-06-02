package fs.actions.object;

import fs.actions.interfaces.FileService;
import net.objects.NetNodeLocation;
import net.objects.interfaces.NetNode;

import java.util.HashMap;

/**
 * This class is used to keeps and pass all the information related to the initialization of a node
 */
public class WrapperFileServiceUtil {
    private NetNodeLocation ownLocation;
    private HashMap<Integer, NetNodeLocation> locationHashMap;
    private FileService service;
    private NetNode netNode;


    public WrapperFileServiceUtil(NetNodeLocation ownLocation, HashMap<Integer, NetNodeLocation> locationHashMap,
                                  FileService service, NetNode netNode) {
        this.ownLocation = ownLocation;
        this.locationHashMap = locationHashMap;
        this.service = service;
        this.netNode = netNode;
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

    public NetNode getNetNode() {
        return netNode;
    }

}