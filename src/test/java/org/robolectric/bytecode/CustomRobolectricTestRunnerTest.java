package org.robolectric.bytecode;

import android.app.Application;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.model.InitializationError;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(CustomRobolectricTestRunnerTest.CustomRobolectricTestRunner.class)
public class CustomRobolectricTestRunnerTest {
    Object preparedTest;
    static Method testMethod;
    static int beforeCallCount = 0;
    static int afterTestCallCount = 0;

    @Before
    public void setUp() throws Exception {
        beforeCallCount++;
    }

    @Test
    public void shouldInitializeApplication() throws Exception {
        assertNotNull(Robolectric.application);
        assertEquals(CustomApplication.class, Robolectric.application.getClass());
    }

    @Test
    public void shouldInvokePrepareTestWithAnInstanceOfTheTest() throws Exception {
        assertEquals(this, preparedTest);
        assertEquals(JavassistInstrumentingClassLoader.class.getName(), preparedTest.getClass().getClassLoader().getClass().getName());
    }

    @Test
    public void shouldInvokeBeforeTestWithTheCorrectMethod() throws Exception {
        assertEquals("shouldInvokeBeforeTestWithTheCorrectMethod", testMethod.getName());
    }

    @AfterClass
    public static void shouldHaveCalledAfterTest() {
        assertTrue(beforeCallCount > 0);
        assertEquals(beforeCallCount, afterTestCallCount);
    }

    public static class CustomRobolectricTestRunner extends TestRunners.WithDefaults {
        public CustomRobolectricTestRunner(Class<?> testClass) throws InitializationError {
            super(testClass);
        }

        @Override public void prepareTest(Object test) {
            ((CustomRobolectricTestRunnerTest) test).preparedTest = test;
        }

        @Override public void beforeTest(Method method) {
            testMethod = method;
        }

        @Override public void afterTest(Method method) {
            afterTestCallCount++;
        }

        @Override protected Application createApplication() {
            return new CustomApplication();
        }
    }

    public static class CustomApplication extends Application {
    }
}
