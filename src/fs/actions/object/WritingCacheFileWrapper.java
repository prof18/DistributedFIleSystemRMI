package fs.actions.object;

import fs.objects.structure.FileAttribute;
import net.objects.NetNodeLocation;

import java.io.File;
import java.util.Date;

public class WritingCacheFileWrapper extends CacheFileWrapper {
    private Date lastModifiedBeforeDownload;


    public WritingCacheFileWrapper(File file, FileAttribute attribute, Date lastModifiedBeforeDownload, String UFID, boolean isLocal) {
        super(file, attribute, UFID, isLocal);
        this.lastModifiedBeforeDownload = lastModifiedBeforeDownload;
        ;
    }

    public long getLastModifiedBeforeDownload() {
        return lastModifiedBeforeDownload.getTime();
    }
}
