package fs.objects;


import java.io.*;

public class FlatImpl implements FlatService {

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

    @Override
    public byte[] read(String fileID, int offset, int count) {
        FileInputStream fileInputStream = null;
        byte[] content = null;
        try {
            File file = new File(path + fileID);
            System.out.println("[READ XX] lunghezza file: "+file.length());
            content = new byte[(int) file.length()];
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(content, offset, count);
            System.out.println("[READ XX] contenuto: "+new String(content));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    public byte[] read(String fileID, int offset) {
        File file = new File(path+fileID);
        long length = file.length();
        System.out.println("[READ]: lunghezza file "+length);
try{
    byte[] content = read(fileID, offset, (int) length);
}
catch (FileNotFoundException e){
    e.printStackTrace();
}
        System.out.println("[READ]: contenuto: "+new String(content));
        return content;
    }

    @Override
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
            fileOutputStream.write(newContent, offset, count);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String create(String dir, String name, FileAttribute attribute) {
        return null;
    }

    @Override
    public void delete(String fileID) {

    }

    @Override
    public FileAttribute getAttributes(String fileID) {
        return null;
    }

    @Override
    public void setAttributes(String fileID, FileAttribute attr) {

    }

    private byte[] joinArray(byte[] first, byte[] second, int offset, int count) throws Exception {
        System.out.println("[JOINARRAY]: prima stringa "+new String(first));
        System.out.println("[JOINARRAY]: seconda stringa "+new String(second));
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
        System.out.println("[JOIN]: la stringa concatenata: "+new String(ret));
        for (int k = 0; k < ret.length; k++) {
            System.out.println(ret[k]);
        }
        return ret;
    }
}
