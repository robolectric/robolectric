package org.robolectric.bytecode;

import android.app.Application;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.model.InitializationError;
import org.robolectric.DefaultTestLifecycle;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import org.robolectric.internal.TestLifecycle;

import java.lang.reflect.Method;

import static org.junit.Assert.*;

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
        assertEquals(AsmInstrumentingClassLoader.class.getName(), preparedTest.getClass().getClassLoader().getClass().getName());
    }

    @Test
    public void shouldInvokeBeforeTestWithTheCorrectMethod() throws Exception {
        assertEquals("shouldInvokeBeforeTestWithTheCorrectMethod", testMethod.getName());
    }

    // todo: make this less dumb
    @AfterClass
    public static void shouldHaveCalledAfterTest() {
        assertTrue(beforeCallCount > 0);
        assertEquals(beforeCallCount, afterTestCallCount);
    }

    public static class CustomRobolectricTestRunner extends TestRunners.WithDefaults {
        public CustomRobolectricTestRunner(Class<?> testClass) throws InitializationError {
            super(testClass);
        }

        @Override protected Class<? extends TestLifecycle> getTestLifecycleClass() {
            return MyTestLifecycle.class;
        }

        public static class MyTestLifecycle extends DefaultTestLifecycle {
            @Override public void prepareTest(Object test) {
                ((CustomRobolectricTestRunnerTest) test).preparedTest = test;
            }

            @Override public void beforeTest(Method method) {
                testMethod = method;
            }

            @Override public void afterTest(Method method) {
                afterTestCallCount++;
            }

            @Override public Application createApplication(Method method) {
                return new CustomApplication();
            }
        }
    }

    public static class CustomApplication extends Application {
    }
}
