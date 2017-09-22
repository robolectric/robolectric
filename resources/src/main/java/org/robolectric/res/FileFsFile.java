package org.robolectric.res;

import com.google.common.annotations.VisibleForTesting;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.robolectric.util.Util;

public class FileFsFile implements FsFile {
  @VisibleForTesting
  static String FILE_SEPARATOR = File.separator;

  private File canonicalFile;
  private final File file;

  FileFsFile(File file) {
    this.file = file;
  }

  FileFsFile(String path) {
    this.file = new File(path);
  }

  @Override
  public boolean exists() {
    return file.exists();
  }

  @Override
  public boolean isDirectory() {
    return file.isDirectory();
  }

  @Override
  public boolean isFile() {
    return file.isFile();
  }

  @Override
  public FsFile[] listFiles() {
    return asFsFiles(file.listFiles());
  }

  @Override
  public FsFile[] listFiles(final Filter filter) {
    return asFsFiles(file.listFiles(new FileFilter() {
      @Override
      public boolean accept(File pathname) {
        return filter.accept(new FileFsFile(pathname));
      }
    }));
  }

  @Override
  public String[] listFileNames() {
    File[] files = file.listFiles();
    if (files == null) return null;
    String[] strings = new String[files.length];
    for (int i = 0; i < files.length; i++) {
      strings[i] = files[i].getName();
    }
    return strings;
  }

  @Override
  public FsFile getParent() {
    File parentFile = file.getParentFile();
    return parentFile == null ? null : Fs.newFile(parentFile);
  }

  @Override
  public String getName() {
    return file.getName();
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return new BufferedInputStream(new FileInputStream(file));
  }

  @Override
  public byte[] getBytes() throws IOException {
    return Util.readBytes(new FileInputStream(file));
  }

  @Override
  public FsFile join(String... pathParts) {
    File f = file;
    for (String pathPart : pathParts) {
      for (String part : pathPart.split(Pattern.quote(FILE_SEPARATOR))) {
        if (!part.equals(".")) {
          f = new File(f, part);
        }
      }
    }

    return Fs.newFile(f);
  }

  public File getFile() {
    return file;
  }

  @Override
  public String toString() {
    return file.getPath();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    FileFsFile fsFile = (FileFsFile) o;

    return getCanonicalFile().equals(fsFile.getCanonicalFile());
  }

  @Override
  public int hashCode() {
    return getCanonicalFile().hashCode();
  }

  @Override
  public String getBaseName() {
    String name = getName();
    int dotIndex = name.indexOf(".");
    return dotIndex >= 0 ? name.substring(0, dotIndex) : name;
  }

  @Override
  public String getPath() {
    return file.getPath();
  }

  private FsFile[] asFsFiles(File[] files) {
    if (files == null) return null;
    FsFile[] fsFiles = new FsFile[files.length];
    for (int i = 0; i < files.length; i++) {
      fsFiles[i] = Fs.newFile(files[i]);
    }
    return fsFiles;
  }

  /**
   * Canonical file queries can be expensive, so perform them lazily. In
   * practice, this should only happen for raw resources, AndroidManifest.xml,
   * and project.properties.
   */
  private synchronized File getCanonicalFile() {
    if (canonicalFile == null) {
      try {
        // Android library references in project.properties are all
        // relative paths, so using a canonical path guarantees that
        // there won't be duplicates.
        this.canonicalFile = file.getCanonicalFile();
      } catch (IOException e) {
        // In a case where file system queries are failing, it makes
        // sense for the test to fail.
        throw new RuntimeException(e);
      }
    }
    return canonicalFile;
  }

  /**
   * Construct an FileFsFile from a series of path components. Path components that are
   * null or empty string will be ignored.
   *
   * @param paths Array of path components.
   * @return New FileFsFile.
   */
  @Nonnull
  public static FileFsFile from(String... paths) {
    File file = null;
    for (String path : paths) {
      if (path != null && path.length() > 0) {
        for (String part : path.split(Pattern.quote(FILE_SEPARATOR))) {
          if (file != null && part.equals(".")) continue;
          file = (file == null)
              ? new File(part)
              : new File(file, part);
        }
      }
    }
    return new FileFsFile(file);
  }
}
