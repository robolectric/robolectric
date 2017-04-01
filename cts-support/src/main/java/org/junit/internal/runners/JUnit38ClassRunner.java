package org.junit.internal.runners;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.robolectric.cts.CtsRobolectricTestRunner;

public class JUnit38ClassRunner extends Runner {
    private volatile Test test;
    private CtsRobolectricTestRunner ctsRobolectricTestRunner;

    @SuppressWarnings("unused")
    public JUnit38ClassRunner(Class<?> klass) {
        this(new TestSuite(klass.asSubclass(TestCase.class)));
        try {
            ctsRobolectricTestRunner = new CtsRobolectricTestRunner(klass);
        } catch (org.junit.runners.model.InitializationError initializationError) {
            throw new RuntimeException(initializationError);
        }
    }

    public JUnit38ClassRunner(Test test) {
        super();
    }

    @Override
    public Description getDescription() {
        return ctsRobolectricTestRunner.getDescription();
    }

    @Override
    public void run(RunNotifier notifier) {
        ctsRobolectricTestRunner.run(notifier);
    }
}
