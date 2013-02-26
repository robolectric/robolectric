package org.robolectric.bytecode;

public class AnUninstrumentedClassWithToString {
    @Override
    public String toString() {
        return "baaaaaah";
    }
}
