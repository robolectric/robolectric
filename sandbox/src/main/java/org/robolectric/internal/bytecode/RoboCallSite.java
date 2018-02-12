package org.robolectric.internal.bytecode;

import java.lang.invoke.MethodType;
import java.lang.invoke.MutableCallSite;

public class RoboCallSite extends MutableCallSite {
  private final Class<?> theClass;

  public RoboCallSite(MethodType type, Class<?> theClass) {
    super(type);
    this.theClass = theClass;
  }

  public Class<?> getTheClass() {
    return theClass;
  }
}
