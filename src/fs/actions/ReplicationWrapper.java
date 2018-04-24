package fs.actions;

import fs.objects.structure.FileAttribute;

public class ReplicationWrapper {
    private String path;
    private String fileName;
    private String UFID;
    private FileAttribute attribute;
    private byte[] content;
    private String checksum;
    private String jSon;


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

    public String getjSon() {
        return jSon;
    }

    public void setjSon(String jSon) {
        this.jSon = jSon;
    }
}
