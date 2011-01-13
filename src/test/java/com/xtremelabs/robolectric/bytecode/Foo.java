package com.xtremelabs.robolectric.bytecode;

import com.xtremelabs.robolectric.internal.Instrument;

@Instrument
public class Foo {
    public Foo(String s) {
        throw new RuntimeException("stub!");
    }

    public String getName() {
//        return null;
        throw new RuntimeException("stub!");
    }

    public void findFooById(int i) {
    }
}
