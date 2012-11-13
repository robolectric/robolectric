package com.xtremelabs.robolectric.internal;

import org.junit.runners.model.TestClass;

import java.lang.reflect.Method;

public interface RobolectricTestRunnerInterface {
    Object createTest() throws Exception;

    void internalBeforeTest(Method method);

    void internalAfterTest(Method method);

    TestClass getTestClass();
}
