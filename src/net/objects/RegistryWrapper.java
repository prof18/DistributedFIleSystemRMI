package net.objects;

import java.rmi.registry.Registry;

public class RegistryWrapper {
    private int port;
    private Registry registry;

    public RegistryWrapper(int port, Registry registry) {
        this.port = port;
        this.registry = registry;
    }

    public int getPort() {
        return port;
    }

    public Registry getRegistry() {
        return registry;
    }
}
