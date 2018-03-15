package fs.actions.object;

import fs.objects.structure.FileAttribute;
import net.objects.NetNodeLocation;

import java.io.File;
import java.io.Serializable;
import java.time.Instant;
import java.util.Date;

public class CacheFileWrapper implements Serializable {
    private File file;
    private FileAttribute attribute;
    private Date lastValidatedTime;
    private Date lastModifiedBeforeDownloadTime;


    public CacheFileWrapper(File file, FileAttribute attribute) {
        this.file = file;
        this.attribute = attribute;
        lastValidatedTime = Date.from(Instant.now());
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
