package fs.actions;

import fs.actions.interfaces.FlatService;
import fs.actions.object.WrapperFlatServiceUtil;
import net.objects.NetNodeLocation;

import java.io.FileNotFoundException;
import java.util.Scanner;

public class ProvaFlatServiceImpl {
    public static void main(String[] args) {
        Scanner console = new Scanner(System.in);
        System.out.println("inserire il path ");
        String path = console.next();
        path = "/home/zigio/Scrivania/" + path + "/";
        System.out.println(path);
        System.out.println("ti vuoi connettere a qualcuno? ");
        NetNodeLocation location = null;
        if (console.next().equals("y")) {
            System.out.println("inserire host");
            String host = console.next();
            System.out.println("inserire porta");
            int port = console.nextInt();
            System.out.println("inserire nome");
            String name = console.next();
            location = new NetNodeLocation(host, port, name);
        }
        WrapperFlatServiceUtil flatServiceUtil = FlatServiceUtil.create(path, "localhost", "host", location);
        FlatService fs = flatServiceUtil.getService();
        String serviceName = flatServiceUtil.getOwnLocation().getName();

        while (true) {
            System.out.println("vuoi leggere un file");
            if (console.next().equals("y")) {
                System.out.println("inserire il nome del file");
                String nome = console.next();
                try {
                    fs.read(nome, 0);
                } catch (FileNotFoundException e) {
                    System.out.println("file non trovato");
                }
            }
            System.out.println("vuoi scrivere su un file");
            if (console.next().equals("y")) {
                System.out.println("inserire il nome del file");
                String nome = console.next();
                console.nextLine();
                System.out.println("inserire cosa scrivere");
                String testo = console.nextLine();
                try {
                    fs.write(nome, 0, testo.length(), testo.getBytes());
                } catch (FileNotFoundException e) {
                    System.out.println("il file non è stato trovato");
                }
            }
            System.out.println("vuoi creare un nuovo file?");
            if (console.next().equals("y")) {
                try {
                    System.out.println("creato il file : " + fs.create(serviceName));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            System.out.println("vuoi eliminare un file?");
            if (console.next().equals("y")) {
                System.out.println("inserire il nome del file da eliminare");
                fs.delete(console.next());
            }

            System.out.println("vuoi leggere gli attributi di un file?");
            if(console.next().equals("y")){
                System.out.println("inserire il nome del file da esaminare");
                try {
                    System.out.println(fs.getAttributes(console.next()).toString());
                } catch (FileNotFoundException e) {
                    System.out.println("file non trovato");
                }
            }
        }
    }

}
