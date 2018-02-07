package org.robolectric.internal.bytecode;

import static org.robolectric.internal.bytecode.MethodCallSite.Kind.STATIC;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

public class MethodCallSite extends RoboCallSite {
  private final String name;
  private final MethodHandle original;
  private final Kind kind;

  public MethodCallSite(Class<?> theClass, MethodType type, String name, MethodHandle original,
      Kind kind) {
    super(type, theClass);
    this.name = name;
    this.original = original;
    this.kind = kind;
  }

  public String getName() {
    return name;
  }

  public MethodHandle getOriginal() {
    return original;
  }

  public Class<?> thisType() {
    return isStatic() ? null : type().parameterType(0);
  }

  public boolean isStatic() {
    return kind == STATIC;
  }

  @Override public String toString() {
    return "RoboCallSite{" +
        "theClass=" + getTheClass() +
        ", original=" + original +
        ", kind=" + kind +
        '}';
  }

  public enum Kind {
    REGULAR,
    STATIC
  }
}
