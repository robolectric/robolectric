package org.robolectric.shadows;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.widget.OverScroller;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * The OverScroller shadow base class.
 *
 * The appropriate shadow implementation will be chosen based on the current
 * {@link org.robolectric.annotation.LooperMode}.
 */
@Implements(value = OverScroller.class, shadowPicker = ShadowOverScroller.Picker.class)
public class ShadowOverScroller {

  public static class Picker extends LooperShadowPicker<ShadowOverScroller> {
    public Picker() {
      super(ShadowLegacyOverScroller.class, ShadowPausedOverScroller.class);
    }
  }
}

