package fs.objects;

import java.util.Date;

public class FileAttribute {

    private int fileLength;
    private Date creationTime;
    private Date writeTime;
    private Date attributeTime;
    private int referenceCount;
    private String Owner;
    private String type;
    private Object acl;

    public FileAttribute(int fileLength, Date creationTime, Date writeTime, Date attributeTime, int referenceCount){
        this.fileLength = fileLength;
        this.creationTime = creationTime;
        this.writeTime = writeTime;
        this.attributeTime = attributeTime;
        this.referenceCount = referenceCount;
    }

    public void setFileLength(int fileLength) {
        this.fileLength = fileLength;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public void setWriteTime(Date writeTime) {
        this.writeTime = writeTime;
    }

    public void setAttributeTime(Date attributeTime) {
        this.attributeTime = attributeTime;
    }

    public void setReferenceCount(int referenceCount) {
        this.referenceCount = referenceCount;
    }

    public void setOwner(String owner) {
        Owner = owner;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setAcl(Object acl) {
        this.acl = acl;
    }

    public int getFileLength() {
        return fileLength;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public Date getWriteTime() {
        return writeTime;
    }

    public Date getAttributeTime() {
        return attributeTime;
    }

    public int getReferenceCount() {
        return referenceCount;
    }

    public String getOwner() {
        return Owner;
    }

    public String getType() {
        return type;
    }

    public Object getAcl() {
        return acl;
    }
}