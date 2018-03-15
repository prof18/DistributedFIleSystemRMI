package fs.actions;

import fs.objects.structure.FileAttribute;
import net.objects.NetNodeLocation;

import java.io.FileNotFoundException;
import java.util.Scanner;

public class ProvaFlatServiceImpl {
    public static void main(String[] args) throws FileNotFoundException {
        Scanner console=new Scanner(System.in);
        System.out.println("inserire il path ");
        String path=console.next();
        path="/home/zigio/Scrivania/"+path;
        System.out.println("ti vuoi connettere a qualcuno? ");
        NetNodeLocation location=null;
        if(console.next().equals("y"))
        {
            System.out.println("inserire host");
            String host=console.next();
            System.out.println("inserire porta");
            int port=console.nextInt();
            System.out.println("inserire nome");
            String name=console.next();
            location=new NetNodeLocation(host,port,name);
        }
        FlatServiceUtil.create(path,"localhost","computer",location);
    }

}
