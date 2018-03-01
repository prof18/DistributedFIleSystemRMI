package net.objects;

import java.io.Serializable;
import java.util.HashMap;

public class NetNodeWrap implements Serializable {
    private HashMap<String, NetNodeLocation> nodes;
    private String ownNode;

    public NetNodeWrap(HashMap<String, NetNodeLocation> nodes, String ownNode) {
        this.nodes = nodes;
        this.ownNode = ownNode;
    }

    public HashMap<String, NetNodeLocation> getNodes() {
        return nodes;
    }

    public String getOwnNode() {
        return ownNode;
    }
}
