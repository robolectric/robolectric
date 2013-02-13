package org.robolectric.bytecode;

import org.robolectric.internal.Instrument;

@Instrument
public class AClassWithEqualsHashCodeToString {
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object obj) {
        return true;
    }

    @Override
    public int hashCode() {
        return 42;
    }

    @Override
    public String toString() {
        return "baaaaaah";
    }
}
