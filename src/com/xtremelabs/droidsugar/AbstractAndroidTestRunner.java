package com.xtremelabs.droidsugar;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.lang.reflect.Method;

public class AbstractAndroidTestRunner extends BlockJUnit4ClassRunner {
    private ClassHandler classHandler;
    protected static FrameworkMethod currentFrameworkMethod;

    public AbstractAndroidTestRunner(Class<?> testClass, Loader loader) throws InitializationError {
        super(loader.bootstrap(testClass));
    }

    public AbstractAndroidTestRunner(Class<?> testClass, Loader loader, ClassHandler classHandler) throws InitializationError {
        this(testClass, loader);
        this.classHandler = classHandler;

        loader.delegateLoadingOf(getClass().getName());
    }

    @Override protected Statement methodBlock(final FrameworkMethod method) {
        if (classHandler != null) classHandler.beforeTest();
        beforeTest(method.getMethod());

        final Statement statement = super.methodBlock(method);
        return new Statement() {
            @Override public void evaluate() throws Throwable {
                // todo: this try/finally probably isn't right -- should mimic RunAfters? [xw]
                try {
                    statement.evaluate();
                } finally {
                    afterTest(method.getMethod());
                    if (classHandler != null) classHandler.afterTest();
                }
            }
        };
    }

    protected void beforeTest(Method method) {
    }

    protected void afterTest(Method method) {
    }
}
