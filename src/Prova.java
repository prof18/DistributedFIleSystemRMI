import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class Prova {
    public static void main(String[] args) throws RemoteException, MalformedURLException, NotBoundException {
        Hello hello =(Hello)Naming.lookup("//localhost/hello");
        System.out.println(hello.sayHello());
    }
}
