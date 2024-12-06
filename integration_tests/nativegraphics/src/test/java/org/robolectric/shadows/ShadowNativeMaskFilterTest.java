package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;

import android.graphics.MaskFilter;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = O)
public class ShadowNativeMaskFilterTest {
  @SuppressWarnings("CheckReturnValue")
  @Test
  public void testConstructor() {
    new MaskFilter();
  }
}
