package test;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Hello extends Remote,Serializable {
    String sayHello() throws RemoteException;
}
