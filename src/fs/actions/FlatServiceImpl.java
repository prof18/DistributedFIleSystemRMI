package fs.actions;

/*
FlatServiceImpl è ad un livello superiore rispetto a NodeImpl e quindi lo inizializza
 */
import fs.actions.interfaces.FlatService;
import fs.objects.structure.FileAttribute;
import net.objects.NetNodeImpl;
import net.objects.interfaces.NetNode;

import java.io.*;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;


public class FlatServiceImpl implements FlatService {

    private NetNode netNode;
    private String path;

    public FlatServiceImpl(String path,String ownIP,String nameService) {
        Scanner scanner=new Scanner(System.in);
        this.path = path;
        this.netNode=FlatServiceUtil.create(ownIP,nameService);
        System.out.println("vuoi connetterti ad un host?");
        if(scanner.next().equals("y")){
            System.out.println("inserire indirizzo nuovo host");
            String masterIP=scanner.next();
            System.out.println("inserire porta nuovo host");
            int portMaster=scanner.nextInt();
            System.out.println("inserire nome nuovo servizio");
            String nameServiceMaster=scanner.next();
            FlatServiceUtil.connect(netNode,masterIP,portMaster,nameServiceMaster);
        }

    }


    public byte[] read(String fileID, int offset, int count) throws FileNotFoundException {
        FileInputStream fileInputStream = null;
        byte[] content = null;
        try {
            File file = new File(path + fileID);
            if (!file.exists()) {
                //provo a vedere se è nella cache o negli altri nodi
                throw new FileNotFoundException();
            }
            System.out.println("[READ XX] lunghezza file: " + file.length());
            content = new byte[(int) file.length()];
            fileInputStream = new FileInputStream(file);
            System.out.println("count = " + count);
            System.out.println("length content = " + content.length);
            fileInputStream.read(content, offset, count);
            System.out.println("[READ XX] contenuto: " + new String(content));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    public byte[] read(String fileID, int offset) throws FileNotFoundException {
        File file = new File(path + fileID);
        if (!file.exists()) {
            throw new FileNotFoundException();
        }
        long length = file.length();
        System.out.println("[READ]: lunghezza file " + length);
        byte[] content = read(fileID, offset, (int) length);
        System.out.println("[READ]: contenuto: " + new String(content));
        return content;
    }


    public void write(String fileID, int offset, int count, byte[] data) throws FileNotFoundException {
        byte[] newContent = null;
        try {
            byte[] content = read(fileID, 0);
            newContent = new byte[0];
            newContent = joinArray(content, data, offset, count);
        } catch (Exception e) {
            System.out.println("problema con gli indici");
            e.printStackTrace();
            System.exit(-1);
        }

        FileOutputStream fileOutputStream = null;
        try {
            File file = new File(path + fileID);
            System.out.println(file.delete());
            file = new File(path + fileID);
            fileOutputStream = new FileOutputStream(file);
            System.out.println("newContent = " + new String(newContent));
            fileOutputStream.write(newContent, 0, newContent.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public String create(String name, FileAttribute attribute) throws Exception {
        File file = new File(name);
        if (file.exists()) {
            throw new Exception();
        }
        file.createNewFile();
        File fileAttr = new File(name + ".attr");
        FileOutputStream fileOutputStream = new FileOutputStream(name + ".attr");
        ObjectOutputStream outputStream = new ObjectOutputStream(fileOutputStream);
        outputStream.writeObject(fileAttr);
        outputStream.close();
        return "dir" + "name";
    }

    @Override
    public String create(String name) throws Exception {
        Date date = Date.from(Instant.now());
        FileAttribute attribute = new FileAttribute(0, date, date, 1);
        create(path + name, attribute);
        return path + name;
    }

    public void delete(String fileID) {
        File file = new File(path + fileID);
        File fileAttr = new File(path + fileID + ".attr");
        System.out.println(file.delete());
        System.out.println(fileAttr.delete());
    }


    public FileAttribute getAttributes(String fileID) {
        FileInputStream fileInputStream = null;
        ObjectInputStream objectInputStream = null;
        FileAttribute fileAttribute = null;
        try {
            fileInputStream = new FileInputStream(fileID + ".attr");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            objectInputStream = new ObjectInputStream(fileInputStream);
            fileAttribute = (FileAttribute) objectInputStream.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return fileAttribute;

    }


    public void setAttributes(String fileID, FileAttribute attr) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(fileID + ".attr");
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(attr);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] joinArray(byte[] first, byte[] second, int offset, int count) throws Exception {
        System.out.println("[JOINARRAY]: prima stringa : " + new String(first));
        System.out.println("[JOINARRAY]: seconda stringa : " + new String(second));
        byte[] ret = new byte[first.length + second.length];
        int i = 0;
        if (offset > first.length) throw new Exception();
        while (i < first.length && i < offset) {
            ret[i] = first[i];
            i++;
        }
        int j = 0;
        while (j < count && j < second.length) {
            ret[i] = second[j];
            j++;
            i++;
        }
        while (i < first.length) {
            ret[i] = first[i];
            i++;
        }
        System.out.println("[JOIN]: la stringa concatenata: " + new String(ret));
        byte[] cleanRet = cleanArray(ret);
        System.out.println("[JOIN]: la stringa concatenata pulita: " + new String(cleanRet));
        return cleanRet;
    }

    private byte[] cleanArray(byte[] tmp) {
        int count = tmp.length - 1;
        while (tmp[count] == 0) {
            count--;
        }
        byte[] newArray = Arrays.copyOfRange(tmp, 0, count + 1);
        return newArray;


    }
}
