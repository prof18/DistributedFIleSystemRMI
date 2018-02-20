import interfaces.Hello;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Prova {
    public static void main(String[] args) throws RemoteException, MalformedURLException, NotBoundException {

        Registry registry = LocateRegistry.getRegistry("10.8.0.4");
        Hello hello = (Hello) registry.lookup("rmi://10.8.0.4/hello");
        System.out.println(hello.sayHello());
    }
}
