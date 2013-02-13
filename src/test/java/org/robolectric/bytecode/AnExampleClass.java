package org.robolectric.bytecode;

import org.robolectric.internal.Instrument;

@SuppressWarnings("UnusedDeclaration")
@Instrument
public class AnExampleClass {
    static int foo = 123;

    public String normalMethod(String stringArg, int intArg) {
        return "normalMethod(" + stringArg + ", " + intArg + ")";
    }

    //        abstract void abstractMethod(); todo
}
