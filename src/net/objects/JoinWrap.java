package net.objects;

import java.util.HashMap;

public class JoinWrap {

    private String name;
    private HashMap<Integer, NetNodeLocation> coNodes;

    public JoinWrap(String name, HashMap<Integer, NetNodeLocation> coNodes) {
        this.name = name;
        this.coNodes = coNodes;
    }

    public String getNameJoin(){
        return name;
    }

    public HashMap<Integer, NetNodeLocation> getCoNodesJoin() {
        return coNodes;
    }
}
