package org.robolectric.bytecode.testing;

import org.robolectric.internal.Instrument;

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
