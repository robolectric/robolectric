package org.robolectric.internal.bytecode;

import java.net.URL;
import java.net.URLClassLoader;

/** ResourceProvider using URLs. */
public class UrlResourceProvider extends URLClassLoader implements ResourceProvider {

  public UrlResourceProvider(URL... urls) {
    super(urls, null);
  }
}
