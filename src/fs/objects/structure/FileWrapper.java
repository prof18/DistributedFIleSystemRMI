package fs.objects.structure;

import java.io.Serializable;

/**
 * This object contains all the information needed to describe a file
 */
public class FileWrapper implements Serializable {

    private String fileName;
    private String UFID;
    private String path;
    private FileAttribute attribute;
    private byte[] content;
    private String checksum;

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public FileWrapper() {
    }

    public FileWrapper(String UFID, String fileName) {
        this.UFID = UFID;
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getUFID() {
        return UFID;
    }

    public void setUFID(String UFID) {
        this.UFID = UFID;
    }

    public FileAttribute getAttribute() {
        return attribute;
    }

    public void setAttribute(FileAttribute attribute) {
        this.attribute = attribute;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return fileName;
    }
}
