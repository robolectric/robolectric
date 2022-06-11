package org.robolectric.internal;

import java.util.Collection;
import java.util.Map;

public interface ShadowProvider {

  void reset();

  String[] getProvidedPackageNames();

  Collection<Map.Entry<String, String>> getShadows();
}
