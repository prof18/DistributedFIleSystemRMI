package net.objects;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * This class is used to contain all the information to reach a specific node, to creare a path.
 */
public class NetNodeLocation implements Serializable {
    private String ip;
    private int port;
    private String name;
    private long timeStamp;
    private int totalByte;


    /**
     * This is the constructor for the class without the name of the service
     *
     * @param ip   of the node
     * @param port where the service is installed
     */
    public NetNodeLocation(String ip, int port) {
        this.ip = ip;
        this.port = port;
        this.timeStamp = System.currentTimeMillis();

    }

    /**
     * This is the constructor for the class with the name of the service
     *
     * @param ip   of the node
     * @param port where the service is installed
     * @param name is the name of the service
     */
    public NetNodeLocation(String ip, int port, String name) {
        this.ip = ip;
        this.port = port;
        this.name = name;
    }

    /**
     * This method set the port number
     *
     * @param port is the port number
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * This method return the name of the service
     *
     * @return a string with the name of the service
     */
    public String getName() {
        return name;
    }


    /**
     * This method return the Ip address
     *
     * @return a string that contains the Ip Address
     */
    public String getIp() {
        return ip;
    }

    /**
     * This method return the number of the port
     *
     * @return the number of the port
     */
    public int getPort() {
        return port;
    }

    /**
     * This method is used to set the name of the service
     *
     * @param name is the new name to assign at the service
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * This method return a string that contains all the information about a service
     *
     * @return a String
     */
    @Override
    public String toString() {
        return "Address='" + ip + '\'' +
                " | Port=" + port +
                " | Hostname='" + name + '\'';
    }

    /**
     * return the address of the service
     *
     * @return a string that contains the address of the service
     */
    public String toUrl() {
        return "rmi://" + ip + ":" + port + "/" + name;
    }

    /**
     * this method compares two node locations
     *
     * @param location is a NetNodeLocation instance
     * @return a boolean that indicate if two nodes are equals
     */
    @Override
    public boolean equals(Object location) {
        if (this == location) return true;
        if (!(location instanceof NetNodeLocation)) return false;
        NetNodeLocation that = (NetNodeLocation) location;
        return getPort() == that.getPort() &&
                Objects.equals(getIp(), that.getIp()) &&
                Objects.equals(getName(), that.getName());
    }

    /**
     * This method is used to compute the hash function of the object
     *
     * @return the hash code of the object
     */
    @Override
    public int hashCode() {

        return Objects.hash(getIp(), getPort(), getName());
    }

    /**
     * This method is used to get the timestamp of the node creation
     *
     * @return a long value that corresponds to the timestamp of the creation
     */
    public long getTimeStamp() {
        return timeStamp;
    }

    /**
     * This method is used to get the number of bytes occupied in the own node by the files
     *
     * @return an int that correspond to the number of bytes occupied
     */
    public int getTotalByte() {
        return totalByte;
    }

    /**
     * This method is used to update the number of bytes occupied in the own node by the files
     *
     * @param tb is the new number of byte
     */
    public void setTotalByte(int tb) {
        totalByte = tb;
    }
}
