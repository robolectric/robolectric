package org.robolectric.bytecode;

import org.robolectric.internal.Instrument;

@SuppressWarnings("UnusedDeclaration")
@Instrument
public class AClassThatCallsAMethodReturningAForgettableClass {
    public void callSomeMethod() {
        AClassToForget forgettableClass = getAForgettableClass();
    }

    public AClassToForget getAForgettableClass() {
        throw new RuntimeException("should never be called!");
    }
}
