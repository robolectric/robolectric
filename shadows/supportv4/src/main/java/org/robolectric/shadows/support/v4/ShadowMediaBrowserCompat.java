package org.robolectric.shadows.support.v4;

import android.support.v4.media.MediaBrowserCompat;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow class for using {@link MediaBrowserCompat} in tests. */
@Implements(MediaBrowserCompat.class)
public class ShadowMediaBrowserCompat {

  private static final String ROOT_ID = "root_id";

  private boolean isConnected;

  @Implementation
  protected void connect() {
    setConnected(true);
  }

  @Implementation
  protected void disconnect() {
    setConnected(false);
  }

  @Implementation
  protected boolean isConnected() {
    return isConnected;
  }

  @Implementation
  protected String getRoot() {
    return ROOT_ID;
  }

  private void setConnected(boolean isConnected) {
    this.isConnected = isConnected;
  }
}
