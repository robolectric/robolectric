package org.robolectric.testing;

import org.robolectric.annotation.internal.Instrument;

@SuppressWarnings("UnusedDeclaration")
@Instrument
public class AClassThatRefersToAForgettableClassInItsConstructor {
  public final AClassToForget aClassToForget;

  public AClassThatRefersToAForgettableClassInItsConstructor() {
    aClassToForget = null;
  }
}
