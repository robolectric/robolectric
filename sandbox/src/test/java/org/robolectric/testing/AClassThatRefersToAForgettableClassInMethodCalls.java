package org.robolectric.testing;

import org.robolectric.annotation.internal.Instrument;

@SuppressWarnings("UnusedDeclaration")
@Instrument
public class AClassThatRefersToAForgettableClassInMethodCalls {
  AClassToForget aMethod(int a, AClassToForget aClassToForget, String b) {
    return null;
  }

  AClassToForget[] anotherMethod(int a, AClassToForget[] aClassToForget, String b) {
    return null;
  }
}
