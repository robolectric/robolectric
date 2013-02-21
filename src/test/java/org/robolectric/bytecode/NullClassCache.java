package org.robolectric.bytecode;

class NullClassCache implements ClassCache {
    @Override
    public byte[] getClassBytesFor(String name) {
        return null;
    }

    @Override
    public boolean isWriting() {
        return false;
    }

    @Override
    public void addClass(String className, byte[] classBytes) {
    }
}
