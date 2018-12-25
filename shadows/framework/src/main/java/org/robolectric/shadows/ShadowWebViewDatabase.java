package org.robolectric.shadows;

import android.content.Context;
import android.webkit.WebViewDatabase;
import javax.annotation.Nullable;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(value = WebViewDatabase.class, callThroughByDefault = false)
public class ShadowWebViewDatabase {

  @Implementation
  protected static WebViewDatabase getInstance(Context ignored) {
    return new RoboWebViewDatabase();
  }

  private static final class RoboWebViewDatabase extends WebViewDatabase {

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
    public void clearFormData() {}
  }
}
