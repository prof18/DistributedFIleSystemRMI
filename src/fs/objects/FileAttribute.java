package fs.objects;

import java.io.Serializable;
import java.nio.file.attribute.FileTime;

public class FileAttribute implements Serializable {

    private long fileLength;
    private FileTime creationTime;
    private FileTime lastModifiedTime;
    private int referenceCount;
    private String Owner;
    private String type;
    private Object acl;

    public FileAttribute(long fileLength, FileTime creationTime, FileTime lastModifiedTime, int referenceCount) {
        this.fileLength = fileLength;
        this.creationTime = creationTime;
        this.lastModifiedTime = lastModifiedTime;
        this.referenceCount = referenceCount;
    }

    public long getFileLength() {
        return fileLength;
    }

    public void setFileLength(int fileLength) {
        this.fileLength = fileLength;
    }

    public FileTime getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(FileTime creationTime) {
        this.creationTime = creationTime;
    }

    public FileTime getLastModifiedTime() {
        return lastModifiedTime;
    }

    public void setLastModifiedTime(FileTime lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    public int getReferenceCount() {
        return referenceCount;
    }

    public void setReferenceCount(int referenceCount) {
        this.referenceCount = referenceCount;
    }

    public String getOwner() {
        return Owner;
    }

    public void setOwner(String owner) {
        Owner = owner;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getAcl() {
        return acl;
    }

    public void setAcl(Object acl) {
        this.acl = acl;
    }
}