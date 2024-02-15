package org.robolectric.shadows;

import android.os.Build.VERSION_CODES;
import java.util.concurrent.atomic.AtomicBoolean;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow for VirtualInputDevice. */
@Implements(
    className = "android.hardware.input.VirtualInputDevice",
    isInAndroidSdk = false,
    minSdk = VERSION_CODES.UPSIDE_DOWN_CAKE)
public class ShadowVirtualInputDevice {

  private final AtomicBoolean isClosed = new AtomicBoolean(false);

  @Implementation
  protected void close() {
    isClosed.set(true);
  }

  public boolean isClosed() {
    return isClosed.get();
  }
}
