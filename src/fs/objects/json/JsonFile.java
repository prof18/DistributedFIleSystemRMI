package fs.objects.json;

import fs.objects.structure.FileAttribute;

import java.util.Objects;

/**
 * This object represent a file in the JSON
 */
public class JsonFile {

    private String fileName;
    private String UFID;
    private String path;
    private FileAttribute attribute;

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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "JsonFile{" +
                "fileName='" + fileName + '\'' +
                ", UFID='" + UFID + '\'' +
                ", attribute=" + attribute +
                '}';
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JsonFile)) return false;
        JsonFile jsonFile = (JsonFile) o;
        return Objects.equals(fileName, jsonFile.fileName) &&
                Objects.equals(UFID, jsonFile.UFID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName, UFID);
    }
}
