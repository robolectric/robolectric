package org.robolectric.nativeruntime;

import static android.os.Build.VERSION_CODES.KITKAT;
import static com.google.common.truth.Truth.assertThat;

import android.app.Application;
import android.database.CursorWindow;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.versioning.AndroidVersions.U;

@RunWith(RobolectricTestRunner.class)
@Config(minSdk = KITKAT, maxSdk = U.SDK_INT)
public final class DefaultNativeRuntimeLazyLoadTest {

  /**
   * Checks to see that RNR is not loaded by default when an empty application is created. RNR load
   * times are typically 0.5-1s, so it is desirable to have it lazy loaded when native code is
   * called.
   *
   * <p>Note that lazy loading is disabled for V and above.
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
