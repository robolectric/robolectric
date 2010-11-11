package com.xtremelabs.robolectric;

import org.junit.runners.model.InitializationError;

import java.lang.reflect.Method;

public class WithoutTestDefaultsRunner extends AbstractRobolectricTestRunner {
    public WithoutTestDefaultsRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    protected void beforeTest(Method method) {
        // Don't do any resource loading or shadow class binding, because that's what we're trying to test here.
    }
}
