package com.xtremelabs.robolectric;

import org.junit.runners.model.InitializationError;

import java.lang.reflect.Method;

public class RobolectricTestRunner extends AbstractRobolectricTestRunner {
    public RobolectricTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    @Override
    protected void beforeTest(Method method) {
        Robolectric.bindDefaultShadowClasses();

        super.beforeTest(method);
    }
}
