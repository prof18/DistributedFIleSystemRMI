package fs.actions;

import fs.objects.structure.FileAttribute;

import java.io.Serializable;

/**
 * This object contains all the information necessary for the replication stuff
 */
public class ReplicationWrapper implements Serializable {
    private String path;
    private String fileName;
    private String UFID;
    private FileAttribute attribute;
    private byte[] content;
    private String checksum;
    private String json;


    public ReplicationWrapper(String UFID, String fileName) {
        this.UFID = UFID;
        this.fileName = fileName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFileName() {
        return fileName;
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

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public String getJSON() {
        return json;
    }

    public void setJSON(String jSon) {
        this.json = json;
    }
}
