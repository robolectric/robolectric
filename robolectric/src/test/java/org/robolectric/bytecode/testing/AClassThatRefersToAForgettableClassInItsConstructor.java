package org.robolectric.bytecode.testing;

import org.robolectric.internal.Instrument;

@SuppressWarnings("UnusedDeclaration")
@Instrument
public class AClassThatRefersToAForgettableClassInItsConstructor {
  public final AClassToForget aClassToForget;

  public AClassThatRefersToAForgettableClassInItsConstructor() {
    aClassToForget = null;
  }
}
