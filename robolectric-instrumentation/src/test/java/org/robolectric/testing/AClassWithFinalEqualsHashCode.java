package org.robolectric.testing;

public class AClassWithFinalEqualsHashCode {
  @Override
  public final int hashCode() {
    return super.hashCode();
  }

  @Override
  public final boolean equals(Object obj) {
    return super.equals(obj);
  }
}
