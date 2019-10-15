package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;

import android.content.Context;
import android.webkit.WebSettings;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Shadow of {@link WebSettings} which returns a dummy user a stub instance rather than the
 * User-Agent used by a WebView.
 */
@Implements(value = WebSettings.class)
public class ShadowWebSettings {

  @Implementation(minSdk = JELLY_BEAN_MR2)
  public static String getDefaultUserAgent(Context context) {
    return "user";
  }
}
