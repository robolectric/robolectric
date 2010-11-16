package com.xtremelabs.robolectric;

import org.junit.runners.model.InitializationError;

import java.lang.reflect.Method;

public class WithoutTestDefaultsRunner extends RobolectricTestRunner {
    public WithoutTestDefaultsRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    public WithoutTestDefaultsRunner(Class<?> testClass, ClassHandler classHandler, String projectRoot, String resourceDirectory) throws InitializationError {
        super(testClass, classHandler, projectRoot, resourceDirectory);
    }

    @Override public void internalBeforeTest(Method method) {
        // Don't do any resource loading or shadow class binding, because that's what we're trying to test here.
    }
}
