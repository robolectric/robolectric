package org.robolectric.bytecode.testing;

import org.robolectric.internal.Instrument;

@Instrument
public class AnInstrumentedChild extends AnUninstrumentedParent {
  public final String childName;

  public AnInstrumentedChild(String name) {
    super(name.toUpperCase() + "'s child");
    this.childName = name;
  }
}
