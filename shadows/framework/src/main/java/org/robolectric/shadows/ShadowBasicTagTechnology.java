package org.robolectric.shadows;

import java.io.IOException;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Extends BasicTagTechnology to allow for testing. */
@Implements(className = "android.nfc.tech.BasicTagTechnology")
public class ShadowBasicTagTechnology {
  private boolean isConnected = false;

  @Implementation
  protected boolean isConnected() {
    return isConnected;
  }

  @Implementation
  protected void connect() throws IOException {
    isConnected = true;
  }

  @Implementation
  protected void close() {
    isConnected = false;
  }
}
