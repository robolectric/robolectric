package org.robolectric.internal.bytecode.testing;

/**
 * Created by nikolasr on 2/9/16.
 */
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
