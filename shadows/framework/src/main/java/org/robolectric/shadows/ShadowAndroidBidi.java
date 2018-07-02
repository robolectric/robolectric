package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O_MR1;

import android.text.Layout;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(className = "android.text.AndroidBidi", isInAndroidSdk = false)
public class ShadowAndroidBidi {
  @Implementation(maxSdk = O_MR1)
  public static int bidi(int dir, char[] chs, byte[] chInfo, int n, boolean haveInfo) {
    // sorry, arabic, hebrew, syriac, n'ko, imperial aramaic, and old turks!
    return Layout.DIR_LEFT_TO_RIGHT;
  }
}