package org.robolectric.shadows;

import android.widget.OverScroller;
import org.robolectric.annotation.Implements;

/**
 * The OverScroller shadow base class.
 *
 * <p>The appropriate shadow implementation will be chosen based on the current {@link
 * org.robolectric.annotation.LooperMode}.
 */
@Implements(value = OverScroller.class, shadowPicker = ShadowOverScroller.Picker.class)
public class ShadowOverScroller {

  public static class Picker extends LooperShadowPicker<ShadowOverScroller> {
    public Picker() {
      super(ShadowLegacyOverScroller.class, ShadowPausedOverScroller.class);
    }
  }
}

