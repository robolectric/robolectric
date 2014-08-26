package org.robolectric.bytecode.testing;

import org.robolectric.internal.Instrument;

@SuppressWarnings("UnusedDeclaration")
@Instrument
public class AClassWithNativeMethodReturningPrimitive {
  public native int nativeMethod();
}
