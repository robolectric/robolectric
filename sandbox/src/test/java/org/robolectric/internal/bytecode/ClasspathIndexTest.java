package org.robolectric.internal.bytecode;

import static com.google.common.truth.Truth.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.base.Splitter;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ClasspathIndexTest {
  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Test
  public void findsTheFileUrlClassLoaderFinds() throws Exception {
    File first = jar("first.jar", null, entries("a/One.class", "first", "b/Two.class", "first"));
    File directory = temporaryFolder.newFolder("classes");
    write(new File(directory, "a/Three.class"), "directory");
    write(new File(directory, "b/Two.class"), "directory");
    File last =
        jar(
            "last.jar",
            null,
            entries("a/One.class", "last", "a/Three.class", "last", "c/Four.class", "last"));
    URL[] urls = {
      first.toURI().toURL(),
      new File(temporaryFolder.getRoot(), "missing.jar").toURI().toURL(),
      directory.toURI().toURL(),
      last.toURI().toURL()
    };

    try (ClasspathIndex index = ClasspathIndex.create(urls);
        URLClassLoader classLoader = new URLClassLoader(urls, null)) {
      for (String name :
          new String[] {
            "a/One.class", "b/Two.class", "a/Three.class", "c/Four.class", "a/Missing.class"
          }) {
        assertThat(read(index.getResourceAsStream(name)))
            .isEqualTo(read(classLoader.getResourceAsStream(name)));
      }
    }
  }

  @Test
  public void findsTheFileTheSystemClassLoaderFinds() throws Exception {
    List<URL> urls = new ArrayList<>();
    for (String entry :
        Splitter.on(File.pathSeparatorChar).split(System.getProperty("java.class.path"))) {
      urls.add(new File(entry).toURI().toURL());
    }
    ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();

    try (ClasspathIndex index = ClasspathIndex.create(urls.toArray(new URL[0]))) {
      assertThat(index).isNotNull();
      for (Class<?> onClassPath :
          new Class<?>[] {ClasspathIndexTest.class, ClasspathIndex.class, Test.class}) {
        String name = onClassPath.getName().replace('.', '/') + ".class";
        byte[] fromSystemClassLoader = readBytes(systemClassLoader.getResourceAsStream(name));
        assertThat(fromSystemClassLoader).isNotNull();
        assertThat(readBytes(index.getResourceAsStream(name))).isEqualTo(fromSystemClassLoader);
      }
      assertThat(index.getResourceAsStream("no/such/Type.class")).isNull();
    }
  }

  @Test
  public void resolvesVersionedEntriesOfMultiReleaseJars() throws Exception {
    Manifest manifest = new Manifest();
    manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
    manifest.getMainAttributes().put(Attributes.Name.MULTI_RELEASE, "true");
    File jar =
        jar(
            "versioned.jar",
            manifest,
            entries(
                "a/One.class", "base",
                "META-INF/versions/9/a/One.class", "versioned",
                "META-INF/versions/9/d/OnlyVersioned.class", "versioned"));
    URL[] urls = {jar.toURI().toURL()};

    try (ClasspathIndex index = ClasspathIndex.create(urls);
        URLClassLoader classLoader = new URLClassLoader(urls, null)) {
      for (String name : new String[] {"a/One.class", "d/OnlyVersioned.class"}) {
        assertThat(read(index.getResourceAsStream(name))).isEqualTo("versioned");
        assertThat(read(classLoader.getResourceAsStream(name))).isEqualTo("versioned");
      }
    }
  }

  @Test
  public void doesNotIndexAClassPathThatAJarExtends() throws Exception {
    Manifest manifest = new Manifest();
    manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
    manifest.getMainAttributes().put(Attributes.Name.CLASS_PATH, "other.jar");
    File jar = jar("pathing.jar", manifest, entries("a/One.class", "one"));

    assertThat(ClasspathIndex.create(new URL[] {jar.toURI().toURL()})).isNull();
  }

  @Test
  public void indexesAJarWhoseManifestHasSectionsForItsEntries() throws Exception {
    Manifest manifest = new Manifest();
    manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
    Attributes entryAttributes = new Attributes();
    entryAttributes.putValue("SHA-256-Digest", "not a class path");
    manifest.getEntries().put("a/One.class", entryAttributes);
    File jar = jar("signed.jar", manifest, entries("a/One.class", "one"));

    try (ClasspathIndex index = ClasspathIndex.create(new URL[] {jar.toURI().toURL()})) {
      assertThat(read(index.getResourceAsStream("a/One.class"))).isEqualTo("one");
    }
  }

  @Test
  public void skipsAJarThatCannotBeOpened() throws Exception {
    File broken = temporaryFolder.newFile("broken.jar");
    write(broken, "not a jar");
    File jar = jar("good.jar", null, entries("a/One.class", "one"));

    try (ClasspathIndex index =
        ClasspathIndex.create(new URL[] {broken.toURI().toURL(), jar.toURI().toURL()})) {
      assertThat(read(index.getResourceAsStream("a/One.class"))).isEqualTo("one");
    }
  }

  @Test
  public void doesNotIndexRemoteEntries() throws Exception {
    assertThat(ClasspathIndex.create(new URL[] {new URL("http://localhost/some.jar")})).isNull();
  }

  private static Map<String, String> entries(String... namesAndContents) {
    Map<String, String> entries = new LinkedHashMap<>();
    for (int i = 0; i < namesAndContents.length; i += 2) {
      entries.put(namesAndContents[i], namesAndContents[i + 1]);
    }
    return entries;
  }

  private File jar(String name, Manifest manifest, Map<String, String> entries) throws Exception {
    File file = temporaryFolder.newFile(name);
    try (OutputStream out = Files.newOutputStream(file.toPath());
        JarOutputStream jar =
            manifest == null ? new JarOutputStream(out) : new JarOutputStream(out, manifest)) {
      for (Map.Entry<String, String> entry : entries.entrySet()) {
        jar.putNextEntry(new JarEntry(entry.getKey()));
        jar.write(entry.getValue().getBytes(UTF_8));
        jar.closeEntry();
      }
    }
    return file;
  }

  private static void write(File file, String content) throws Exception {
    file.getParentFile().mkdirs();
    Files.write(file.toPath(), content.getBytes(UTF_8));
  }

  private static String read(InputStream in) throws Exception {
    byte[] bytes = readBytes(in);
    return bytes == null ? null : new String(bytes, UTF_8);
  }

  private static byte[] readBytes(InputStream in) throws Exception {
    if (in == null) {
      return null;
    }
    try (InputStream stream = in) {
      return stream.readAllBytes();
    }
  }
}
