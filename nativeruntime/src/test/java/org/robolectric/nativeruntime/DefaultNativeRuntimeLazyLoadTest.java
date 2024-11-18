package org.robolectric.nativeruntime;

import static com.google.common.truth.Truth.assertThat;

import android.database.CursorWindow;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.versioning.AndroidVersions.U;

@RunWith(RobolectricTestRunner.class)
@Config(minSdk = Config.OLDEST_SDK, maxSdk = U.SDK_INT)
public final class DefaultNativeRuntimeLazyLoadTest {

  /**
   * Checks to see that RNR is not loaded by default when an empty application is created. RNR load
   * times are typically 0.5-1s, so it is desirable to have it lazy loaded when native code is
   * called.
   *
   * <p>Note that lazy loading is disabled for V and above.
   */
  @Test
  public void lazyLoad() {
    RuntimeEnvironment.getApplication();
    assertThat(DefaultNativeRuntimeLoader.isLoaded()).isFalse();
    CursorWindow cursorWindow = new CursorWindow("hi");
    cursorWindow.close();
    assertThat(DefaultNativeRuntimeLoader.isLoaded()).isTrue();
  }
}
