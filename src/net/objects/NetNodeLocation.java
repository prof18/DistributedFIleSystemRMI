package net.objects;

import java.io.Serializable;

public class NetNodeLocation implements Serializable {
    private String ip;
    private int port;
    private String name;

    public NetNodeLocation(String ip, int port, String name) {
        this.ip = ip;
        this.port = port;
        this.name = name;

    }

    public String getName() {
        return name;
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

    public String toUrl() {
        return "rmi://" + ip + ":" + port + "/";
    }
}
