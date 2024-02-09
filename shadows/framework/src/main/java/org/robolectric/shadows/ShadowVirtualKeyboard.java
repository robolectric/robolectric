package org.robolectric.shadows;

import android.hardware.input.VirtualKeyEvent;
import android.hardware.input.VirtualKeyboard;
import android.os.Build.VERSION_CODES;
import java.util.ArrayList;
import java.util.List;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow for VirtualKeyboard. */
@Implements(value = VirtualKeyboard.class, minSdk = VERSION_CODES.TIRAMISU, isInAndroidSdk = false)
public class ShadowVirtualKeyboard extends ShadowVirtualInputDevice {

  private final List<VirtualKeyEvent> sentEvents = new ArrayList<>();

  @Implementation
  protected void sendKeyEvent(VirtualKeyEvent event) {
    sentEvents.add(event);
  }

  public List<VirtualKeyEvent> getSentEvents() {
    return sentEvents;
  }
}
