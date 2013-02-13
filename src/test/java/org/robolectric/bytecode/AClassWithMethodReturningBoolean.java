package org.robolectric.bytecode;

import org.robolectric.internal.Instrument;

@SuppressWarnings("UnusedDeclaration")
@Instrument
public class AClassWithMethodReturningBoolean {
    public boolean normalMethodReturningBoolean(boolean boolArg, boolean[] boolArrayArg) {
        return true;
    }
}
