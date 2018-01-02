package org.robolectric.testing;

import org.robolectric.ShadowWranglerIntegrationTest;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

@Implements(Foo.class)
public class ShadowFoo extends ShadowWranglerIntegrationTest.ShadowFooParent {
  @RealObject public Foo realFooField;
  public Foo realFooInConstructor;
  public String name;

  @Override
  @Implementation
  protected void __constructor__(String name) {
    super.__constructor__(name);
    this.name = name;
    realFooInConstructor = realFooField;
  }

  @SuppressWarnings({"UnusedDeclaration"})
  public String getName() {
    return name;
  }
}
