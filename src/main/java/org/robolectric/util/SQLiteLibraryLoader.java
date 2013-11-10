package org.robolectric.util;

import com.almworks.sqlite4java.SQLite;
import com.almworks.sqlite4java.SQLiteException;
import com.github.axet.litedb.SQLiteNatives;
import org.apache.commons.io.IOUtils;
import org.robolectric.res.Fs;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Initializes sqlite native libraries.
 */
public class SQLiteLibraryLoader {
  private static boolean loaded;

  private SQLiteLibraryLoader() { }

  public static void load() {
    if (loaded) { return; }

    final long startTime = System.currentTimeMillis();
    final File extractedLibrary = getNativeLibraryPath();

    if (isExtractedLibUptodate(extractedLibrary)) {
      loadFromDirectory(extractedLibrary.getParentFile());
    } else {
      extractAndLoad(getLibraryStream(), extractedLibrary);
    }

    logWithTime("SQLite natives prepared in", startTime);
  }

  protected static File getNativeLibraryPath() {
    String tempPath = System.getProperty("java.io.tmpdir");
    if (tempPath == null) {
      throw new IllegalStateException("Java temporary directory is not defined (java.io.tmpdir)");
    }
    return new File(Fs.fileFromPath(tempPath).join("robolectric-libs", getLibName()).getPath());
  }

  protected static void mustReload() {
    loaded = false;
  }

  private static void logWithTime(final String message, final long startTime) {
    log(message + " " + (System.currentTimeMillis() - startTime));
  }

  private static void log(final String message) {
    System.out.println(message);
  }

  private static boolean isExtractedLibUptodate(File extractedLib) {
    if (extractedLib.exists()) {
      try {
        String existingMd5 = md5sum(new FileInputStream(extractedLib));
        String actualMd5 = md5sum(getLibraryStream());
        return existingMd5.equals(actualMd5);
      } catch (IOException e) {
        return false;
      }
    } else {
      return false;
    }
  }

  private static InputStream getLibraryStream() {
    String classpathResourceName = getLibClasspathResourceName();
    InputStream libraryStream = SQLiteNatives.class.getResourceAsStream(classpathResourceName);
    if (libraryStream == null) {
      throw new RuntimeException("Cannot find '" + classpathResourceName + "' in classpath");
    }
    return libraryStream;
  }

  private static String getLibClasspathResourceName() {
    String libName = getLibName();
    if (libName.endsWith("dylib")) {
      // for some reason the osx version is packaged as .jnilib
      libName = libName.replace("dylib", "jnilib");
    }
    return "/" + libName;
  }

  private static void extractAndLoad(final InputStream input, final File output) {
    File libPath = output.getParentFile();
    libPath.mkdirs();

    FileOutputStream outputStream = null;
    try {
      outputStream = new FileOutputStream(output);
      IOUtils.copy(input, outputStream);
    } catch (IOException e) {
      throw new RuntimeException("Cannot extractAndLoad SQLite library into " + output, e);
    } finally {
      IOUtils.closeQuietly(outputStream);
      IOUtils.closeQuietly(input);
    }

    loadFromDirectory(libPath);
  }

  private static void loadFromDirectory(final File libPath) {
    // configure less verbose logging
    Logger.getLogger("com.almworks.sqlite4java").setLevel(Level.WARNING);

    SQLite.setLibraryPath(libPath.getAbsolutePath());
    try {
      log("SQLite version: library " + SQLite.getLibraryVersion() + " / core " + SQLite.getSQLiteVersion());
    } catch (SQLiteException e) {
      throw new RuntimeException(e);
    }
    loaded = true;
  }

  private static String getLibName() {
    return System.mapLibraryName("sqlite4java");
  }

  private static String md5sum(InputStream input) throws IOException {
    BufferedInputStream in = new BufferedInputStream(input);

    try {
      MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
      DigestInputStream digestInputStream = new DigestInputStream(in, digest);
      while (digestInputStream.read() >= 0);
      ByteArrayOutputStream md5out = new ByteArrayOutputStream();
      md5out.write(digest.digest());
      return new BigInteger(md5out.toByteArray()).toString();
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("MD5 algorithm is not available: " + e);
    }
    finally {
      in.close();
    }
  }

}
