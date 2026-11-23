package org.robolectric.internal.bytecode;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ClasspathIndexTest {
  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private Path root;
  private final List<ClasspathIndex> acquired = new ArrayList<>();

  @Before
  public void setUp() throws IOException {
    // Canonical, so URLs compare equal however the temporary directory is reached.
    root = temporaryFolder.getRoot().toPath().toRealPath();
    ClasspathIndex.clearShared();
    System.setProperty(ClasspathIndex.PROPERTY, "true");
  }

  @After
  public void tearDown() {
    for (ClasspathIndex index : acquired) {
      index.release();
    }
    ClasspathIndex.clearShared();
    System.clearProperty(ClasspathIndex.PROPERTY);
  }

  @Test
  public void findResource_matchesUrlClassLoader_firstJarInOrderWins() throws Exception {
    URL[] urls = {
      jar("empty.jar", ImmutableMap.of()),
      jar("a.jar", ImmutableMap.of("p/x.txt", "a", "p/only-a.txt", "a")),
      jar("b.jar", ImmutableMap.of("p/x.txt", "b", "q/y.txt", "b")),
    };

    assertMatchesUrlClassLoader(
        urls, "p/x.txt", "p/only-a.txt", "q/y.txt", "p/missing.txt", "r/missing.txt", "p", "p/");
  }

  @Test
  public void findResources_matchesUrlClassLoader() throws Exception {
    URL[] urls = {
      jar("a.jar", ImmutableMap.of("META-INF/services/S", "a")),
      jar("b.jar", ImmutableMap.of("META-INF/other", "b")),
      jar("c.jar", ImmutableMap.of("META-INF/services/S", "c")),
    };

    assertMatchesUrlClassLoader(urls, "META-INF/services/S", "META-INF/services/T", "META-INF");
  }

  @Test
  public void directories_areSearchedLive_inClassPathOrder() throws Exception {
    Path dir = Files.createDirectories(root.resolve("classes"));
    URL[] urls = {jar("a.jar", ImmutableMap.of("p/in-jar.txt", "a")), dir.toUri().toURL()};
    ClasspathIndex index = acquire(urls);
    assertThat(index.findResource("p/late.txt")).isNull();

    // Created after the index was built; a directory is never indexed, so it is still found.
    Files.createDirectories(dir.resolve("p"));
    Files.write(dir.resolve("p/late.txt"), "late".getBytes(UTF_8));

    assertMatchesUrlClassLoader(urls, "p/late.txt", "p/in-jar.txt");
  }

  @Test
  public void missingAndUnreadableJars_areSkipped() throws Exception {
    Path notAJar = root.resolve("not-a.jar");
    Files.write(notAJar, "garbage".getBytes(UTF_8));
    URL[] urls = {
      root.resolve("does-not-exist.jar").toUri().toURL(),
      notAJar.toUri().toURL(),
      jar("a.jar", ImmutableMap.of("p/x.txt", "a")),
    };

    assertMatchesUrlClassLoader(urls, "p/x.txt", "p/missing.txt");
  }

  @Test
  public void classPathManifestAttribute_isExpandedWhereUrlClassLoaderExpandsIt() throws Exception {
    URL b = jar("b.jar", ImmutableMap.of("x/y.txt", "b"));
    URL a = jar("a.jar", ImmutableMap.of("a.txt", "a"), ImmutableMap.of("Class-Path", "b.jar"));
    URL c = jar("c.jar", ImmutableMap.of("x/y.txt", "c"));

    // a.jar's Class-Path puts b.jar ahead of c.jar, and b.jar's own appearance is then skipped.
    assertMatchesUrlClassLoader(new URL[] {a, c, b}, "x/y.txt", "a.txt");
  }

  @Test
  public void repeatedEntries_areSkipped() throws Exception {
    URL a = jar("a.jar", ImmutableMap.of("x.txt", "a"));
    URL b = jar("b.jar", ImmutableMap.of("x.txt", "b"));

    assertMatchesUrlClassLoader(new URL[] {a, b, a}, "x.txt");
  }

  @Test
  public void multiReleaseJar_answersWithVersionedEntries() throws Exception {
    URL[] urls = {
      jar(
          "mr.jar",
          ImmutableMap.of("META-INF/versions/9/v/only-versioned.txt", "9", "v/base.txt", "base"),
          ImmutableMap.of("Multi-Release", "true")),
    };

    assertMatchesUrlClassLoader(urls, "v/only-versioned.txt", "v/base.txt");
  }

  @Test
  public void nonFileUrls_areNotIndexed() throws Exception {
    URL[] urls = {jar("a.jar", ImmutableMap.of()), new URL("http://example.com/b.jar")};

    assertThat(ClasspathIndex.acquire(urls)).isNull();
  }

  @Test
  public void property_offByDefault() throws Exception {
    URL[] urls = {jar("a.jar", ImmutableMap.of("x.txt", "a"))};
    System.clearProperty(ClasspathIndex.PROPERTY);

    assertThat(ClasspathIndex.acquire(urls)).isNull();
  }

  @Test
  public void property_onAndOff() throws Exception {
    URL[] urls = {jar("a.jar", ImmutableMap.of("x.txt", "a"))};

    for (String off : new String[] {"false", "off", "OFF", " false ", ""}) {
      System.setProperty(ClasspathIndex.PROPERTY, off);
      assertWithMessage("'%s'", off).that(ClasspathIndex.isEnabledFor(urls)).isFalse();
    }
    for (String on : new String[] {"true", "on", "TRUE", " on "}) {
      System.setProperty(ClasspathIndex.PROPERTY, on);
      assertWithMessage("'%s'", on).that(ClasspathIndex.isEnabledFor(urls)).isTrue();
    }
  }

  @Test
  public void property_number_enablesFromThatManyJars_notCountingDirectories() throws Exception {
    Path dir = Files.createDirectories(root.resolve("classes"));
    URL[] twoJarsAndADirectory = {
      jar("a.jar", ImmutableMap.of("x.txt", "a")),
      dir.toUri().toURL(),
      jar("b.jar", ImmutableMap.of()),
    };

    System.setProperty(ClasspathIndex.PROPERTY, "3");
    assertThat(ClasspathIndex.isEnabledFor(twoJarsAndADirectory)).isFalse();
    assertThat(ClasspathIndex.acquire(twoJarsAndADirectory)).isNull();

    System.setProperty(ClasspathIndex.PROPERTY, "2");
    assertThat(ClasspathIndex.isEnabledFor(twoJarsAndADirectory)).isTrue();
    assertThat(read(acquire(twoJarsAndADirectory).findResource("x.txt"))).isEqualTo("a");

    System.setProperty(ClasspathIndex.PROPERTY, "0");
    assertThat(ClasspathIndex.isEnabledFor(new URL[0])).isTrue();
  }

  @Test
  public void property_unparseable_isOff() throws Exception {
    URL[] urls = {jar("a.jar", ImmutableMap.of("x.txt", "a"))};
    System.setProperty(ClasspathIndex.PROPERTY, "lots");

    assertThat(ClasspathIndex.acquire(urls)).isNull();
  }

  @Test
  public void acquire_sharesOneIndexPerClassPath() throws Exception {
    URL[] urls = {jar("a.jar", ImmutableMap.of("x.txt", "a"))};
    // Sandboxes other tests left open in this JVM hold indexes of their own.
    int before = ClasspathIndex.sharedIndexCount();

    ClasspathIndex first = acquire(urls);
    ClasspathIndex second = acquire(urls.clone());
    ClasspathIndex other = acquire(new URL[] {jar("b.jar", ImmutableMap.of())});

    assertThat(second).isSameInstanceAs(first);
    assertThat(other).isNotSameInstanceAs(first);
    assertThat(ClasspathIndex.sharedIndexCount()).isEqualTo(before + 2);
  }

  @Test
  public void lastRelease_closesJars_andALaterAcquireReopensThem() throws Exception {
    URL[] urls = {jar("a.jar", ImmutableMap.of("x.txt", "a"))};
    ClasspathIndex first = ClasspathIndex.acquire(urls);
    ClasspathIndex second = ClasspathIndex.acquire(urls);
    assertThat(first.openJarCount()).isEqualTo(1);

    first.release();
    assertThat(first.openJarCount()).isEqualTo(1);
    second.release();
    assertThat(first.openJarCount()).isEqualTo(0);
    assertThat(first.findResource("x.txt")).isNull();

    // Nothing changed on disk, so the index is reused, and reopens jars as they are probed.
    ClasspathIndex again = acquire(urls);
    assertThat(again).isSameInstanceAs(first);
    assertThat(again.openJarCount()).isEqualTo(0);
    assertThat(read(again.findResource("x.txt"))).isEqualTo("a");
    assertThat(again.openJarCount()).isEqualTo(1);
  }

  @Test
  public void reacquire_afterAJarChanges_rebuilds() throws Exception {
    URL[] urls = {jar("a.jar", ImmutableMap.of("x.txt", "a"))};
    ClasspathIndex first = ClasspathIndex.acquire(urls);
    first.release();

    Path jar = root.resolve("a.jar");
    jar("a.jar", ImmutableMap.of("y/z.txt", "new"));
    Files.setLastModifiedTime(
        jar, FileTime.fromMillis(Files.getLastModifiedTime(jar).toMillis() + 10_000));

    ClasspathIndex rebuilt = acquire(urls);
    assertThat(rebuilt).isNotSameInstanceAs(first);
    assertThat(rebuilt.findResource("x.txt")).isNull();
    assertThat(read(rebuilt.findResource("y/z.txt"))).isEqualTo("new");
  }

  @Test
  public void reacquire_afterASkippedJarAppears_rebuilds() throws Exception {
    URL missing = root.resolve("later.jar").toUri().toURL();
    URL[] urls = {missing};
    ClasspathIndex first = ClasspathIndex.acquire(urls);
    assertThat(first.findResource("x.txt")).isNull();
    first.release();

    jar("later.jar", ImmutableMap.of("x.txt", "later"));

    ClasspathIndex rebuilt = acquire(urls);
    assertThat(rebuilt).isNotSameInstanceAs(first);
    assertThat(read(rebuilt.findResource("x.txt"))).isEqualTo("later");
  }

  @Test
  public void verifiedLookup_returnsTheLoadersAnswer_andStopsAnswering_onDisagreement()
      throws Exception {
    URL[] urls = {jar("a.jar", ImmutableMap.of("x.txt", "a"))};
    URL elsewhere = jar("elsewhere.jar", ImmutableMap.of("x.txt", "elsewhere"));
    ClasspathIndex index = acquire(urls);

    try (URLClassLoader agreeing = new URLClassLoader(urls, null)) {
      assertThat(index.findResource("x.txt", agreeing)).isEqualTo(agreeing.getResource("x.txt"));
      assertThat(index.isMismatched()).isFalse();
    }

    ClasspathIndex other = acquire(new URL[] {jar("b.jar", ImmutableMap.of("x.txt", "b"))});
    try (URLClassLoader disagreeing = new URLClassLoader(new URL[] {elsewhere}, null)) {
      URL url = other.findResource("x.txt", disagreeing);
      assertThat(read(url)).isEqualTo("elsewhere");
      assertThat(other.isMismatched()).isTrue();
    }
  }

  @Test
  public void keyOf() {
    assertThat(ClasspathIndex.keyOf("a/b/C.class")).isEqualTo("a/b");
    assertThat(ClasspathIndex.keyOf("a/b/")).isEqualTo("a");
    assertThat(ClasspathIndex.keyOf("a/b")).isEqualTo("a");
    assertThat(ClasspathIndex.keyOf("top.txt")).isEqualTo("");
    assertThat(ClasspathIndex.keyOf("META-INF/services/S")).isEqualTo("META-INF/services/S");
    assertThat(ClasspathIndex.keyOf("META-INF/services/")).isEqualTo("META-INF/services");
    assertThat(ClasspathIndex.keyOf("META-INF/versions/9/a/B.class"))
        .isEqualTo("META-INF/versions/9/a");
  }

  private void assertMatchesUrlClassLoader(URL[] urls, String... names) throws IOException {
    ClasspathIndex index = acquire(urls);
    try (URLClassLoader expected = new URLClassLoader(urls, null)) {
      for (String name : names) {
        assertWithMessage(name)
            .that(String.valueOf(index.findResource(name)))
            .isEqualTo(String.valueOf(expected.findResource(name)));
        assertWithMessage(name)
            .that(list(index.findResources(name)))
            .isEqualTo(list(expected.findResources(name)));
      }
    }
  }

  private ClasspathIndex acquire(URL[] urls) {
    ClasspathIndex index = ClasspathIndex.acquire(urls);
    assertThat(index).isNotNull();
    acquired.add(index);
    return index;
  }

  private static List<String> list(Enumeration<URL> urls) {
    List<String> list = new ArrayList<>();
    for (URL url : Collections.list(urls)) {
      list.add(url.toString());
    }
    return list;
  }

  private static String read(URL url) throws IOException {
    try (var in = url.openStream()) {
      return new String(in.readAllBytes(), UTF_8);
    }
  }

  private URL jar(String name, Map<String, String> entries) throws IOException {
    return jar(name, entries, ImmutableMap.of());
  }

  private URL jar(String name, Map<String, String> entries, Map<String, String> attributes)
      throws IOException {
    Manifest manifest = new Manifest();
    manifest.getMainAttributes().putValue("Manifest-Version", "1.0");
    attributes.forEach(manifest.getMainAttributes()::putValue);
    Path path = root.resolve(name);
    try (OutputStream out = Files.newOutputStream(path);
        JarOutputStream jar = new JarOutputStream(out, manifest)) {
      for (Map.Entry<String, String> entry : entries.entrySet()) {
        jar.putNextEntry(new JarEntry(entry.getKey()));
        jar.write(entry.getValue().getBytes(UTF_8));
        jar.closeEntry();
      }
    }
    return path.toUri().toURL();
  }
}
