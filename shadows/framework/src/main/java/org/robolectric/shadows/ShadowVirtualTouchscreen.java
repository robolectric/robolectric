package org.robolectric.shadows;

import android.hardware.input.VirtualTouchEvent;
import android.hardware.input.VirtualTouchscreen;
import android.os.Build.VERSION_CODES;
import java.util.ArrayList;
import java.util.List;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow for VirtualTouchscreen. */
@Implements(
    value = VirtualTouchscreen.class,
    minSdk = VERSION_CODES.TIRAMISU,
    isInAndroidSdk = false)
public class ShadowVirtualTouchscreen extends ShadowVirtualInputDevice {

  private final List<VirtualTouchEvent> sentEvents = new ArrayList<>();

  @Implementation
  protected void sendTouchEvent(VirtualTouchEvent event) {
    sentEvents.add(event);
  }

  public List<VirtualTouchEvent> getSentEvents() {
    return sentEvents;
  }
}
