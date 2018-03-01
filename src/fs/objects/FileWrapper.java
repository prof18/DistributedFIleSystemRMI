package fs.objects;

public class FileWrapper {

    private String fileName;
    private FileAttribute attribute;
    private byte[] content; //fare checksum per verificare correttezza messaggio

    public FileWrapper(String fileName){
        this.fileName = fileName;
    }

    public String getFileName(){
        return fileName;
    }

}
