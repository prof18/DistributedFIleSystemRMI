package fs.actions.object;

import fs.objects.structure.FileAttribute;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.time.Instant;
import java.util.Date;

/**
 * This class is used to wrap all the information
 * related to a file inserted in a cache
 */
public class CacheFileWrapper implements Serializable {
    private String UFID;
    private File file;
    private FileAttribute attribute;
    private Date lastValidatedTime;
    private boolean isLocal;
    private byte[] content;


    public CacheFileWrapper(File file, FileAttribute attribute, String UFID, boolean isLocal) {
        this.UFID = UFID;
        this.file = file;
        this.attribute = attribute;
        this.isLocal = isLocal;
        FileInputStream fis;
        try {
            fis = new FileInputStream(file);
            content = fis.readAllBytes();
        } catch (IOException e) {
            e.printStackTrace();
        }
        lastValidatedTime = Date.from(Instant.now());
    }

    public byte[] getContent() {
        return content;
    }

    public boolean isLocal() {
        return isLocal;
    }

    public void setLocal(boolean local) {
        isLocal = local;
    }

    public String getUFID() {
        return UFID;
    }

    public File getFile() {
        return file;
    }

    public FileAttribute getAttribute() {
        return attribute;
    }

    public long getLastValidatedTime() {
        return lastValidatedTime.getTime();
    }

}
