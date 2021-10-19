package android.webkit;

import static com.google.common.truth.Truth.assertThat;

import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.internal.DoNotInstrument;

/** Compatibility test for {@link CookieManager} */
@DoNotInstrument
@RunWith(AndroidJUnit4.class)
public class CookieManagerTest {

  @Before
  public void setUp() {
    // Required to initialize native CookieManager for emulators with SDK < 19.
    if (VERSION.SDK_INT < VERSION_CODES.KITKAT) {
      CookieSyncManager.createInstance(ApplicationProvider.getApplicationContext());
    }
  }

  @After
  public void tearDown() {
    CookieManager.getInstance().removeAllCookie();
  }

  @Test
  public void setCookie_doesNotAllowMultipleCookies() {
    final String httpsUrl = "https://robolectric.org/";
    final CookieManager cookieManager = CookieManager.getInstance();
    cookieManager.setCookie(httpsUrl, "A=100; B=200");
    String cookie = cookieManager.getCookie(httpsUrl);
    assertThat(cookie).isEqualTo("A=100");
  }

  @Test
  public void setCookie_multipleCookies() {
    final String httpsUrl = "https://robolectric.org/";
    final CookieManager cookieManager = CookieManager.getInstance();
    cookieManager.setCookie(httpsUrl, "A=100;");
    cookieManager.setCookie(httpsUrl, "B=100;");
    String cookie = cookieManager.getCookie(httpsUrl);
    assertThat(cookie).isEqualTo("A=100; B=100");
  }

  @Test
  public void getCookie_doesNotReturnAttributes() {
    final String httpsUrl = "https://robolectric.org/";
    final CookieManager cookieManager = CookieManager.getInstance();
    cookieManager.setCookie(httpsUrl, "ID=test-id; Path=/; Domain=.robolectric.org");
    String cookie = cookieManager.getCookie(httpsUrl);
    assertThat(cookie).isEqualTo("ID=test-id");
  }
}
