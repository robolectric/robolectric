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

  public static File getNativeLibraryPath() {
    String tempPath = System.getProperty("java.io.tmpdir");
    if (tempPath == null) {
      throw new IllegalStateException("Java temporary directory is not defined (java.io.tmpdir)");
    }
    return new File(Fs.fileFromPath(tempPath).join("robolectric-libs", getLibName()).getPath());
  }

  public static void mustReload() {
    loaded = false;
  }

  public static void load() {
    if (loaded) { return; }

    long startTime = System.currentTimeMillis();

    String libName = getLibName();
    String libPath = "/" + libName;
    InputStream libraryStream = SQLiteNatives.class.getResourceAsStream(libPath);
    if (libraryStream == null) {
      throw new RuntimeException("Cannot find '" + libPath + "' in classpath");
    }

    File extractedLibPath = getNativeLibraryPath();

    if (!extractedLibPath.exists()) {
      extractAndLoad(libraryStream, extractedLibPath);
      logWithTime("SQLite natives prepared in", startTime);
      return;
    }

    try {
      String existingMd5 = md5sum(new FileInputStream(extractedLibPath));
      String actualMd5 = md5sum(libraryStream);
      if (!existingMd5.equals(actualMd5)) {
        extractAndLoad(SQLiteNatives.class.getResourceAsStream(libPath), extractedLibPath);
      } else {
        loadFrom(extractedLibPath.getParentFile());
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    logWithTime("SQLite natives prepared in", startTime);
  }

  private static void logWithTime(final String message, final long startTime) {
    System.out.println(message + " " + (System.currentTimeMillis() - startTime));
  }

  private static void log(final String message) {
    System.out.println(message);
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

    loadFrom(libPath);
  }

  private static void loadFrom(final File libPath) {
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
    String name = System.mapLibraryName("sqlite4java");
    if (name.endsWith("dylib")) {
      name = name.replace("dylib", "jnilib");
    }
    return name;
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
