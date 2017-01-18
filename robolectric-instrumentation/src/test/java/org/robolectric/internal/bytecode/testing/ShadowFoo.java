package org.robolectric.internal.bytecode.testing;

import org.robolectric.internal.bytecode.ShadowWranglerTest;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

@Implements(Foo.class)
public class ShadowFoo extends ShadowWranglerTest.ShadowFooParent {
  @RealObject public Foo realFooField;
  public Foo realFooInConstructor;
  public String name;

  @Override
  @SuppressWarnings({"UnusedDeclaration"})
  public void __constructor__(String name) {
    super.__constructor__(name);
    this.name = name;
    realFooInConstructor = realFooField;
  }

  @SuppressWarnings({"UnusedDeclaration"})
  public String getName() {
    return name;
  }
}
