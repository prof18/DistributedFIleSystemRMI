package fs.actions.object;

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

