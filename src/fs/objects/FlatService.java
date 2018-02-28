package fs.objects;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.Remote;

public interface FlatService extends Remote, Serializable {

    byte[] read(String fileID, int i, int n);

    byte[] read(String fileId, int offset);

    void write(String fileID, int i, int count, byte[] data);

    String create(String dir, String name, FileAttribute attribute);

    void delete(String fileID);

    FileAttribute getAttributes(String fileID);

    void setAttributes(String fileID, FileAttribute attr);
}