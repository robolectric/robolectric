package org.robolectric.shadows;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.cookie.SM;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BestMatchSpec;
import org.apache.http.message.BasicHeader;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import android.webkit.CookieManager;

/**
 * Shadows the {@code android.webkit.CookieManager} class.
 */
@Implements(CookieManager.class)
public class ShadowCookieManager {
  private static final String HTTP = "http://";
  private static final String HTTPS = "https://";
  private static final String[] COOKIE_ATTRS_NOT_STRICT = {"Expires", "expires"};
  private static CookieManager sRef;
  private CookieStore store = new BasicCookieStore();
  private boolean accept;
  private static final List<Cookie> emtpyCookieList = new ArrayList<Cookie>();

  @Implementation
  public static CookieManager getInstance() {
    if (sRef == null) {
      sRef = Robolectric.newInstanceOf(CookieManager.class);
    }
    return sRef;
  }

  @Implementation
  public void setCookie(String url, String value) {
    List<Cookie> cookies = parseCookies(url, value);
    for(Cookie cookie : cookies) {
      store.addCookie(cookie);
    }
  }

  @Implementation
  public String getCookie(String url) {
    CookieOrigin origin = getOrigin(url);
    List<Cookie> matchedCookies = filter(origin);
    if (matchedCookies.isEmpty()) {
      return null;
    }

    StringBuffer cookieHeaderValue = new StringBuffer();
    for (int i=0, n= matchedCookies.size(); i<n ; i++) {
      Cookie cookie = matchedCookies.get(i);

      if (i > 0) {
        cookieHeaderValue.append("; ");
      }
      cookieHeaderValue.append(cookie.getName());
      String value = cookie.getValue();
      if (value != null) {
        cookieHeaderValue.append("=");
        cookieHeaderValue.append(value);
      }
    }

    return cookieHeaderValue.toString();
  }

  private List<Cookie> filter(CookieOrigin origin) {
    List<Cookie> matchedCookies = new ArrayList<Cookie>();
    Date now = new Date();
    CookieSpec cookieSpec = createSpec();
    for (Cookie cookie : store.getCookies()) {
        if (!cookie.isExpired(now)) {
            if (cookieSpec.match(cookie, origin)) {
                matchedCookies.add(cookie);
            }
        }
    }
    return matchedCookies;
  }

  @Implementation
  public void setAcceptCookie(boolean accept) {
    this.accept = accept;
  }

  @Implementation
  public boolean acceptCookie() {
    return this.accept;
  }

  @Implementation
  public void removeAllCookie() {
    store.clear();
  }
  
  @Implementation
  public void removeExpiredCookie() {
    store.clearExpired(new Date());
  }

  @Implementation
  public boolean hasCookies() {
    return !store.getCookies().isEmpty();
  }

  @Implementation
  public void removeSessionCookie() {
    synchronized(store){
      clearAndAddPersistentCookies();
    }
  }

  private void clearAndAddPersistentCookies() {
    List<Cookie> cookies = new ArrayList<Cookie>(store.getCookies());
    store.clear();
    for(Cookie cookie : cookies) {
      if(cookie.isPersistent()){
        store.addCookie(cookie);
      }
    }
  }

  private List<Cookie> parseCookies(String url, String cookieHeader) {
      CookieOrigin origin = getOrigin(url);
      BasicHeader header = new BasicHeader(SM.SET_COOKIE, cookieHeader);
      int attrIndex = 0;
      do {
        try {
          CookieSpec cookieSpec = createSpec();
          return cookieSpec.parse(header, origin);
        } catch (MalformedCookieException e) {
          int indexOfAttrTitle = cookieHeader.indexOf(COOKIE_ATTRS_NOT_STRICT[attrIndex]);
          if (indexOfAttrTitle != -1) {
            cookieHeader = cookieHeader.substring(0,indexOfAttrTitle);
            header = new BasicHeader(SM.SET_COOKIE, cookieHeader);
          }
          attrIndex++;
        }
      } while (attrIndex <= COOKIE_ATTRS_NOT_STRICT.length);
      return emtpyCookieList;
  }

  private CookieSpec createSpec() {
    return new BestMatchSpec();
  }

  private CookieOrigin getOrigin(String url) {
    if ( !(url.startsWith(HTTP) || url.startsWith(HTTPS)) ) {
      url = HTTP + url;
    }
    URI uri = null;
    try {
      uri = new URI(url);
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException("wrong URL :" + url, e);
    }

    int port = (uri.getPort() < 0) ? 80 : uri.getPort();
    boolean secure = "https".equals(uri.getScheme());
    CookieOrigin origin = new CookieOrigin(uri.getHost(), port, uri.getPath(),  secure);
    return origin;
  }
}
