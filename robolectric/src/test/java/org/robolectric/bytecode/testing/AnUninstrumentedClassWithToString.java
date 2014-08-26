package org.robolectric.bytecode.testing;

public class AnUninstrumentedClassWithToString {
  @Override
  public String toString() {
    return "baaaaaah";
  }
}
