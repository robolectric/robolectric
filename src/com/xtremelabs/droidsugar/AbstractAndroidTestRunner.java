package com.xtremelabs.droidsugar;

import org.junit.Test;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

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

        if (currentFrameworkMethod != null) throw new IllegalStateException("current framework method is already set!");
        currentFrameworkMethod = method;
        final Statement statement = super.methodBlock(method);
        return new Statement() {
            @Override public void evaluate() throws Throwable {
                // todo: this try/finally probably isn't right -- should mimic RunAfters? [xw]
                try {
                    statement.evaluate();
                } finally {
                    afterTest(method.getMethod());
                    if (classHandler != null) classHandler.afterTest();
                    currentFrameworkMethod = null;
                }
            }
        };
    }

    protected void beforeTest(Method method) {
    }

    protected void afterTest(Method method) {
    }

    @Override protected Object createTest() throws Exception {
        Class<?> declaringClass = AbstractAndroidTestRunner.currentFrameworkMethod.getMethod().getDeclaringClass();
        Object test;
        if (declaringClass.isMemberClass()) {
            Object enclosingTest = declaringClass.getEnclosingClass().newInstance();
            test = declaringClass.getConstructor(enclosingTest.getClass()).newInstance(enclosingTest);
        } else {
            test = declaringClass.newInstance();
        }
        System.out.println("created test = " + test);
        return test;
    }

    @Override protected List<FrameworkMethod> getChildren() {
        List<FrameworkMethod> children = super.getChildren();
        ArrayList<FrameworkMethod> mine = new ArrayList<FrameworkMethod>();
        mine.addAll(children);
        Class<?>[] innerClasses = getTestClass().getJavaClass().getClasses();
        for (final Class<?> innerClass : innerClasses) {
            TestClass testClass = new TestClass(innerClass);
            List<FrameworkMethod> methods = testClass.getAnnotatedMethods(Test.class);
            for (FrameworkMethod method : methods) {
                mine.add(new FrameworkMethod(method.getMethod()) {
                    @Override public String getName() {
                        return innerClass.getSimpleName() + "." + super.getName();
                    }
                });
            }
        }
        return mine;
    }

//    @Override
//    protected Description describeChild(FrameworkMethod method) {
//        return Description.createTestDescription(getTestClass().getJavaClass(),
//                testName(method), method.getAnnotations());
//    }
}
