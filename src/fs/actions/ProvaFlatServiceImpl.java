package fs.actions;

public class ProvaFlatServiceImpl {
    public static void main(String[] args){
        String path="/home/zigio/Scrivania/prova/";
        String host="localhost";
        String nameService="prova";
        FlatServiceImpl flatService=new FlatServiceImpl(path,host,nameService);
    }
}
