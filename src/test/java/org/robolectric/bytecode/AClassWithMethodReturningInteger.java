package org.robolectric.bytecode;

import org.robolectric.internal.Instrument;

@SuppressWarnings("UnusedDeclaration")
@Instrument
public class AClassWithMethodReturningInteger {
    public int normalMethodReturningInteger(int intArg) {
        return intArg + 1;
    }
}
