package org.robolectric.internal.bytecode;

import java.io.Closeable;
import java.io.InputStream;
import java.net.URL;

/** A provider of resources (à la ClassLoader). */
public interface ResourceProvider extends Closeable {

  URL getResource(String resName);

  InputStream getResourceAsStream(String resName);
}
