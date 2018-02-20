import java.io.Serializable;
import java.rmi.Remote;

public interface FlatService extends Remote, Serializable {
    void Create();
    void Delete(int fileID);
}
