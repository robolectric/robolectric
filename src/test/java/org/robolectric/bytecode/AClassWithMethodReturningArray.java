package org.robolectric.bytecode;

import org.robolectric.internal.Instrument;

@SuppressWarnings("UnusedDeclaration")
@Instrument
public class AClassWithMethodReturningArray {
    public String[] normalMethodReturningArray() {
        return new String[] { "hello, working!" };
    }
}
