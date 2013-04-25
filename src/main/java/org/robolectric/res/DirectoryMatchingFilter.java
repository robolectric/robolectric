package org.robolectric.res;

import java.io.File;

public class DirectoryMatchingFilter implements FsFile.Filter {
    private final String folderBaseName;

    public DirectoryMatchingFilter(String folderBaseName) {
        this.folderBaseName = folderBaseName;
    }

    @Override
    public boolean accept(FsFile file) {
        return file.getPath().contains(File.separator + folderBaseName);
    }
}
