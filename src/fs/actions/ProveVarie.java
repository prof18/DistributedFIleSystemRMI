package fs.actions;

public class ProveVarie {
    public static void main(String[] args){
        FlatServiceImpl flatService=new FlatServiceImpl("/home/zigio/Scrivania/t2/","localhost","prova",null);

        flatService.write("prova",0,5,"ciao".getBytes());
    }
}
