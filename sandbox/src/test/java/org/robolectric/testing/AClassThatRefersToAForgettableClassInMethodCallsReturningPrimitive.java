package org.robolectric.testing;

import org.robolectric.annotation.internal.Instrument;

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

  float floatMethod() {
    return AClassToForget.floatReturningMethod();
  }

  float[] floatArrayMethod() {
    return AClassToForget.floatArrayReturningMethod();
  }

  double doubleMethod() {
    return AClassToForget.doubleReturningMethod();
  }

  double[] doubleArrayMethod() {
    return AClassToForget.doubleArrayReturningMethod();
  }

  short shortMethod() {
    return AClassToForget.shortReturningMethod();
  }

  short[] shortArrayMethod() {
    return AClassToForget.shortArrayReturningMethod();
  }

  void voidReturningMethod() {
    AClassToForget.voidReturningMethod();
  }
}
