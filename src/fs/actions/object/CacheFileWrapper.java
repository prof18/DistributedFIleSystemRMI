package fs.actions.object;

import fs.objects.structure.FileAttribute;
import net.objects.NetNodeLocation;

import java.io.File;
import java.io.Serializable;
import java.time.Instant;
import java.util.Date;

public class CacheFileWrapper implements Serializable {
    private String UFID;
    private File file;
    private FileAttribute attribute;
    private Date lastValidatedTime;
    private Date lastModifiedBeforeDownloadTime;
    private boolean isLocal;


    public CacheFileWrapper(File file, FileAttribute attribute,String UFID,boolean isLocal) {
        this.UFID=UFID;
        this.file = file;
        this.attribute = attribute;
        this.isLocal=isLocal;
        lastValidatedTime = Date.from(Instant.now());
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

    public Date getLastModifiedBeforeDownloadTime() {
        return lastModifiedBeforeDownloadTime;
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

    public void setLastValidatedTime(Date lastValidatedTime) {
        this.lastValidatedTime = lastValidatedTime;
    }
}
