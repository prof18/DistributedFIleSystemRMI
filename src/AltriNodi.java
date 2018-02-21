import java.rmi.RemoteException;

public class AltriNodi {
    public static void main(String[] args) {
        Node node = null;
        try {
            node = new NodeImpl();
        } catch (RemoteException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        try {
            node.join("localhost","file","localhost");
            System.out.println("mi sono aggiunto al filesystem");
        } catch (RemoteException e) {
            e.printStackTrace();
            System.exit(-1);
        }

    }
}
