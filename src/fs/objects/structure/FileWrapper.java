package fs.objects.structure;

public class FileWrapper {

    private String UFID;
    private String fileName;
    private FileAttribute attribute;
    private byte[] content; //fare checksum per verificare correttezza messaggio

    public FileWrapper(String UFID, String fileName){
        this.UFID = UFID;
        this.fileName = fileName;
    }

    public String getFileName(){
        return fileName;
    }

    public String getUFID(){
        return UFID;
    }

    public void setFileName(String name){
        fileName = name;
    }

}
