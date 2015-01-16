package org.robolectric.bytecode.testing;

import org.robolectric.bytecode.ShadowWranglerTest;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

@Implements(Foo.class)
public class ShadowFoo extends ShadowWranglerTest.ShadowFooParent {
  @RealObject public Foo realFooField;
  public Foo realFooInConstructor;
  public Foo realFooCtor;
  public String name;

  public ShadowFoo(Foo foo) {
    this.realFooCtor = foo;
  }

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
