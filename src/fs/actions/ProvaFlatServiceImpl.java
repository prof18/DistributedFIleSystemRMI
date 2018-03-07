package fs.actions;

import java.io.FileNotFoundException;
import java.util.Scanner;

public class ProvaFlatServiceImpl {
    public static void main(String[] args) throws FileNotFoundException {
        Scanner console=new Scanner(System.in);
        System.out.println("inserire il path");
        String path=console.next();
        System.out.println("inserire l'host");
        String host=console.next();
        System.out.println("inserire il nome del servizio");
        String nameService=console.next();
        FlatServiceImpl flatService=new FlatServiceImpl(path,host,nameService);
        System.out.println("vuoi leggere il file ?");
        String ret=console.next();
        if(ret.equals("y")){
            flatService.read("ciao",0);
        }
    }
}
