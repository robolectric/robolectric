package org.robolectric;

import android.app.Application;
import org.junit.Test;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.robolectric.bytecode.Setup;
import org.robolectric.util.Transcript;

import java.lang.reflect.Method;

import static org.robolectric.util.TestUtil.resourceFile;

public class TestRunnerSequenceTest {
    public static Transcript transcript;

    @Test public void shouldRunThingsInTheRightOrder() throws Exception {
        transcript = new Transcript();
        new TestRunnerSequenceTest.Runner(SimpleTest.class).run(new RunNotifier());
        transcript.assertEventsSoFar(
                "configureShadows",
                "resetStaticState",
                "setupApplicationState",
                "createApplication",
                "beforeTest",
                "prepareTest",
                "TEST!",
                "afterTest"
        );
    }

    public static class SimpleTest {
        @Test public void shouldDoNothingMuch() throws Exception {
            transcript.add("TEST!");
        }
    }

    public static class Runner extends RobolectricTestRunner {
        public Runner(Class<?> testClass) throws InitializationError {
            super(RobolectricContext.bootstrap(Runner.class, testClass, new RobolectricContext.Factory() {
                @Override
                public RobolectricContext create() {
                    return new RobolectricContext() {
                        @Override public Setup createSetup() {
                            return new Setup() {
                                @Override public boolean shouldAcquire(String name) {
                                    if (name.equals(TestRunnerSequenceTest.class.getName())) return false;
                                    return super.shouldAcquire(name);
                                }
                            };
                        }

                        @Override protected AndroidManifest createAppManifest() {
                            return new AndroidManifest(resourceFile("TestAndroidManifest.xml"), resourceFile("res"), resourceFile("assets"));
                        }
                    };
                }
            }));
        }

        @Override protected void configureShadows(Method testMethod) {
            transcript.add("configureShadows");
            super.configureShadows(testMethod);
        }

        @Override protected void resetStaticState() {
            transcript.add("resetStaticState");
        }

        @Override
        public void setupApplicationState(Method testMethod) {
            transcript.add("setupApplicationState");
            super.setupApplicationState(testMethod);
        }

        @Override protected Application createApplication() {
            transcript.add("createApplication");
            return super.createApplication();
        }

        @Override public void beforeTest(Method method) {
            transcript.add("beforeTest");
        }

        @Override public void prepareTest(Object test) {
            transcript.add("prepareTest");
            super.prepareTest(test);
        }

        @Override public void afterTest(Method method) {
            transcript.add("afterTest");
        }
    }
}
