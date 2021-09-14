package org.robolectric;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import android.database.CursorWindow;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.SQLiteMode;
import org.robolectric.annotation.SQLiteMode.Mode;

/** Tests for {@link org.robolectric.nativeruntime.NativeRuntimeLoader} */
@RunWith(RobolectricTestRunner.class)
@SQLiteMode(Mode.NATIVE)
public class NativeRuntimeLoaderTest {

  public static final String NATIVERUNTIME_LOADED = "robolectric.nativeruntime.loaded";
  public static final String LOGGING_ENABLED = "robolectric.logging.enabled";
  private PrintStream originalSystemOut;
  private boolean originalLoggingEnabled;
  private ByteArrayOutputStream byteArrayOutputStream;

  @Before
  public void setUp() {
    originalLoggingEnabled = Boolean.getBoolean(LOGGING_ENABLED);
    System.setProperty(LOGGING_ENABLED, "true");
    originalSystemOut = System.out;
    byteArrayOutputStream = new ByteArrayOutputStream();
    PrintStream printStream = new PrintStream(byteArrayOutputStream);
    System.setOut(printStream);
  }

  @After
  public void tearDown() throws Exception {
    boolean loadedDuringTest =
        byteArrayOutputStream.toString("UTF-8").contains("Loading the native runtime");
    if (hasBeenLoaded() && loadedDuringTest) {
      fail("The native runtime has been loaded multiple times");
    } else if (loadedDuringTest) {
      markLoaded();
    }
    assertThat(hasBeenLoaded()).isTrue();
    System.setOut(originalSystemOut);
    System.setProperty(LOGGING_ENABLED, Boolean.toString(originalLoggingEnabled));
  }

  @Test
  public void nativeRuntime_shouldBeLoadedOnce() {
    // (maybe) trigger a native runtime load
    new CursorWindow("API" + RuntimeEnvironment.getApiLevel());
  }

  private static boolean hasBeenLoaded() {
    return Boolean.getBoolean(NATIVERUNTIME_LOADED);
  }

  private static void markLoaded() {
    System.setProperty(NATIVERUNTIME_LOADED, "true");
  }
}
