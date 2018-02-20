import java.util.Date;

public class FileAttribute {

    private int fileLength;
    private Date creationTime;
    private Date writeTime;
    private Date attributeTime;
    private int referenceCount;
    private String owner;
    private String type;
    private Object acl;

    public FileAttribute(int fileLength, Date creationTime, Date writeTime, Date attributeTime, int referenceCount) {
        this.fileLength = fileLength;
        this.creationTime = creationTime;
        this.writeTime = writeTime;
        this.attributeTime = attributeTime;
        this.referenceCount = referenceCount;
    }

    public int getFileLength() {
        return fileLength;
    }

    public void setFileLength(int fileLength) {
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
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
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
