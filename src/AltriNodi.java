import java.rmi.RemoteException;

public class AltriNodi {
    public static void main(String[] args) {
        Node node = null;
        Wrap nodi=null;
        try {
            node = new NodeImpl();
        } catch (RemoteException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        try {
            nodi=node.join("localhost","file","localhost");
            System.out.println("mi sono aggiunto al filesystem");
            System.out.println("dimensione nodi "+nodi.getNodes().length);
            for (String nodo:nodi.getNodes()){
                System.out.println(nodo);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        System.out.println("sono il nodo : "+nodi.getOwnNode());
        System.out.println("cosa vuoi fare ...");
        try {
            System.out.println("provo a comunicare con i miei amici");
            node.sayHello(nodi.getOwnNode());
        } catch (RemoteException e) {
            e.printStackTrace();
            System.exit(-1);
        }

    }
}
