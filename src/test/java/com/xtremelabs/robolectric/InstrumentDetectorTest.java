package com.xtremelabs.robolectric;

import com.xtremelabs.robolectric.RobolectricTestRunner.InstrumentDetector;
import com.xtremelabs.robolectric.bytecode.ClassHandler;
import com.xtremelabs.robolectric.bytecode.RobolectricClassLoader;
import com.xtremelabs.robolectric.bytecode.ShadowWrangler;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runners.model.InitializationError;

import java.io.PrintWriter;
import java.io.StringWriter;

import static com.xtremelabs.robolectric.util.TestUtil.resourceFile;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Tests related to custom instrument detection strategy
 * that is used for introducing custom class loaders to test runners.
 */
public class InstrumentDetectorTest {

    // ==================== CLASSLOADER ====================

    /** Custom class loader. It must be singleton. */
    public static class ClassLoaderForTesting extends RobolectricClassLoader {

        /** Instance. */
        private static ClassLoaderForTesting instance;

        /** Usage flag. */
        static boolean usageFlag = false;

        public static ClassLoaderForTesting getInstance() {
            if (instance == null) {
                instance = new ClassLoaderForTesting(ShadowWrangler.getInstance());
            }
            return instance;
        }

        protected ClassLoaderForTesting(final ClassHandler classHandler) {
            super(classHandler);
        }

        @Override
        public Class<?> loadClass(final String name) throws ClassNotFoundException {
            if (!usageFlag) {
                System.setProperty("robolectric.cusomClassLoader", "yes");
            }
            usageFlag = true;
            return super.loadClass(name);
        }

    }

    // ==================== RUNNERS ====================

    /** Custom runner that uses custom class loader but does not modify instrument detection.  */
    public static class RunnerForTesting extends RobolectricTestRunner {

        public RunnerForTesting(final Class<?> testClass) throws InitializationError {
            super(
                    testClass,
                    isInstrumented() ? null : ShadowWrangler.getInstance(),
                    isInstrumented() ? null : ClassLoaderForTesting.getInstance(),
                    new RobolectricConfig(resourceFile("TestAndroidManifest.xml"), resourceFile("res"), resourceFile("assets"))
                  );
        }

    }

    /** Custom runner that uses custom class loader.  */
    public static class RunnerForTestingThatWillPass extends RobolectricTestRunner {

        static {
            RunnerForTestingThatWillPass.setInstrumentDetector(CUSTOM_DETECTOR);
        }

        public RunnerForTestingThatWillPass(final Class<?> testClass) throws InitializationError {
            super(
                    testClass,
                    isInstrumented() ? null : ShadowWrangler.getInstance(),
                    isInstrumented() ? null : ClassLoaderForTesting.getInstance(),
                    new RobolectricConfig(resourceFile("TestAndroidManifest.xml"), resourceFile("res"), resourceFile("assets"))
                  );
        }

    }

    /** Custom instrument detector. */
    private static final InstrumentDetector CUSTOM_DETECTOR = new InstrumentDetector() {
        @Override
        public boolean isInstrumented() {
            final String currentLoader = RunnerForTestingThatWillPass.class.getClassLoader().getClass().getName();
            return currentLoader.contains(ClassLoaderForTesting.class.getName());
        }
    };


    // ==================== TEST CLASSES ====================

    /**
     * Default behavior test.
     */
    public static class DefaultBehaviorTest {
        @Test
        public void fakeTest() {
            assertThat(true, equalTo(true));
        }
    }

    /**
     * Test that is launched with a custom test runner and will fail.
     */
    @RunWith(RunnerForTesting.class)
    public static class TestWithCustomRunner {
        @Test
        public void loadedWithCustomClassLoader_usageFlagShoudBeTrue() {
            assertThat(System.getProperty("robolectric.cusomClassLoader"), equalTo("yes"));
        }
    }

    /**
     * Test that is launched with a custom test runner and will pass.
     */
    @RunWith(RunnerForTestingThatWillPass.class)
    public static class TestWithCustomRunnerThatWillPass extends TestWithCustomRunner {
        // nothing here, we just need another annotation
    }


    // ==================== RUN METHODS ====================

    /**
     * This is default behavior.
     */
    @Test
    public void withDefaultDetectorAndDefaultRunner_shouldPass() {
        final Result result =  JUnitCore.runClasses(DefaultBehaviorTest.class);
        assertThat(result.getRunCount(), equalTo(1));
        assertThat(result.getFailureCount(), equalTo(0));
    }

    /**
     * This test simulates wrong instrument detection when custom class loader is used.
     */
    @Test
    public void wrongInstrumentDetection_shouldRaiseLinkageError() {
        final Result result =  JUnitCore.runClasses(TestWithCustomRunner.class);
        assertThat(result.getRunCount(), equalTo(1));
        assertThat(result.getFailureCount(), equalTo(1));

        // check whether it's a linkage error
        final StringWriter buffer = new StringWriter();
        result.getFailures().get(0).getException().printStackTrace(new PrintWriter(buffer));
        final int linkageErrorNameIndex = buffer.toString().indexOf(LinkageError.class.getName());
        assertTrue(linkageErrorNameIndex >= 0);
    }

    /**
     * Propper behavior test.
     */
    @Test
    public void customizeInstumentDetection_shouldPass() {
        final Result result =  JUnitCore.runClasses(TestWithCustomRunnerThatWillPass.class);
        assertThat(result.getRunCount(), equalTo(1));
        assertThat(result.getFailureCount(), equalTo(0));
    }

}
