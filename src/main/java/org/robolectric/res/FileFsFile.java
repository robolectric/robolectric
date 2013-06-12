package org.robolectric.res;

import org.robolectric.util.Util;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileFsFile implements FsFile {
  private File file;

  FileFsFile(File file) {
    this.file = file;
  }

  @Override public boolean exists() {
    return file.exists();
  }

  @Override public boolean isDirectory() {
    return file.isDirectory();
  }

  @Override public boolean isFile() {
    return file.isFile();
  }

  @Override public FsFile[] listFiles() {
    return asFsFiles(file.listFiles());
  }

  @Override public FsFile[] listFiles(final Filter filter) {
    return asFsFiles(file.listFiles(new FileFilter() {
      @Override public boolean accept(File pathname) {
        return filter.accept(new FileFsFile(pathname));
      }
    }));
  }

  @Override public String[] listFileNames() {
    File[] files = file.listFiles();
    if (files == null) return null;
    String[] strings = new String[files.length];
    for (int i = 0; i < files.length; i++) {
      strings[i] = files[i].getName();
    }
    return strings;
  }

  @Override public FsFile getParent() {
    File parentFile = file.getParentFile();
    return parentFile == null ? null : Fs.newFile(parentFile);
  }

  @Override public String getName() {
    return file.getName();
  }

  @Override public InputStream getInputStream() throws IOException {
    return new FileInputStream(file);
  }

  @Override public byte[] getBytes() throws IOException {
    return Util.readBytes(new FileInputStream(file));
  }

  @Override public FsFile join(String... pathParts) {
    File f = file;
    for (String pathPart : pathParts) {
      f = new File(f, pathPart);
    }

    return Fs.newFile(f);
  }

  public File getFile() {
    return file;
  }

  @Override public String toString() {
    return file.getPath();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    FileFsFile fsFile = (FileFsFile) o;

    if (!file.equals(fsFile.file)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return file.hashCode();
  }

  private FsFile[] asFsFiles(File[] files) {
    if (files == null) return null;
    FsFile[] fsFiles = new FsFile[files.length];
    for (int i = 0; i < files.length; i++) {
      fsFiles[i] = Fs.newFile(files[i]);
    }
    return fsFiles;
  }

  @Override public String getBaseName() {
    String name = getName();
    int dotIndex = name.indexOf(".");
    return dotIndex >= 0 ? name.substring(0, dotIndex) : name;
  }

  @Override public String getPath() {
    return file.getPath();
  }
}
