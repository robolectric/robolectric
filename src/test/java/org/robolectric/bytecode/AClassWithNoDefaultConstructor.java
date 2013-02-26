package org.robolectric.bytecode;

import org.robolectric.internal.Instrument;

@SuppressWarnings("UnusedDeclaration")
@Instrument
class AClassWithNoDefaultConstructor {
    private String name;

    AClassWithNoDefaultConstructor(String name) {
        this.name = name;
    }
}
