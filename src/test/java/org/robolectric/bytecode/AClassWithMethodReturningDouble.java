package org.robolectric.bytecode;

import org.robolectric.internal.Instrument;

@SuppressWarnings("UnusedDeclaration")
@Instrument
public class AClassWithMethodReturningDouble {
    public double normalMethodReturningDouble(double doubleArg) {
        return doubleArg + 1;
    }
}
