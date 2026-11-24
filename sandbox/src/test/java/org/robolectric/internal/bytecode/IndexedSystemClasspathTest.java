package org.robolectric.internal.bytecode;

import static com.google.common.truth.Truth.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class IndexedSystemClasspathTest {
  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private final List<ClasspathIndex> indexesCreated = new ArrayList<>();
  private final List<Runnable> indexingNotYetRun = new ArrayList<>();
  // A class path of two entries.
  private String classPath;

  @Before
  public void createClassPath() throws Exception {
    File directory = temporaryFolder.newFolder("classes");
    write(new File(directory, "a/One.class"), "one");
    write(new File(directory, "java/lang/String.class"), "not the platform's");
    classPath = directory + File.pathSeparator + new File(directory, "missing.jar");
  }

  @After
  public void closeIndexes() {
    indexesCreated.forEach(ClasspathIndex::close);
  }

  @Test
  public void leavesAShortClassPathUnindexed() {
    IndexedSystemClasspath classpath =
        new IndexedSystemClasspath(classPath, 3, this::createIndex, Runnable::run);

    classpath.startIndexing();

    assertThat(classpath.getResourceAsStream("a/One.class")).isNull();
    assertThat(indexesCreated).isEmpty();
  }

  @Test
  public void leavesAnUnsetClassPathUnindexed() {
    IndexedSystemClasspath classpath =
        new IndexedSystemClasspath(null, 2, this::createIndex, Runnable::run);

    classpath.startIndexing();

    assertThat(classpath.getResourceAsStream("a/One.class")).isNull();
    assertThat(indexesCreated).isEmpty();
  }

  @Test
  public void leavesLookupsToTheCallerUntilTheIndexIsReady() throws Exception {
    IndexedSystemClasspath classpath =
        new IndexedSystemClasspath(classPath, 2, this::createIndex, indexingNotYetRun::add);

    assertThat(classpath.getResourceAsStream("a/One.class")).isNull();
    classpath.startIndexing();
    assertThat(classpath.getResourceAsStream("a/One.class")).isNull();
    indexingNotYetRun.forEach(Runnable::run);

    assertThat(read(classpath.getResourceAsStream("a/One.class"))).isEqualTo("one");
    assertThat(classpath.getResourceAsStream("a/Missing.class")).isNull();
  }

  @Test
  public void indexesTheClassPathOnce() throws Exception {
    IndexedSystemClasspath classpath =
        new IndexedSystemClasspath(classPath, 2, this::createIndex, Runnable::run);

    classpath.startIndexing();
    classpath.startIndexing();

    assertThat(read(classpath.getResourceAsStream("a/One.class"))).isEqualTo("one");
    assertThat(indexesCreated).hasSize(1);
  }

  @Test
  public void leavesFilesOfThePlatformClassLoaderToTheCaller() {
    IndexedSystemClasspath classpath =
        new IndexedSystemClasspath(classPath, 2, this::createIndex, Runnable::run);

    classpath.startIndexing();

    assertThat(classpath.getResourceAsStream("java/lang/String.class")).isNull();
  }

  @Test
  public void leavesEveryLookupToTheCallerIfTheClassPathCannotBeIndexed() {
    IndexedSystemClasspath classpath =
        new IndexedSystemClasspath(classPath, 2, urls -> null, Runnable::run);

    classpath.startIndexing();

    assertThat(classpath.getResourceAsStream("a/One.class")).isNull();
  }

  @Test
  public void indexesOnADaemonThread() throws Exception {
    AtomicBoolean ranOnDaemonThread = new AtomicBoolean();
    CountDownLatch ran = new CountDownLatch(1);

    IndexedSystemClasspath.daemonThread()
        .execute(() -> {
          ranOnDaemonThread.set(Thread.currentThread().isDaemon());
          ran.countDown();
        });

    assertThat(ran.await(10, SECONDS)).isTrue();
    assertThat(ranOnDaemonThread.get()).isTrue();
  }

  private ClasspathIndex createIndex(URL[] urls) {
    ClasspathIndex index = ClasspathIndex.create(urls);
    indexesCreated.add(index);
    return index;
  }

  private static void write(File file, String content) throws Exception {
    file.getParentFile().mkdirs();
    Files.write(file.toPath(), content.getBytes(UTF_8));
  }

  private static String read(InputStream in) throws Exception {
    try (InputStream stream = in) {
      return new String(stream.readAllBytes(), UTF_8);
    }
  }
}
