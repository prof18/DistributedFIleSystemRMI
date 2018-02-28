package net.objects;

import java.io.Serializable;

public class NodeLocation implements Serializable {
    private String ip;
    private int port;

    public NodeLocation(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return "ip= " + ip + " " + "port= " + port;
    }
    public String toUrl(){
        return "rmi://"+ip+":"+port+"/";
    }
}