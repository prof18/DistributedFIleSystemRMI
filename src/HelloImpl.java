import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class HelloImpl extends UnicastRemoteObject implements Hello {
    public HelloImpl() throws RemoteException {
        super();
    }

    public static void main(String[] args) throws RemoteException, AlreadyBoundException, MalformedURLException {
        String ip="10.8.0.4";
        int port=1099;
        Hello hello = new HelloImpl();
        System.setProperty("java.rmi.server.hostname", ip);
        Registry registry = LocateRegistry.createRegistry(port);
        String connectPath = "rmi://" + ip + "/" + "hello";
        System.out.println(connectPath);
        System.out.println(registry);
        registry.rebind(connectPath, hello);

    }

    public String sayHello() throws RemoteException {
        System.out.println("hai salutato qualcuno");
        return "Ciao bello";
    }
}
