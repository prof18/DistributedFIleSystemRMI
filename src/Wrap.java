import java.io.Serializable;

public class Wrap implements Serializable {
    private String[] nodes;
    private String ownNode;

    public Wrap(String[] nodes, String ownNode) {
        this.nodes = nodes;
        this.ownNode = ownNode;
    }

    public String[] getNodes() {
        return nodes;
    }

    public String getOwnNode() {
        return ownNode;
    }
}
