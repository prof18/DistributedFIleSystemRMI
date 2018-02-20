import java.io.Serializable;
import java.rmi.Remote;

public interface DirectoryService extends Remote, Serializable {
    String lookup(String dir, String name);

    void addName(String dir, String name, String fileID);

    void unName(String dir, String name);

    String getNames(String dir, String pattern);
}
