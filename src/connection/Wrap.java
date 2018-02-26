package connection;

import java.io.Serializable;
import java.util.HashMap;

public class Wrap implements Serializable {
    private HashMap<String, NodeLocation> nodes;
    private String ownNode;

    public Wrap(HashMap<String, NodeLocation> nodes, String ownNode) {
        this.nodes = nodes;
        this.ownNode = ownNode;
    }

    public HashMap<String, NodeLocation> getNodes() {
        return nodes;
    }

    public String getOwnNode() {
        return ownNode;
    }
}
