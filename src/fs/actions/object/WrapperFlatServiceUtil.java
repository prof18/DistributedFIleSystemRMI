package fs.actions.object;

import fs.actions.interfaces.FlatService;
import net.objects.NetNodeLocation;

import java.util.ArrayList;
import java.util.HashMap;

public class WrapperFlatServiceUtil {
    private NetNodeLocation ownLocation;
    private HashMap<Integer, NetNodeLocation> locationHashMap;
    private FlatService service;
    private HashMap<String, ArrayList<NetNodeLocation>> netNodeList; //hashmap file-nodi che possiedono una copia di tale file.

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

    public HashMap<String, ArrayList<NetNodeLocation>> getNetNodeList() {
        return netNodeList;
    }

    public void setNetNodeList(HashMap<String, ArrayList<NetNodeLocation>> netNodeList) {
        this.netNodeList = netNodeList;
    }
}
