package org.robolectric.bytecode;

public class ShadowConfig {
    public final String shadowClassName;
    public final boolean callThroughByDefault;

    ShadowConfig(String shadowClassName, boolean callThroughByDefault) {
        this.callThroughByDefault = callThroughByDefault;
        this.shadowClassName = shadowClassName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ShadowConfig that = (ShadowConfig) o;

        if (callThroughByDefault != that.callThroughByDefault) return false;
        if (!shadowClassName.equals(that.shadowClassName)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = shadowClassName.hashCode();
        result = 31 * result + (callThroughByDefault ? 1 : 0);
        return result;
    }
}
