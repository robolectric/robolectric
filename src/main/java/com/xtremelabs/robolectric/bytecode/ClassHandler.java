package com.xtremelabs.robolectric.bytecode;

import com.xtremelabs.robolectric.RobolectricConfig;

import javassist.CtClass;

public interface ClassHandler {
	void configure(RobolectricConfig robolectricConfig);
	
    void instrument(CtClass ctClass);

    void beforeTest();

    void afterTest();

    Object methodInvoked(Class clazz, String methodName, Object instance, String[] paramTypes, Object[] params) throws Throwable;
}
