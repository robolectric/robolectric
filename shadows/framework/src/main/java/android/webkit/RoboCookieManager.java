package android.webkit;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.Nullable;

/**
 * Robolectric implementation of {@link android.webkit.CookieManager}.
 *
 * <p>Basic implementation which does not fully implement RFC2109.
 */
public class RoboCookieManager extends CookieManager {
  private static final String HTTP = "http://";
  private static final String HTTPS = "https://";
  private static final String EXPIRATION_FIELD_NAME = "Expires";
  private static final String SECURE_ATTR_NAME = "SECURE";
  private final List<Cookie> store = new ArrayList<>();
  private boolean accept;

  @Override
  public void setCookie(String url, String value) {
    Cookie cookie = parseCookie(url, value);
    if (cookie != null) {
      store.add(cookie);
    }
  }

  @Override
  public void setCookie(String url, String value, @Nullable ValueCallback<Boolean> valueCallback) {
    setCookie(url, value);
    if (valueCallback != null) {
      valueCallback.onReceiveValue(true);
    }
  }

  @Override
  public void setAcceptThirdPartyCookies(WebView webView, boolean b) {}

  @Override
  public boolean acceptThirdPartyCookies(WebView webView) {
    return false;
  }

  @Override
  public void removeAllCookies(@Nullable ValueCallback<Boolean> valueCallback) {
    store.clear();
    if (valueCallback != null) {
      valueCallback.onReceiveValue(Boolean.TRUE);
    }
  }

  @Override
  public void flush() {}

  @Override
  public void removeSessionCookies(@Nullable ValueCallback<Boolean> valueCallback) {
    boolean value;
    synchronized (store) {
      value = clearAndAddPersistentCookies();
    }
    if (valueCallback != null) {
      valueCallback.onReceiveValue(value);
    }
  }

  @Override
  public String getCookie(String url) {
    // Return null value for empty url
    if (url == null || url.equals("")) {
      return null;
    }

    try {
      url = URLDecoder.decode(url, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }

    final List<Cookie> matchedCookies;
    if (url.startsWith(".")) {
      matchedCookies = filter(url.substring(1));
    } else if (url.contains("//.")) {
      matchedCookies = filter(url.substring(url.indexOf("//.") + 3));
    } else {
      matchedCookies = filter(getCookieHost(url), url.startsWith(HTTPS));
    }
    if (matchedCookies.isEmpty()) {
      return null;
    }

    StringBuilder cookieHeaderValue = new StringBuilder();
    for (int i = 0, n = matchedCookies.size(); i < n; i++) {
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

  @Override
  public String getCookie(String s, boolean b) {
    return null;
  }

  private List<Cookie> filter(String domain) {
    return filter(domain, false);
  }

  private List<Cookie> filter(String domain, boolean isSecure) {
    List<Cookie> matchedCookies = new ArrayList<>();
    for (Cookie cookie : store) {
      if (cookie.isSameHost(domain) && (isSecure == cookie.isSecure() || isSecure)) {
        matchedCookies.add(cookie);
      }
    }
    return matchedCookies;
  }

  @Override
  public void setAcceptCookie(boolean accept) {
    this.accept = accept;
  }

  @Override
  public boolean acceptCookie() {
    return this.accept;
  }

  public void removeAllCookie() {
    store.clear();
  }

  public void removeExpiredCookie() {
    List<Cookie> expired = new ArrayList<>();
    Date now = new Date();

    for (Cookie cookie : store) {
      if (cookie.isExpiredAt(now)) {
        expired.add(cookie);
      }
    }

    store.removeAll(expired);
  }

  @Override
  public boolean hasCookies() {
    return !store.isEmpty();
  }

  @Override
  public boolean hasCookies(boolean b) {
    return false;
  }

  public void removeSessionCookie() {
    synchronized (store) {
      clearAndAddPersistentCookies();
    }
  }

  @Override
  protected boolean allowFileSchemeCookiesImpl() {
    return false;
  }

  @Override
  protected void setAcceptFileSchemeCookiesImpl(boolean b) {}

  private boolean clearAndAddPersistentCookies() {
    List<Cookie> existing = new ArrayList<>(store);
    int length = store.size();
    store.clear();
    for (Cookie cookie : existing) {
      if (cookie.isPersistent()) {
        store.add(cookie);
      }
    }
    return store.size() < length;
  }

  @Nullable
  private static Cookie parseCookie(String url, String cookieHeader) {
    Date expiration = null;
    boolean isSecure = false;

    String[] fields = cookieHeader.split(";", 0);
    String cookieValue = fields[0].trim();

    for (int i = 1; i < fields.length; i++) {
      String field = fields[i].trim();
      if (field.startsWith(EXPIRATION_FIELD_NAME)) {
        expiration = getExpiration(field);
      } else if (field.toUpperCase().equals(SECURE_ATTR_NAME)) {
        isSecure = true;
      }
    }

    String hostname = getCookieHost(url);
    if (expiration == null || expiration.compareTo(new Date()) >= 0) {
      return new Cookie(hostname, isSecure, cookieValue, expiration);
    }

    return null;
  }

  private static String getCookieHost(String url) {
    if (!(url.startsWith(HTTP) || url.startsWith(HTTPS))) {
      url = HTTP + url;
    }

    try {
      return new URI(url).getHost();
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException("wrong URL : " + url, e);
    }
  }

  private static Date getExpiration(String field) {
    int equalsIndex = field.indexOf("=");

    if (equalsIndex < 0) {
      return null;
    }

    String date = field.substring(equalsIndex + 1);

    try {
      DateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
      return dateFormat.parse(date);
    } catch (ParseException e) {
      // No-op. Try to inferFromValue additional date formats.
    }

    try {
      DateFormat dateFormat = new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss zzz");
      return dateFormat.parse(date);
    } catch (ParseException e) {
      return null; // Was not parsed by any date formatter.
    }
  }

  private static class Cookie {
    private final String mName;
    private final String mValue;
    private final Date mExpiration;
    private final String mHostname;
    private final boolean mIsSecure;

    public Cookie(String hostname, boolean isSecure, String cookie, Date expiration) {
      mHostname = hostname;
      mIsSecure = isSecure;
      mExpiration = expiration;

      int equalsIndex = cookie.indexOf("=");
      if (equalsIndex >= 0) {
        mName = cookie.substring(0, equalsIndex);
        mValue = cookie.substring(equalsIndex + 1);
      } else {
        mName = cookie;
        mValue = null;
      }
    }

    public String getName() {
      return mName;
    }

    public String getValue() {
      return mValue;
    }

    public boolean isExpiredAt(Date date) {
      return mExpiration != null && mExpiration.compareTo(date) < 0;
    }

    public boolean isPersistent() {
      return mExpiration != null;
    }

    public boolean isSameHost(String host) {
      return mHostname.endsWith(host);
    }

    public boolean isSecure() {
      return mIsSecure;
    }
  }
}
