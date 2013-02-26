package org.robolectric.bytecode;

import org.robolectric.internal.DoNotInstrument;

@DoNotInstrument
public class AnUninstrumentedParent {
    public final String parentName;

    public AnUninstrumentedParent(String name) {
        this.parentName = name;
    }

    @Override
    public String toString() {
        return "UninstrumentedParent{parentName='" + parentName + '\'' + '}';
    }
}
