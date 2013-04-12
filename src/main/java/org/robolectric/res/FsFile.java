package org.robolectric.res;

import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;

public interface FsFile {
    boolean exists();

    boolean isDirectory();

    boolean isFile();

    FsFile[] listFiles();

    FsFile[] listFiles(FileFilter fileFilter);

    String[] listFileNames();

    FsFile getParent();

    String getName();

    InputStream getInputStream() throws IOException;

    byte[] getBytes() throws IOException;

    FsFile join(String... pathParts);

    @Override String toString();

    @Override boolean equals(Object o);

    @Override int hashCode();

    String getBaseName();
}
