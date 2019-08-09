package org.robolectric.shadows;

import android.content.Context;
import android.webkit.WebViewDatabase;
import javax.annotation.Nullable;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(value = WebViewDatabase.class, callThroughByDefault = false)
public class ShadowWebViewDatabase {
  private static RoboWebViewDatabase webViewDatabase;

  @Implementation
  protected static WebViewDatabase getInstance(Context ignored) {
    if (webViewDatabase == null) {
      webViewDatabase = new RoboWebViewDatabase();
    }
    return webViewDatabase;
  }

  /** Resets the {@code WebViewDatabase} instance to clear any state between tests. */
  public void resetDatabase() {
    webViewDatabase = null;
  }

  /** Returns {@code true} if {@link WebViewDatabase#clearFormData()} was called. */
  public boolean wasClearFormDataCalled() {
    return webViewDatabase.wasClearFormDataCalled();
  }

  /** Resets {@link #wasClearFormDataCalled()}, setting it back to false. */
  public void resetClearFormData() {
    webViewDatabase.resetClearFormData();
  }

  private static final class RoboWebViewDatabase extends WebViewDatabase {
    private boolean wasClearFormDataCalled = false;

    RoboWebViewDatabase() {}

    @Override
    public boolean hasUsernamePassword() {
      return false;
    }

    @Override
    public void clearUsernamePassword() {}

    @Override
    public boolean hasHttpAuthUsernamePassword() {
      return false;
    }

    @Override
    public void clearHttpAuthUsernamePassword() {}

    @Override
    public void setHttpAuthUsernamePassword(
        String host, String realm, String username, String password) {}

    @Nullable
    @Override
    public String[] getHttpAuthUsernamePassword(String host, String realm) {
      return null;
    }

    @Override
    public boolean hasFormData() {
      return false;
    }

    @Override
    public void clearFormData() {
      wasClearFormDataCalled = true;
    }

    private boolean wasClearFormDataCalled() {
      return wasClearFormDataCalled;
    }

    private void resetClearFormData() {
      wasClearFormDataCalled = false;
    }
  }
}
