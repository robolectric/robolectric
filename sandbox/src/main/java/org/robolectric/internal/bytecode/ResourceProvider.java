package org.robolectric.internal.bytecode;

import java.io.InputStream;
import java.net.URL;

/** A provider of resources (à la ClassLoader). */
public interface ResourceProvider {

  URL getResource(String resName);

  InputStream getResourceAsStream(String resName);
}
