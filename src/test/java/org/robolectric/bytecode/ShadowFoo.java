package org.robolectric.bytecode;

import org.robolectric.internal.Implements;
import org.robolectric.internal.RealObject;

@Implements(Foo.class)
public class ShadowFoo extends ShadowWranglerTest.ShadowFooParent {
    @RealObject Foo realFooField;
    Foo realFooInConstructor;

    Foo realFooCtor;

    String name;

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
