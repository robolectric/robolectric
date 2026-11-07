package org.robolectric.testing;

import java.util.Locale;
import org.robolectric.annotation.internal.Instrument;

@Instrument
public class AnInstrumentedChild extends AnUninstrumentedParent {
  public final String childName;

  public AnInstrumentedChild(String name) {
    super(name.toUpperCase(Locale.ROOT) + "'s child");
    this.childName = name;
  }
}
