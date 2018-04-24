package net.objects;

import java.rmi.registry.Registry;

/**
 * This class is used to wrap the port and the registry, in the returned value of the method getNextFreePort of the class Util
 */
public class RegistryWrapper {
    private int port;
    private Registry registry;

    /**
     * Is the constructor of the class
     *
     * @param port     is the number of the service port
     * @param registry is the registry
     */
    public RegistryWrapper(int port, Registry registry) {
        this.port = port;
        this.registry = registry;
    }

    /**
     * @return the number of the port
     */
    public int getPort() {
        return port;
    }

    /**
     * @return the instance of the object registry
     */
    public Registry getRegistry() {
        return registry;
    }
}
