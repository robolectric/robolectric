package org.robolectric.internal.bytecode;

import java.lang.invoke.MethodType;
import java.lang.invoke.MutableCallSite;

public class RoboCallSite extends MutableCallSite {
  private final Class<?> caller;

  public RoboCallSite(MethodType type, Class<?> caller) {
    super(type);
    this.caller = caller;
  }

  public Class<?> getCaller() {
    return caller;
  }
}
