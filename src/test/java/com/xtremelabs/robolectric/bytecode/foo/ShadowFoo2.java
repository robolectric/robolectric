package com.xtremelabs.robolectric.bytecode.foo;

import com.xtremelabs.robolectric.bytecode.Foo;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(Foo2.class)
public class ShadowFoo2 {

    @Implementation
    public boolean crossCallFooDirectly(Foo foo) {
        return false;
    }
    
}
