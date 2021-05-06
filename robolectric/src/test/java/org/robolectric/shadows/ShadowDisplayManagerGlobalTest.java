package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static com.google.common.truth.Truth.assertThat;

import android.hardware.display.DisplayManagerGlobal;
import android.view.Display;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.experimental.LazyApplication;
import org.robolectric.annotation.experimental.LazyApplication.LazyLoad;

/** Unit tests for {@link ShadowDisplayManagerGlobal} */
@RunWith(AndroidJUnit4.class)
public class ShadowDisplayManagerGlobalTest {

  @LazyApplication(LazyLoad.ON)
  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void testDisplayManagerGlobalIsLazyLoaded() {
    assertThat(ShadowDisplayManagerGlobal.getGlobalInstance()).isNull();
    assertThat(DisplayManagerGlobal.getInstance().getRealDisplay(Display.DEFAULT_DISPLAY))
        .isNotNull();
  }
}
