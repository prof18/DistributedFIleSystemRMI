package fs.objects.structure;

import utils.Util;

import java.io.Serializable;

public class FileWrapper implements Serializable {

    private String fileName;
    private String UFID;
    private String path;
    private FileAttribute attribute;
    private byte[] content;
    private String checksum;

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

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
        checksum = Util.getChecksum(content); //compute checksum
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

    public String getChecksum() {
        return checksum;
    }
}
