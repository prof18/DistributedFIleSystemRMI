package fileSystem.json;

import fs.objects.FileAttribute;

public class JsonFile {

    private String fileName;
    private String UFID;
    private FileAttribute attribute;

    public JsonFile() {
    }

    public JsonFile(String fileName, String UFID, FileAttribute attribute) {
        this.fileName = fileName;
        this.UFID = UFID;
        this.attribute = attribute;
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
}
