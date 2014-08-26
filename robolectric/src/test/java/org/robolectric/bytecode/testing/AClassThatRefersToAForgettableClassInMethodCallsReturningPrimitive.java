package org.robolectric.bytecode.testing;

import org.robolectric.internal.Instrument;

@SuppressWarnings("UnusedDeclaration")
@Instrument
public class AClassThatRefersToAForgettableClassInMethodCallsReturningPrimitive {
  byte byteMethod() {
    return AClassToForget.byteReturningMethod();
  }

  byte[] byteArrayMethod() {
    return AClassToForget.byteArrayReturningMethod();
  }

  int intMethod() {
    return AClassToForget.intReturningMethod();
  }

  int[] intArrayMethod() {
    return AClassToForget.intArrayReturningMethod();
  }

  long longMethod() {
    return AClassToForget.longReturningMethod("str", 123, 456);
  }

  long[] longArrayMethod() {
    return AClassToForget.longArrayReturningMethod();
  }
}
