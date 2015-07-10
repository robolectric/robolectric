package org.robolectric.shadows;

import android.content.Context;
import android.webkit.WebViewDatabase;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.internal.Shadow;

/**
 * Shadow for {@link android.webkit.WebViewDatabase}.
 */
@Implements(value = WebViewDatabase.class, callThroughByDefault = false)
public class ShadowWebViewDatabase {

  @Implementation
  public static WebViewDatabase getInstance(Context ignored) {
    return Shadow.newInstanceOf(WebViewDatabase.class);
  }
}
