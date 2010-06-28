package com.xtremelabs.droidsugar;

import javassist.CtClass;

public interface ClassHandler {
    void instrument(CtClass ctClass);

    void beforeTest();

    void afterTest();

    Object methodInvoked(String className, String methodName, Object instance, String[] paramTypes, Object[] params);
}
