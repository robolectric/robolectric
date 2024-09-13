package org.robolectric;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.S;

import android.content.Intent;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.Reflector;

/** Tests for @{@link Direct} annotation incorporated inside {@link Reflector}. */
@RunWith(AndroidJUnit4.class)
public class MethodNameTest {
  @Config(sdk = S, shadows = ShadowThrowingIntent.class)
  @Test
  public void methodName_shouldNotInvokeOlderSdks() {
    Intent intent = new Intent();
    intent.setAction("test"); // should not crash
  }

  @Implements(Intent.class)
  public static class ShadowThrowingIntent {
    @Implementation(minSdk = LOLLIPOP, maxSdk = LOLLIPOP, methodName = "setAction")
    protected void setActionImpl(String action) {
      throw new RuntimeException("Should never get called");
    }
  }
}
