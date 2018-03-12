package fs.actions;

import fs.objects.structure.FileAttribute;
import net.objects.NetNodeLocation;

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
        System.out.println("ti vuoi connettere a qualcuno");
        String risposta=console.next();
        NetNodeLocation netNodeLocation=null;
        if(risposta.equals("y")){
            System.out.println("inserire l'indirizzo ip");
            String ip=console.next();
            System.out.println("inserire il numero di porta");
            int port=console.nextInt();
            System.out.println("inserire il nome del servizio");
            String servizio=console.next();
            netNodeLocation=new NetNodeLocation(ip,port,servizio);
        }
        FlatServiceImpl flatService = new FlatServiceImpl(path, host, nameService,netNodeLocation);
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
