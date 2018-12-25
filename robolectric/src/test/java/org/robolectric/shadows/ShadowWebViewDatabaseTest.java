package org.robolectric.shadows;

import android.webkit.WebViewDatabase;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests for {@link ShadowWebViewDatabase} */
@RunWith(AndroidJUnit4.class)
public final class ShadowWebViewDatabaseTest {

  @Test
  public void webViewDatabaseDoesNotCrash() throws Exception {
    WebViewDatabase.getInstance(ApplicationProvider.getApplicationContext());
  }
}
