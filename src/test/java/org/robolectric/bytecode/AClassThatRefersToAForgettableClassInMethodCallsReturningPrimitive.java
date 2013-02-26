package org.robolectric.bytecode;

import org.robolectric.internal.Instrument;

@SuppressWarnings("UnusedDeclaration")
@Instrument
public class AClassThatRefersToAForgettableClassInMethodCallsReturningPrimitive {
    byte byteMethod() {
        return AClassToForget.byteReturningMethod();
    }

    byte[] byteArrayMethod() {
        return AClassToForget.byteArrayReturningMethod();
    }

    int intMethod() {
        return AClassToForget.intReturningMethod();
    }

    int[] intArrayMethod() {
        return AClassToForget.intArrayReturningMethod();
    }
    
    long longMethod() {
        return AClassToForget.longReturningMethod();
    }

    long[] longArrayMethod() {
        return AClassToForget.longArrayReturningMethod();
    }
}
