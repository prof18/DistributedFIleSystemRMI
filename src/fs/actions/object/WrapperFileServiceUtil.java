package fs.actions.object;

import fs.actions.interfaces.FileService;
import net.objects.NetNodeLocation;
import net.objects.interfaces.NetNode;

import java.util.ArrayList;
import java.util.HashMap;

public class WrapperFileServiceUtil {
    private NetNodeLocation ownLocation;
    private HashMap<Integer, NetNodeLocation> locationHashMap;
    private FileService service;
    private NetNode netNode;
    private HashMap<String, ArrayList<NetNodeLocation>> netNodeList = new HashMap(); //hashmap file-nodi che possiedono una copia di tale file.

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

    public HashMap<String, ArrayList<NetNodeLocation>> getNetNodeList() {
        return netNodeList;
    }

    public NetNode getNetNode() {
        return netNode;
    }

    public void nodeFileAssociation(String UFID, NetNodeLocation netNode) {
        if (!netNodeList.containsKey(UFID)) {
            ArrayList<NetNodeLocation> a = new ArrayList<>();
            a.add(netNode);
            netNodeList.put(UFID, a);
        } else {
            netNodeList.get(UFID).add(netNode);
        }
    }
}