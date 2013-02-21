package org.robolectric.bytecode;

public class DirectObjectMarker {
    public static final DirectObjectMarker INSTANCE = new DirectObjectMarker() {
    };

    private DirectObjectMarker() {
    }
}
