package org.robolectric.internal.bytecode;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.annotation.Nullable;

/**
 * Finds a resource on a class path of jars and directories by asking only the jars that contain the
 * resource's package.
 *
 * <p>A class loader asks every class path entry in turn until one has the resource, so each lookup
 * costs time proportional to the length of the class path. This index records, once, which jars
 * contain which packages. A lookup then asks those jars, in class path order, and so finds the same
 * entry the class loader would.
 *
 * <p>The index describes the class path as it was when the index was created.
 */
final class ClasspathIndex implements Closeable {
  private static final String JAR_INDEX_NAME = "META-INF/INDEX.LIST";
  private static final int[] NO_JARS = new int[0];

  // The class path entries, in class path order. A directory has no JarFile.
  private final File[] files;
  private final JarFile[] jars;
  // The positions of the directories in the class path, in ascending order.
  private final int[] directories;
  // The directory part of an entry name, without a trailing slash, to the positions of the jars
  // that have entries there, in ascending order.
  private final Map<String, int[]> jarsByPackage;

  private ClasspathIndex(
      File[] files, JarFile[] jars, int[] directories, Map<String, int[]> jarsByPackage) {
    this.files = files;
    this.jars = jars;
    this.directories = directories;
    this.jarsByPackage = jarsByPackage;
  }

  /**
   * Indexes the class path, or returns null if it has an entry whose contents this index cannot
   * reproduce: one that is not a local file, or a jar that adds further entries to the class path.
   */
  @Nullable
  static ClasspathIndex create(URL[] urls) {
    List<File> files = new ArrayList<>(urls.length);
    List<JarFile> jars = new ArrayList<>(urls.length);
    List<Integer> directories = new ArrayList<>();
    Map<String, List<Integer>> jarsByPackage = new HashMap<>();
    boolean complete = false;
    try {
      for (URL url : urls) {
        if (!"file".equals(url.getProtocol())) {
          return null;
        }
        File file = new File(url.toURI());
        Integer position = files.size();
        if (file.isDirectory()) {
          directories.add(position);
          files.add(file);
          jars.add(null);
        } else if (file.isFile()) {
          // Opened the way a class loader opens it, so that the versioned entries of a
          // multi-release jar resolve the same way.
          JarFile jar;
          try {
            jar = new JarFile(file, true, ZipFile.OPEN_READ, JarFile.runtimeVersion());
          } catch (IOException e) {
            // The system class loader skips a jar it cannot open, so it contributes nothing here.
            continue;
          }
          files.add(file);
          jars.add(jar);
          if (addsClassPathEntries(file)) {
            return null;
          }
          addPackages(jar, position, jarsByPackage);
        }
        // An entry that does not exist contributes nothing, as on a real class path.
      }
      complete = true;
    } catch (IOException | URISyntaxException | IllegalArgumentException e) {
      return null;
    } finally {
      if (!complete) {
        closeAll(jars);
      }
    }

    Map<String, int[]> positionsByPackage = new HashMap<>(jarsByPackage.size() * 4 / 3 + 1);
    for (Map.Entry<String, List<Integer>> entry : jarsByPackage.entrySet()) {
      positionsByPackage.put(entry.getKey(), toIntArray(entry.getValue()));
    }
    return new ClasspathIndex(
        files.toArray(new File[0]),
        jars.toArray(new JarFile[0]),
        toIntArray(directories),
        positionsByPackage);
  }

  /** Opens the first class path entry's copy of a file, or returns null if no entry has it. */
  @Nullable
  InputStream getResourceAsStream(String name) throws IOException {
    int nextDirectory = 0;
    for (int jarPosition : jarsByPackage.getOrDefault(packageOf(name), NO_JARS)) {
      // Directories that come before this jar on the class path are asked first.
      for (;
          nextDirectory < directories.length && directories[nextDirectory] < jarPosition;
          nextDirectory++) {
        InputStream fromDirectory = openFile(files[directories[nextDirectory]], name);
        if (fromDirectory != null) {
          return fromDirectory;
        }
      }
      JarFile jar = jars[jarPosition];
      JarEntry entry = jar.getJarEntry(name);
      if (entry != null && !entry.isDirectory()) {
        return jar.getInputStream(entry);
      }
    }
    for (; nextDirectory < directories.length; nextDirectory++) {
      InputStream fromDirectory = openFile(files[directories[nextDirectory]], name);
      if (fromDirectory != null) {
        return fromDirectory;
      }
    }
    return null;
  }

