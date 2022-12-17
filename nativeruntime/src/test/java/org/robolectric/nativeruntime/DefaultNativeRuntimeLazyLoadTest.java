package org.robolectric.nativeruntime;

import static com.google.common.truth.Truth.assertThat;

import android.app.Application;
import android.database.CursorWindow;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

@RunWith(RobolectricTestRunner.class)
public final class DefaultNativeRuntimeLazyLoadTest {

  /**
   * Checks to see that RNR is not loaded by default when an empty application is created. RNR load
   * times are typically 0.5-1s, so it is desirable to have it lazy loaded when native code is
   * called.
   */
  @SuppressWarnings("UnusedVariable")
  @Test
  public void lazyLoad() throws Exception {
    Application application = RuntimeEnvironment.getApplication();
    assertThat(DefaultNativeRuntimeLoader.isLoaded()).isFalse();
    CursorWindow cursorWindow = new CursorWindow("hi");
    cursorWindow.close();
    assertThat(DefaultNativeRuntimeLoader.isLoaded()).isTrue();
  }
}
