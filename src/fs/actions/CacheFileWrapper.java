package fs.actions;

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
    private NetNodeLocation originLocation;

    public CacheFileWrapper(File file, FileAttribute attribute,NetNodeLocation originLocation) {
        this.file = file;
        this.attribute = attribute;
        this.originLocation=originLocation;
        lastValidatedTime = Date.from(Instant.now());
    }

    public NetNodeLocation getOriginLocation() {
        return originLocation;
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
