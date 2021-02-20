package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.fail;

import android.content.Context;
import android.os.FileObserver;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.File;
import java.io.Writer;
import java.nio.file.Files;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests for {@link ShadowFileObserver} */
@RunWith(AndroidJUnit4.class)
public final class ShadowFileObserverTest {
  private File testDir;

  @Before
  public void setUp() {
    Context context = ApplicationProvider.getApplicationContext();
    File cacheDir = context.getCacheDir();
    testDir = new File(cacheDir, "test");
    testDir.mkdirs();
  }

  private static class Expectation {
    private int expectedEvent;
    @Nullable private String expectedPath;
    private CountDownLatch latch = null;

    public Expectation(int expectedEvent, @Nullable String expectedPath) {
      reset(expectedEvent, expectedPath);
    }

    public void reset(int expectedEvent, @Nullable String expectedPath) {
      this.expectedEvent = expectedEvent;
      this.expectedPath = expectedPath;
      this.latch = new CountDownLatch(1);
    }

    public void check(int event, @Nullable String path) {
      if (this.expectedEvent == event
          && ((this.expectedPath == null && path == null)
              || (this.expectedPath != null && this.expectedPath.equals(path)))) {
        this.latch.countDown();
      }
    }

    public void expect() throws InterruptedException {
      this.latch.await(30, TimeUnit.SECONDS);
    }

    public boolean hasBeenMet() {
      return this.latch == null || this.latch.getCount() == 0;
    }
  }

  @Test
  public void monitorDirectory() throws Exception {
    File newFile = new File(testDir, "new.file");
    Expectation expectation = new Expectation(FileObserver.CREATE, newFile.getName());

    FileObserver fileObserver =
        new FileObserver(testDir.getAbsolutePath()) {
          @Override
          public void onEvent(int event, @Nullable String path) {
            if (!expectation.hasBeenMet()) {
              expectation.check(event, path);
            }
          }
        };

    fileObserver.startWatching();

    newFile.createNewFile();
    expectation.expect();

    expectation.reset(FileObserver.MODIFY, newFile.getName());
    try (Writer myWriter = Files.newBufferedWriter(newFile.toPath(), UTF_8)) {
      myWriter.write("Some Content.");
    }
    expectation.expect();

    expectation.reset(FileObserver.DELETE, newFile.getName());
    newFile.delete();
    expectation.expect();

    File secondFile = new File(testDir, "second.file");
    expectation.reset(FileObserver.CREATE, secondFile.getName());
    secondFile.createNewFile();
    expectation.expect();

    fileObserver.stopWatching();
  }

  @Test
  public void monitorFile() throws Exception {
    File newFile = new File(testDir, "new.file");
    File secondFile = new File(testDir, "second.file");
    Expectation expectation = new Expectation(FileObserver.CREATE, newFile.getName());

    FileObserver fileObserver =
        new FileObserver(newFile.getAbsolutePath()) {
          @Override
          public void onEvent(int event, @Nullable String path) {
            assertThat(path).isNotEqualTo(secondFile.getName());
            if (!expectation.hasBeenMet()) {
              expectation.check(event, path);
            }
          }
        };

    fileObserver.startWatching();

    newFile.createNewFile();
    expectation.expect();

    expectation.reset(FileObserver.MODIFY, newFile.getName());
    try (Writer myWriter = Files.newBufferedWriter(newFile.toPath(), UTF_8)) {
      myWriter.write("Some Content.");
    }
    expectation.expect();

    // The event handler is set to assert if it ever encounters anything about this second file.
    secondFile.createNewFile();
    try (Writer secondWriter = Files.newBufferedWriter(secondFile.toPath(), UTF_8)) {
      secondWriter.write("Some other content.");
    }

    expectation.reset(FileObserver.DELETE, newFile.getName());
    newFile.delete();
    expectation.expect();

    fileObserver.stopWatching();
  }

  @Test
  public void nothingToMonitor_regression() {
    // Tests that this implementation works even if there is nothing supported to monitor.
    File newFile = new File(testDir, "new.file");
    FileObserver fileObserver =
        new FileObserver(newFile.getAbsolutePath(), FileObserver.ATTRIB) {
          @Override
          public void onEvent(int event, @Nullable String path) {
            fail("Not expecting any events");
          }
        };

    fileObserver.startWatching();
    fileObserver.stopWatching();
  }
}
