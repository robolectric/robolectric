package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;

import android.graphics.fonts.SystemFonts;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
public class SystemFontsQTest {
  /**
   * A test to ensure that {@link SystemFonts#getAvailableFonts} will trigger RNG to load if it is
   * called early in a test run.
   *
   * <p>This should be the first test that is run in this test class.
   */
  @Config(minSdk = Q, maxSdk = R)
  @Test
  public void getAvailableFonts() {
    SystemFonts.getAvailableFonts();
  }
}
