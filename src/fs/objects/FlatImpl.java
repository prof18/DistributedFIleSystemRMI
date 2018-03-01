package fs.objects;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class FlatImpl {

    private String path;

    public FlatImpl(String path) {
        this.path = path;
    }

    //sistemare se count è maggiore della lunghezza
    public static void main(String[] args) {
        FlatImpl impl = new FlatImpl("/home/zigio/Scrivania/prova/");
        String a = "andrea";
        String b = "davide";
        byte[] ab = a.getBytes();
        byte[] bb = b.getBytes();
        impl.write("ciao", 0, ab.length, ab);
        System.out.println(new String(impl.read("ciao", 0, 6)));
        //ho dei problemi in questo punto
        impl.write("ciao", 1, bb.length, bb);
        System.out.println(new String(impl.read("ciao", 0, 7)));

    }


    public byte[] read(String fileID, int offset, int count) {
        FileInputStream fileInputStream = null;
        byte[] content = null;
        try {
            File file = new File(path + fileID);
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

    public byte[] read(String fileID, int offset) {
        File file = new File(path + fileID);
        long length = file.length();
        System.out.println("[READ]: lunghezza file " + length);
        byte[] content = read(fileID, offset, (int) length);
        System.out.println("[READ]: contenuto: " + new String(content));
        return content;
    }


    public void write(String fileID, int offset, int count, byte[] data) {
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
            file.delete();
            file = new File(path + fileID);
            fileOutputStream = new FileOutputStream(file);
            System.out.println("newContent = " + new String(newContent));
            fileOutputStream.write(newContent, 0, newContent.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public String create(String dir, String name, FileAttribute attribute) {
        return null;
    }

    public void delete(String fileID) {

    }


    public FileAttribute getAttributes(String fileID) {
        return null;
    }


    public void setAttributes(String fileID, FileAttribute attr) {

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
