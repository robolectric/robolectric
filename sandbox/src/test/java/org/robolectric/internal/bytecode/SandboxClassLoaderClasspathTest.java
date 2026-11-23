package org.robolectric.internal.bytecode;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

/** Tests for how {@link SandboxClassLoader} finds class path resources, and what it caches. */
@RunWith(JUnit4.class)
public class SandboxClassLoaderClasspathTest {
  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private final InstrumentationConfiguration config =
      InstrumentationConfiguration.newBuilder().build();
  private final List<AutoCloseable> toClose = new ArrayList<>();
  private Path root;

  @Before
  public void setUp() throws IOException {
    root = temporaryFolder.getRoot().toPath().toRealPath();
    ClasspathIndex.clearShared();
    System.setProperty(ClasspathIndex.PROPERTY, "true");
  }

  @After
  public void tearDown() throws Exception {
    for (AutoCloseable closeable : toClose) {
      closeable.close();
    }
    ClasspathIndex.clearShared();
    System.clearProperty(ClasspathIndex.PROPERTY);
  }

  @Test
  public void lookups_matchUnindexedLookups_withUrlClassLoaderParent() throws Exception {
    Path dir = Files.createDirectories(root.resolve("classes/dir"));
    Files.write(dir.resolve("in-dir.txt"), "dir".getBytes(UTF_8));
    URL[] urls = {
      jar("empty.jar"),
      jar("a.jar", "p/A", "p/res.txt", "META-INF/services/S"),
      root.resolve("classes").toUri().toURL(),
      jar("b.jar", "p/A", "p/B", "p/res.txt", "META-INF/services/S"),
    };
    URLClassLoader parent = closeLater(new URLClassLoader(urls, getClass().getClassLoader()));
    SandboxClassLoader indexed = sandbox(parent);
    SandboxClassLoader unindexed;
    System.setProperty(ClasspathIndex.PROPERTY, "false");
    try {
      unindexed = sandbox(parent);
    } finally {
      System.setProperty(ClasspathIndex.PROPERTY, "true");
    }

    for (String name :
        new String[] {
          "p/A.class",
          "p/B.class",
          "p/res.txt",
          "dir/in-dir.txt",
          "META-INF/services/S",
          "p/missing.txt",
          "missing/C.class",
          "org/junit/Test.class",
        }) {
      assertWithMessage(name)
          .that(String.valueOf(indexed.getResource(name)))
          .isEqualTo(String.valueOf(unindexed.getResource(name)));
      assertWithMessage(name)
          .that(Collections.list(indexed.getResources(name)))
          .isEqualTo(Collections.list(unindexed.getResources(name)));
    }
    // p.A comes from a.jar, the first jar that has it.
    assertThat(indexed.loadClass("p.A").getClassLoader()).isSameInstanceAs(indexed);
    assertThat(read(indexed.getResource("p/res.txt"))).isEqualTo("a.jar");
    assertThat(indexed.loadClass("p.B").getClassLoader()).isSameInstanceAs(indexed);
  }

  @Test
  public void lookups_matchTheApplicationClassLoader() throws Exception {
    ClassLoader app = ClassLoader.getSystemClassLoader();
    SandboxClassLoader indexed = sandbox(app);
    SandboxClassLoader unindexed;
    System.setProperty(ClasspathIndex.PROPERTY, "false");
    try {
      unindexed = sandbox(app);
    } finally {
      System.setProperty(ClasspathIndex.PROPERTY, "true");
    }

    for (String name :
        new String[] {
          "org/junit/Test.class",
          "com/google/common/truth/Truth.class",
          "org/robolectric/internal/bytecode/SandboxClassLoader.class",
          "META-INF/MANIFEST.MF",
          "com/sun/tools/javac/Main.class",
          "java/lang/Object.class",
          "module-info.class",
          "org/junit/",
          "no/such/Resource.txt",
        }) {
      assertWithMessage(name)
          .that(String.valueOf(indexed.getResource(name)))
          .isEqualTo(String.valueOf(app.getResource(name)));
      assertWithMessage(name)
          .that(Collections.list(indexed.getResources(name)))
          .isEqualTo(Collections.list(unindexed.getResources(name)));
    }
  }

