package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.L;
import static android.os.Build.VERSION_CODES.M;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import android.os.Debug;
import java.io.File;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
public class ShadowDebugTest {

  private static final String TRACE_FILENAME = "dmtrace.trace";

  @Before
  public void setup() {
    ShadowDebug.reset();
  }

  @Test
  public void initNoCrash() {
    assertThat(Debug.getNativeHeapAllocatedSize()).isAtLeast(0L);
  }

  @Test
  @Config(minSdk = M)
  public void getRuntimeStats() {
    assertThat(Debug.getRuntimeStats()).isNotNull();
  }

  @Test
  @Config
  public void startStopTracingShouldWriteFile() {
    Debug.startMethodTracing(TRACE_FILENAME);
    Debug.stopMethodTracing();

    assertThat(
            new File(RuntimeEnvironment.application.getExternalFilesDir(null), TRACE_FILENAME)
                .exists())
        .isTrue();
  }

  @Test
  @Config(minSdk = L)
  public void startStopTracingSamplingShouldWriteFile() {
    Debug.startMethodTracingSampling(TRACE_FILENAME, 100, 100);
    Debug.stopMethodTracing();

    assertThat(
            new File(RuntimeEnvironment.application.getExternalFilesDir(null), TRACE_FILENAME)
                .exists())
        .isTrue();
  }

  @Test
  @Config
  public void startTracingShouldThrowIfAlreadyStarted() {
    Debug.startMethodTracing(TRACE_FILENAME);

    assertThrows(RuntimeException.class, () -> Debug.startMethodTracing(TRACE_FILENAME));
  }

  @Test
  @Config
  public void stopTracingShouldThrowIfNotStarted() {
    assertThrows(RuntimeException.class, () -> Debug.stopMethodTracing());
  }
}
