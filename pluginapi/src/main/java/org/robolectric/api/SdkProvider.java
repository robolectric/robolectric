package org.robolectric.api;

import java.net.URL;

public interface SdkProvider {
  Sdk[] availableSdks();

  URL getPathForSdk(Sdk sdk);
}
