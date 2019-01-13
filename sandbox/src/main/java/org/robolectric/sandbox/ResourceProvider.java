package org.robolectric.sandbox;

import java.io.InputStream;
import java.net.URL;

public interface ResourceProvider {

  URL getResource(String name);

  InputStream getResourceAsStream(String resName);
}
