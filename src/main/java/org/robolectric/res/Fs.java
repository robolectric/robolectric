package org.robolectric.res;

import java.io.File;
import java.io.FileFilter;

public class Fs {
    private final FsFile baseDir;

    public Fs(File baseDir) {
        this.baseDir = new FsFile(baseDir);
    }

    public FsFile[] listFiles(FileFilter fileFilter) {
        return baseDir.listFiles(fileFilter);
    }

    public FsFile join(String folderBaseName) {
        return baseDir.join(folderBaseName);
    }
}
