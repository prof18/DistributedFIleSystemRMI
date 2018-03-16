package fs.actions;

import fs.actions.interfaces.FlatService;
import fs.actions.object.WrapperFlatServiceUtil;
import fs.objects.structure.FileAttribute;
import net.objects.NetNodeLocation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;
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
        WrapperFlatServiceUtil flatServiceUtil = FlatServiceUtil.create(path, "localhost", "computer", location);
        FlatService fs = flatServiceUtil.getService();

        while (true) {
            System.out.println("vuoi leggere un file");
            if (console.next().equals("y")) {
                System.out.println("inserire il nome del file");
                String nome = console.next();
                try {
                    fs.read(nome, 0);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
