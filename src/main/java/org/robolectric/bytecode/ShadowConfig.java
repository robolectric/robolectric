package org.robolectric.bytecode;

public class ShadowConfig {
    public final String shadowClassName;
    public final boolean callThroughByDefault;

    ShadowConfig(String shadowClassName, boolean callThroughByDefault) {
        this.callThroughByDefault = callThroughByDefault;
        this.shadowClassName = shadowClassName;
    }
}
