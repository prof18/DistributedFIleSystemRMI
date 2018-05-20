package net.objects;

import fs.actions.object.ListFileWrapper;

import java.io.Serializable;
import java.util.HashMap;

/**
 * This is class is used to wrap the name of service and the list of connected nodes, that are
 * returned after a join operation
 */
public class JoinWrap implements Serializable {

    private String name;
    private HashMap<Integer, NetNodeLocation> coNodes;
    HashMap<String, ListFileWrapper> fileNodeList;

    /**
     * It is the constructor of the class
     *
     * @param name    is the name of the service
     * @param coNodes is the list of connected nodes
     */
    public JoinWrap(String name, HashMap<Integer, NetNodeLocation> coNodes , HashMap<String, ListFileWrapper> fileNodeList ) {
        this.name = name;
        this.coNodes = coNodes;
        this.fileNodeList = fileNodeList;
    }

    /**
     * It is used to get the name of the service
     *
     * @return the string that contains the name of the service
     */
    public String getNameJoin() {
        return name;
    }

    /**
     * It is used to get the list of connected nodes of the distributed FS
     *
     * @return the list of connected nodes of the distributed FS
     */
    public HashMap<Integer, NetNodeLocation> getCoNodesJoin() {
        return coNodes;
    }

    public HashMap<String, ListFileWrapper> getFileNodeList(){return fileNodeList; }
}
