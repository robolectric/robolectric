package org.robolectric.bytecode;

public interface ClassCache {
    byte[] getClassBytesFor(String name);

    boolean isWriting();

    void addClass(String className, byte[] classBytes);
}
