package org.robolectric.res;

import org.robolectric.util.Util;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FsFile {
    private File file;

    public FsFile(File file) {
        this.file = file;
    }

    public boolean exists() {
        return file.exists();
    }

    public FsFile[] listFiles(FileFilter fileFilter) {
        File[] files = file.listFiles(fileFilter);
        FsFile[] fsFiles = new FsFile[files.length];
        for (int i = 0; i < files.length; i++) {
            fsFiles[i] = new FsFile(files[i]);
        }
        return fsFiles;
    }

    public FsFile getParentFile() {
        File parentFile = file.getParentFile();
        return parentFile == null ? null : new FsFile(parentFile);
    }

    public String getName() {
        return file.getName();
    }

    public InputStream getInputStream() throws IOException {
        return new FileInputStream(file);
    }

    public byte[] getBytes() throws IOException {
        return Util.readBytes(new FileInputStream(file));
    }

    public FsFile join(String path) {
        return new FsFile(new File(file, path));
    }
}
