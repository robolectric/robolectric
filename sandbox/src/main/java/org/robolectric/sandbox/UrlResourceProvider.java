package org.robolectric.sandbox;

import java.net.URL;
import java.net.URLClassLoader;

public class UrlResourceProvider extends URLClassLoader implements ResourceProvider {

  public UrlResourceProvider(URL... urls) {
    super(urls, null);
  }
}
