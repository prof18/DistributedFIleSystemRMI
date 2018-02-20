import javax.naming.Name;
import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

public class HelloImpl extends UnicastRemoteObject implements  Hello{
    public HelloImpl() throws RemoteException {
        super();
    }
    public String sayHello() throws RemoteException{
        System.out.println("hai salutato qualcuno");
        return "Ciao bello";
    }
    public static void main(String[] args) throws RemoteException, AlreadyBoundException, MalformedURLException {
        Hello hello=new HelloImpl();
        Naming.bind("/localhost/hello",hello);
    }
}