  @Override
  public void close() {
    closeAll(Arrays.asList(jars));
  }

  private static void addPackages(
      JarFile jar, Integer position, Map<String, List<Integer>> jarsByPackage) {
    String[] previousPackage = {null};
    jar.versionedStream()
        .forEach(entry -> {
          String name = entry.getName();
          if (name.endsWith("/")) {
            return;
          }
          String packageName = packageOf(name);
          // Entries of one package are usually adjacent.
          if (packageName.equals(previousPackage[0])) {
            return;
          }
          previousPackage[0] = packageName;
          List<Integer> positions =
              jarsByPackage.computeIfAbsent(packageName, k -> new ArrayList<>(1));
          if (positions.isEmpty() || !positions.get(positions.size() - 1).equals(position)) {
            positions.add(position);
          }
        });
  }

  /** The directory part of an entry name. An entry in the root has the package "". */
  private static String packageOf(String name) {
    int lastSlash = name.lastIndexOf('/');
    return lastSlash < 0 ? "" : name.substring(0, lastSlash);
  }

  @Nullable
  private static InputStream openFile(File directory, String name) throws IOException {
    File file = new File(directory, name);
    return file.isFile() ? Files.newInputStream(file.toPath()) : null;
  }

  /**
   * Whether a class loader that reads this jar goes on to search jars that the jar names, in the
   * Class-Path attribute of its manifest or in a jar index.
   */
  private static boolean addsClassPathEntries(File file) throws IOException {
    // Read through a plain ZipFile, because reading the manifest through the JarFile would set up
    // signature verification for every signed jar on the class path, used or not.
    try (ZipFile zip = new ZipFile(file)) {
      // Java 21 and later ignore jar indexes.
      if (Runtime.version().feature() < 21 && zip.getEntry(JAR_INDEX_NAME) != null) {
        return true;
      }
      ZipEntry manifestEntry = zip.getEntry(JarFile.MANIFEST_NAME);
      if (manifestEntry == null) {
        return false;
      }
      try (InputStream in = zip.getInputStream(manifestEntry)) {
        Manifest mainSection = new Manifest(new ByteArrayInputStream(readMainSection(in)));
        return mainSection.getMainAttributes().getValue(Attributes.Name.CLASS_PATH) != null;
      }
    }
  }

  /**
   * Reads a manifest up to the blank line that ends its main section. The sections after it list
   * every entry of a signed jar.
   */
  private static byte[] readMainSection(InputStream manifest) throws IOException {
    ByteArrayOutputStream mainSection = new ByteArrayOutputStream(512);
    byte[] buffer = new byte[512];
    boolean lineIsBlank = false;
    int count;
    while ((count = manifest.read(buffer)) > 0) {
      for (int i = 0; i < count; i++) {
        byte b = buffer[i];
        if (b == '\n') {
          if (lineIsBlank) {
            mainSection.write(buffer, 0, i + 1);
            return mainSection.toByteArray();
          }
          lineIsBlank = true;
        } else if (b != '\r') {
          lineIsBlank = false;
        }
      }
      mainSection.write(buffer, 0, count);
    }
    return mainSection.toByteArray();
  }

  private static int[] toIntArray(List<Integer> values) {
    int[] array = new int[values.size()];
    for (int i = 0; i < array.length; i++) {
      array[i] = values.get(i);
    }
    return array;
  }

  private static void closeAll(List<JarFile> jars) {
    for (JarFile jar : jars) {
      if (jar != null) {
        try {
          jar.close();
        } catch (IOException e) {
          // Closing is best effort.
        }
      }
    }
  }
}
