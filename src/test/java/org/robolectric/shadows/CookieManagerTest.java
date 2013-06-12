package org.robolectric.shadows;

import android.webkit.CookieManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(TestRunners.WithDefaults.class)
public class CookieManagerTest {

  @Test
  public void shouldGetASingletonInstance() {
    assertNotNull(CookieManager.getInstance());
    assertThat(CookieManager.getInstance()).isSameAs(CookieManager.getInstance());
  }

  @Test
  public void shouldSetAndGetACookie() {
    CookieManager cookieManager = CookieManager.getInstance();
    String url = "http://www.google.com";
    String value = "my cookie";
    cookieManager.setCookie(url, value);
    assertThat(cookieManager.getCookie(url)).isEqualTo(value);
  }

  @Test
  public void shouldGetCookieForUrl() {
    CookieManager cookieManager = CookieManager.getInstance();
    String url1 = "http://www.google.com";
    String value1 = "my cookie";
    cookieManager.setCookie(url1, value1);

    String url2 = "http://www.hotbot.com";
    String value2 = "some special value: thing";
    cookieManager.setCookie(url2, value2);

    assertThat(cookieManager.getCookie("http://www.google.com")).isEqualTo(value1);
    assertThat(cookieManager.getCookie(url2)).isEqualTo(value2);
  }

  @Test
  public void shouldRecordAcceptCookie() {
    CookieManager cookieManager = CookieManager.getInstance();
    cookieManager.setCookie("foo", "bar");
    cookieManager.setCookie("baz", "qux");
    assertNotNull(cookieManager.getCookie("foo"));
    cookieManager.removeAllCookie();
    assertNull(cookieManager.getCookie("foo"));
    assertNull(cookieManager.getCookie("baz"));
  }
}

