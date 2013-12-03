package org.robolectric.shadows;

import android.webkit.CookieManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(TestRunners.WithDefaults.class)
public class CookieManagerTest {
  String url = "robolectric.org/";
  String httpUrl = "http://robolectric.org/";
  String httpsUrl = "https://robolectric.org/";
  CookieManager cookieManager = Robolectric.newInstanceOf(CookieManager.class);;
  
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
  
  @Test
  public void shouldHaveCookieWhenCookieIsSet() {
    cookieManager.setCookie(url, "name=value; Expires=Wed, 09 Jun 2021 10:18:14 GMT");
    assertThat(cookieManager.hasCookies()).isEqualTo(true);
  }

  @Test
  public void shouldNotHaveCookieWhenCookieIsNotSet() {
    assertThat(cookieManager.hasCookies()).isEqualTo(false);
  }

  @Test
  public void shouldGetNullWhenCookieIsNotPresent() {
    assertThat(cookieManager.getCookie(url)).isNull();
  }

  @Test
  public void shouldGetNullWhenCookieIsNotPresentInUrl() {
    cookieManager.setCookie(httpUrl, "name=value; Expires=Wed, 11 Jul 2035 08:12:26 GMT");
    assertThat(cookieManager.getCookie("http://google.com")).isNull();
  }
 
  @Test
  public void shouldSetAndGetOneCookie() {
    cookieManager.setCookie(httpUrl, "name=value; Expires=Wed, 11 Jul 2035 08:12:26 GMT");
    assertThat(cookieManager.getCookie(httpUrl)).isEqualTo("name=value");
  }

  @Test
  public void shouldSetWithHttpAndGetWithoutHttp() {
    cookieManager.setCookie(httpUrl, "name=value; Expires=Wed, 11 Jul 2035 08:12:26 GMT");
    assertThat(cookieManager.getCookie(url)).isEqualTo("name=value");
  }

  @Test
  public void shouldSetWithHttpAndGetWithHttps() {
    cookieManager.setCookie(httpUrl, "name=value; Expires=Wed, 11 Jul 2035 08:12:26 GMT");
    assertThat(cookieManager.getCookie(httpsUrl)).isEqualTo("name=value");
  }

  @Test
  public void shouldSetTwoCookies() {
    cookieManager.setCookie(url, "name=value; Expires=Wed, 09 Jun 2021 10:18:14 GMT");
    cookieManager.setCookie(url, "name2=value2; Expires=Wed, 09 Jun 2021 10:18:14 GMT");
    assertThat(cookieManager.getCookie(url)).isEqualTo("name=value; name2=value2");
  }

  @Test
  public void shouldSetCookieWithInvalidExpiesValue() {
    cookieManager.setCookie(httpUrl, "name=value; Expires=3234asdfasdf10:18:14 GMT");
    assertThat(cookieManager.getCookie(url)).isEqualTo("name=value");
  }

  @Test
  public void shouldSetCookieWithoutValue() {
    cookieManager.setCookie(httpUrl, "name=");
    assertThat(cookieManager.getCookie(url)).isEqualTo("name=");
  }
  
  @Test
  public void shouldSetCookieWithNameOnly() {
    cookieManager.setCookie(httpUrl, "name");
    assertThat(cookieManager.getCookie(url)).isEqualTo("name");
  }

  @Test
  public void testSetAndGetCookieWhenAcceptCookieIsFalse() {
    cookieManager.setAcceptCookie(false);
    cookieManager.setCookie(httpUrl, "name=value; Expires=3234asdfasdf10:18:14 GMT");
    assertThat(cookieManager.getCookie(url)).isEqualTo("name=value");
    assertThat(cookieManager.acceptCookie()).isEqualTo(false);
  }

  @Test
  public void shouldRemoveAllCookie() {
    cookieManager.setCookie(url, "name=value; Expires=Wed, 09 Jun 2021 10:18:14 GMT");
    cookieManager.setCookie(url, "name2=value2;");
    cookieManager.removeAllCookie();
    assertThat(cookieManager.getCookie(url)).isNull();
  }

  @Test
  public void shouldRemoveExpiredCookie() {
    cookieManager.setCookie(url, "name=value; Expires=Wed, 11 Jul 2035 10:18:14 GMT");
    cookieManager.setCookie(url, "name2=value2; Expires=Wed, 13 Jul 2011 10:18:14 GMT");
    cookieManager.removeExpiredCookie();
    assertThat(cookieManager.getCookie(url)).isEqualTo("name=value");
  }

  @Test
  public void shouldRemoveSessionCookie() {
    cookieManager.setCookie(url, "name=value; Expires=Wed, 09 Jun 2021 10:18:14 GMT");
    cookieManager.setCookie(url, "name2=value2;");
    cookieManager.removeSessionCookie();
    assertThat(cookieManager.getCookie(url)).isEqualTo("name=value");
  }
}

