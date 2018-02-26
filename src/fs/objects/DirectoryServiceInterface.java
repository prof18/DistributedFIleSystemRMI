package fs.objects;

import java.io.Serializable;
import java.rmi.Remote;

public interface DirectoryServiceInterface extends Remote, Serializable {
    String lookup(String dir, String name);

    void addName(String dir, String name, String fileID);

    void unName(String dir, String name);

    String getNames(String dir, String pattern);
}