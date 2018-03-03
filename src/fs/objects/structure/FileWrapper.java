package fs.objects.structure;

public class FileWrapper {

    private String fileName;
    private String UFID;
    private FileAttribute attribute;
    private byte[] content; //fare checksum per verificare correttezza messaggio

    public FileWrapper() {
    public FileWrapper(String UFID, String fileName){
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
    }
}
