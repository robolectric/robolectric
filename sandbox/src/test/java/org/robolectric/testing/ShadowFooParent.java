package org.robolectric.testing;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

/** A test class that is a parent for {@link Foo}. */
@Implements(Foo.class)
public class ShadowFooParent {
  @RealObject private Foo realFoo;
  public Foo realFooInParentConstructor;

  @Implementation
  protected void __constructor__(String name) {
    realFooInParentConstructor = realFoo;
  }
}
