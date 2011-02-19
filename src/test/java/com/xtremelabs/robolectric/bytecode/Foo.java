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

    public void displayText(CharSequence text, boolean bold) {
        throw new RuntimeException("stub!");
    }

    /** Represents a method overload added to the android API to support internationalization. */
    public void displayText(int textId, boolean bold) {
        throw new RuntimeException("stub!");
    }
}
