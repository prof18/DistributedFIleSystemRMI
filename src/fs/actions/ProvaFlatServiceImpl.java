package fs.actions;

import fs.objects.structure.FileAttribute;

import java.io.FileNotFoundException;
import java.util.Scanner;

public class ProvaFlatServiceImpl {
    public static void main(String[] args) throws FileNotFoundException {

        Scanner console = new Scanner(System.in);
        System.out.println("inserire il path");
        String path = "/home/zigio/Scrivania/" + console.next() + "/";
        //System.out.println("inserire l'host");
        //String host=console.next();
        String host = "localhost";
        //System.out.println("inserire il nome del servizio");
        //String nameService=console.next();
        String nameService = "host";
        FlatServiceImpl flatService = new FlatServiceImpl(path, host, nameService);
        System.out.println("vuoi creare il file ?");
        String ret = console.next();
        if (ret.equals("y")) {
            try {
                flatService.create("ciao");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("vuoi leggere gli attributi del file ?");
        ret = console.next();
        if (ret.equals("y")) {
            try {
                FileAttribute fileAttribute = flatService.getAttributes("ciao");
                System.out.println(fileAttribute.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        while (true) {
            System.out.println("vuoi leggere il file ?");
            ret = console.next();
            if (ret.equals("y")) {
                flatService.read("ciao", 0);
            }
            System.out.println("vuoi scrivere ciao ?");
            ret = console.next();
            if(ret.equals("y")){
                flatService.write("ciao",0,15,"nuova scrittura".getBytes());
            }

        }
    }
}
