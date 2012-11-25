package com.xtremelabs.robolectric.bytecode;

import javassist.CtClass;

public interface ClassHandler {
    void instrument(CtClass ctClass);

    void reset();

    void classInitializing(Class clazz);

    Object methodInvoked(Class clazz, String methodName, Object instance, String[] paramTypes, Object[] params) throws Throwable;

    // todo: definitely shouldn't live here
    void setStrictI18n(boolean strictI18n);
}
