import java.rmi.RemoteException;

public class PrimoNodo {
    public static void main(String[] args){
        Node node=null;
        try {
            node=new NodeImpl();
        } catch (RemoteException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        try{
            //modificare questo ip
            node.create("file","localhost");
        }
        catch (RemoteException e){
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
