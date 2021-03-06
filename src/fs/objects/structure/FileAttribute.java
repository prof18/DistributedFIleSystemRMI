package fs.objects.structure;

import java.io.Serializable;
import java.util.Date;

@SuppressWarnings("ALL")
public class FileAttribute implements Serializable {

    private long fileLength;
    private Date creationTime;
    private Date lastModifiedTime;
    private int referenceCount;
    private String Owner;
    private String type;
    private Object acl;

    public FileAttribute() {

    }

    public FileAttribute(long fileLength, Date creationTime, Date lastModifiedTime, int referenceCount) {
        this.fileLength = fileLength;
        this.creationTime = creationTime;
        this.lastModifiedTime = lastModifiedTime;
        this.referenceCount = referenceCount;
    }

    public long getFileLength() {
        return fileLength;
    }

    public void setFileLength(long fileLength) {
        this.fileLength = fileLength;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public Date getLastModifiedTime() {
        return lastModifiedTime;
    }

    public void setLastModifiedTime(Date lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    @SuppressWarnings("unused")
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

    public void setAcl(Object acl) {
        this.acl = acl;
    }

    @Override
    public String toString() {
        return "FileAttribute{" +
                "fileLength=" + fileLength +
                ", creationTime=" + creationTime +
                ", lastModifiedTime=" + lastModifiedTime +
                ", referenceCount=" + referenceCount +
                ", Owner='" + Owner + '\'' +
                ", type='" + type + '\'' +
                ", acl=" + acl +
                '}';
    }
}