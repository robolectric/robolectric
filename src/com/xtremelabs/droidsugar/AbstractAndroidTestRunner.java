package com.xtremelabs.droidsugar;

import java.lang.reflect.Method;

import org.junit.internal.runners.JUnit4ClassRunner;
import org.junit.runner.notification.RunNotifier;

public class AbstractAndroidTestRunner extends JUnit4ClassRunner {
    private ClassHandler classHandler;

    public AbstractAndroidTestRunner(Class<?> testClass, Loader loader) throws org.junit.internal.runners.InitializationError {
        super(loader.bootstrap(testClass));
    }

    public AbstractAndroidTestRunner(Class<?> testClass, Loader loader, ClassHandler classHandler) throws org.junit.internal.runners.InitializationError {
        this(testClass, loader);
        this.classHandler = classHandler;

        loader.delegateLoadingOf(getClass().getName());
    }

    @Override
    protected void invokeTestMethod(Method method, RunNotifier notifier) {
        if (classHandler != null) classHandler.beforeTest();

        super.invokeTestMethod(method, notifier);

        if (classHandler != null) classHandler.afterTest();
    }
}
