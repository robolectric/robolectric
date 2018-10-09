package org.robolectric.shadows;

import android.webkit.WebStorage;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Shadow of {@link WebStorage} which constructs a stub instance rather than attempting to create a
 * full Chromium-backed instance.
 */
@Implements(value = WebStorage.class)
public class ShadowWebStorage {

  @Implementation
  protected static WebStorage getInstance() {
    return new WebStorage();
  }
}