  @Test
  public void applicationClassPathUrls_matchTheApplicationClassLoadersUrls() throws Exception {
    URL[] urls =
        SandboxClassLoader.ParentLookup.applicationClassPathUrls(
            System.getProperty("java.class.path"));
    assertThat(urls).isNotNull();
    URL junit = ClassLoader.getSystemClassLoader().getResource("org/junit/Test.class");
    String jar = junit.toString().replaceFirst("^jar:", "").replaceFirst("!/.*$", "");
    List<String> strings = new ArrayList<>();
    for (URL url : urls) {
      strings.add(url.toString());
    }
    assertThat(strings).contains(jar);
  }

  @Test
  public void close_releasesTheSharedIndex() throws Exception {
    URL[] urls = {jar("a.jar", "p/res.txt")};
    URLClassLoader parent = closeLater(new URLClassLoader(urls, getClass().getClassLoader()));
    SandboxClassLoader first = sandbox(parent);
    SandboxClassLoader second = sandbox(parent);
    first.getResource("p/res.txt");

    ClasspathIndex index = ClasspathIndex.acquire(urls);
    try {
      first.close();
      second.close();
      assertThat(index.openJarCount()).isEqualTo(1);
    } finally {
      index.release();
    }
    assertThat(index.openJarCount()).isEqualTo(0);
    assertThat(first.getResource("p/res.txt")).isNotNull(); // still found through the parent
  }

  @Test
  public void unclosedSandbox_releasesItsIndex_onceUnreachable() throws Exception {
    URL[] urls = {jar("a.jar", "p/res.txt")};
    URLClassLoader parent = closeLater(new URLClassLoader(urls, getClass().getClassLoader()));
    ClasspathIndex index = ClasspathIndex.acquire(urls);
    try {
      createAndAbandonSandbox(parent);
      // The sandbox holds two references: one for its own URLs, one for its parent's.
      assertThat(index.referenceCount()).isEqualTo(3);

      long deadline = System.nanoTime() + 10_000_000_000L;
      while (index.referenceCount() > 1 && System.nanoTime() < deadline) {
        System.gc();
        Thread.sleep(10);
      }
      assertThat(index.referenceCount()).isEqualTo(1);
    } finally {
      index.release();
    }
  }

  private void createAndAbandonSandbox(ClassLoader parent) {
    SandboxClassLoader sandbox =
        new SandboxClassLoader(
            parent,
            config,
            new UrlResourceProvider(),
            new ClassInstrumentor(new ShadowDecorator()));
    assertThat(sandbox.getResource("p/res.txt")).isNotNull();
  }

  @Test
  public void addUrl_goesBackToSearchingUrlsDirectly() throws Exception {
    URL[] urls = {jar("a.jar", "p/res.txt")};
    URL extra = jar("extra.jar", "q/extra.txt");
    URLClassLoader parent = closeLater(new URLClassLoader(urls, getClass().getClassLoader()));
    SandboxClassLoader loader =
        closeLater(
            new SandboxClassLoader(
                parent,
                config,
                new UrlResourceProvider(),
                new ClassInstrumentor(new ShadowDecorator())) {
              {
                addURL(extra);
              }
            });

    assertThat(read(loader.getResource("q/extra.txt"))).isEqualTo("extra.jar");
    assertThat(read(loader.getResource("p/res.txt"))).isEqualTo("a.jar");
  }

  private SandboxClassLoader sandbox(ClassLoader parent) {
    return closeLater(
        new SandboxClassLoader(
            parent,
            config,
            new UrlResourceProvider(),
            new ClassInstrumentor(new ShadowDecorator())));
  }

  private <T extends AutoCloseable> T closeLater(T closeable) {
    toClose.add(closeable);
    return closeable;
  }

  private static String read(URL url) throws IOException {
    try (InputStream in = url.openStream()) {
      return new String(in.readAllBytes(), UTF_8);
    }
  }

  /** A jar holding the named classes (no extension) and resources, each resource's text its jar. */
  private URL jar(String name, String... entries) throws IOException {
    Path path = root.resolve(name);
    try (OutputStream out = Files.newOutputStream(path);
        JarOutputStream jar = new JarOutputStream(out)) {
      for (String entry : entries) {
        boolean isClass = !entry.contains(".") && !entry.startsWith("META-INF/");
        jar.putNextEntry(new JarEntry(isClass ? entry + ".class" : entry));
        if (isClass) {
          ClassWriter cw = new ClassWriter(0);
          cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, entry, null, "java/lang/Object", null);
          cw.visitEnd();
          jar.write(cw.toByteArray());
        } else {
          jar.write(name.getBytes(UTF_8));
        }
        jar.closeEntry();
      }
    }
    return path.toUri().toURL();
  }
}
