package org.robolectric.testing;

import org.robolectric.annotation.internal.Instrument;

@SuppressWarnings("UnusedDeclaration")
@Instrument
public class AClassWithFunnyConstructors {
  private final AnUninstrumentedParent uninstrumentedParent;
  private String name;

  public AClassWithFunnyConstructors(String name) {
    this(new AnUninstrumentedParent(name), "foo");
    this.name = name;
  }

  public AClassWithFunnyConstructors(
      AnUninstrumentedParent uninstrumentedParent, String fooString) {
    this.uninstrumentedParent = uninstrumentedParent;
  }
}
