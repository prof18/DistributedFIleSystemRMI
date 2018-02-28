package fs.objects;

import fs.objects.FileAttribute;

import java.io.Serializable;
import java.rmi.Remote;

public interface FlatService extends Remote, Serializable {

    Object read(String fileID, int i, int n);

    void write(String fileID, int i, Object data);

    String Create();

    void Delete(String fileID);

    FileAttribute getAttributes(String fileID);

    void setAttributes(String fileID, FileAttribute attr);
}