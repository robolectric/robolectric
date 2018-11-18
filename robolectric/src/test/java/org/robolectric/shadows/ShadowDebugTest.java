package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.L;
import static android.os.Build.VERSION_CODES.M;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import android.content.Context;
import android.os.Debug;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.File;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
public class ShadowDebugTest {

  private static final String TRACE_FILENAME = "dmtrace.trace";
  private Context context;

  @Before
  public void setUp() throws Exception {
    context = ApplicationProvider.getApplicationContext();
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
  public void startStopTracingShouldWriteFile() {
    Debug.startMethodTracing(TRACE_FILENAME);
    Debug.stopMethodTracing();

    assertThat(new File(context.getExternalFilesDir(null), TRACE_FILENAME).exists()).isTrue();
  }

  @Test
  @Config(minSdk = L)
  public void startStopTracingSamplingShouldWriteFile() {
    Debug.startMethodTracingSampling(TRACE_FILENAME, 100, 100);
    Debug.stopMethodTracing();

    assertThat(new File(context.getExternalFilesDir(null), TRACE_FILENAME).exists()).isTrue();
  }

  @Test
  public void startTracingShouldThrowIfAlreadyStarted() {
    Debug.startMethodTracing(TRACE_FILENAME);

    try {
      Debug.startMethodTracing(TRACE_FILENAME);
      fail("RuntimeException not thrown.");
    } catch (RuntimeException e) {
      // expected
    }
  }

  @Test
  public void stopTracingShouldThrowIfNotStarted() {
    try {
      Debug.stopMethodTracing();
      fail("RuntimeException not thrown.");
    } catch (RuntimeException e) {
      // expected
    }
  }
}
