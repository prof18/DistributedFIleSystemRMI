package fs.actions;

import fs.objects.structure.FileAttribute;

import java.io.Serializable;

/**
 * This object contains all the information necessary for the replication stuff
 */
public class ReplicationWrapper implements Serializable {
    private String path;
    private String UFID;
    private FileAttribute attribute;
    private byte[] content;
    private String checksum;
    private String json;


    public ReplicationWrapper(String UFID) {
        this.UFID = UFID;;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
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

    public void setJSON(String json) {
        this.json = json;
    }
}
