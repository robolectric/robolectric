package com.xtremelabs.robolectric.bytecode;

import com.xtremelabs.robolectric.internal.Instrument;

@Instrument
public class Foo {
    public Foo(String s) {
        throw new RuntimeException("stub!");
    }
    
    public String getName() {
        throw new RuntimeException("stub!");
    }

    public void findFooById(int i) {
        throw new RuntimeException("stub!");
    }
    
    /* package */ boolean invokedDirectly() {
        return true;
    }
    
    public String callGetName() {
        return getName();
    }
    
    public boolean callInvokedDirectly() {
        return invokedDirectly();
    }
}
