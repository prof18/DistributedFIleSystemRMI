package utils;

import net.objects.NodeLocation;

import java.util.HashMap;
import java.util.Map;

public class Util {
    public static void plot(HashMap<String, NodeLocation> hashMap){
        String leftAlignFormat = "| %-10s | %-10s | %-8d | %n";

        System.out.format("+------------+------------+----------+%n");
        System.out.format("| NameHost   | Ip         | Port     |%n");
        System.out.format("+------------+------------+----------+%n");

        for(Map.Entry<String,NodeLocation> entry:hashMap.entrySet()){
            System.out.format(leftAlignFormat,entry.getKey(),entry.getValue().getIp(),entry.getValue().getPort());
            System.out.format("+------------+------------+----------+%n");
        }

    }
}