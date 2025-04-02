package org.robolectric.shadows;

import android.content.Context;
import android.webkit.WebSettings;
import javax.annotation.Nullable;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.fakes.RoboWebSettings;

/**
 * Shadow of {@link WebSettings} which returns a dummy user a stub instance rather than the
 * User-Agent used by a WebView.
 */
@Implements(value = WebSettings.class)
public class ShadowWebSettings {

  /**
   * Returns the default User-Agent used by a WebView. An instance of WebView could use a different
   * User-Agent if a call is made to {@link WebSettings#setUserAgentString(String)}.
   *
   * @param context a Context object used to access application assets
   */
  @Implementation
  protected static String getDefaultUserAgent(Context context) {
    return RoboWebSettings.getDefaultUserAgent();
  }

  /**
   * Sets the default user agent for the WebView. The value set here is returned from {@link
   * #getDefaultUserAgent(Context)}.
   *
   * <p>If the value is null, the default user agent will be provided by Robolectric.
   */
  public static void setDefaultUserAgent(@Nullable String defaultUserAgent) {
    RoboWebSettings.setDefaultUserAgentOverride(defaultUserAgent);
  }

  @Resetter
  public static void reset() {
    RoboWebSettings.setDefaultUserAgentOverride(null);
  }
}
