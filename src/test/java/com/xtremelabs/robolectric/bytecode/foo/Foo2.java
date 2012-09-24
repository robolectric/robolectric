package com.xtremelabs.robolectric.bytecode.foo;

import com.xtremelabs.robolectric.bytecode.Foo;
import com.xtremelabs.robolectric.internal.Instrument;

@Instrument
public class Foo2 {
    
    public boolean crossCallFooDirectly(Foo foo) {
        return deeperCall(foo);
    }

    protected boolean deeperCall(Foo foo) {
        return foo.callInvokedDirectly();
    }
    
}
