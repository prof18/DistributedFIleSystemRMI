package fs.objects;

import java.time.Instant;
import java.util.Date;

public class FileAttribute {

    //TODO: decide the format (b,kb,etc)
    private long fileLength;
    private Date creationTime;
    private Date writeTime;
    private Date attributeTime;
    private int referenceCount;
    private String Owner;
    private String type;
    private Object acl;

    public FileAttribute(){
       // new FileAttribute(0, Date.from(Instant.EPOCH), Date.from(Instant.EPOCH), Date.from(Instant.EPOCH), 0);
    }

    public Long getFileLength() {
        return fileLength;
    }

    public void setFileLength(Long fileLength) {
        this.fileLength = fileLength;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public Date getWriteTime() {
        return writeTime;
    }

    public void setWriteTime(Date writeTime) {
        this.writeTime = writeTime;
    }

    public Date getAttributeTime() {
        return attributeTime;
    }

    public void setAttributeTime(Date attributeTime) {
        this.attributeTime = attributeTime;
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