package fs.actions.object;

/**
 * This class is used to keep all the information to send to the MainUI
 * about the reading of a specific file
 */
public class ReadWrapper {
    private byte[] content;
    private boolean writable;

    public ReadWrapper(byte[] content, boolean writable) {
        this.content = content;
        this.writable = writable;
    }

    public byte[] getContent() {
        return content;
    }

    public boolean isWritable() {
        return writable;
    }
}

