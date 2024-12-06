package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;

import android.view.ThreadedRenderer;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(minSdk = O, maxSdk = P)
public class ShadowNativeThreadedRendererTest {
  @Test
  public void testInitialization() {
    ThreadedRenderer unused =
        ThreadedRenderer.create(ApplicationProvider.getApplicationContext(), false, "Name");
  }
}
