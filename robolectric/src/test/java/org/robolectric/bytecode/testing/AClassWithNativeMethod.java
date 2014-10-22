package org.robolectric.bytecode.testing;

import org.robolectric.internal.Instrument;

@SuppressWarnings("UnusedDeclaration")
@Instrument
public class AClassWithNativeMethod {
  public native String nativeMethod(String stringArg, int intArg);
}
