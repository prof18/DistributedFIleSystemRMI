package net.objects;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class NetNodeLocation implements Serializable {
    private String ip;
    private int port;
    private String name;
    private long timeStamp = new Date().getTime();

    public NetNodeLocation(String ip, int port, String name) {
        this.ip = ip;
        this.port = port;
        this.name = name;

    }

    public String getName() {
        return name;
    }

    public long getTimeStamp(){return timeStamp;}

    public void setName(String name){this.name = name;}

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
        return "rmi://" + ip + ":" + port + "/"+name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NetNodeLocation)) return false;
        NetNodeLocation that = (NetNodeLocation) o;
        return getPort() == that.getPort() &&
                Objects.equals(getIp(), that.getIp()) &&
                Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getIp(), getPort(), getName());
    }
}