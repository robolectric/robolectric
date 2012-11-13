package com.xtremelabs.robolectric;

import org.junit.runners.model.InitializationError;

import static com.xtremelabs.robolectric.util.TestUtil.resourceFile;

public class WithTestDefaultsRunner extends RobolectricTestRunner {
    @SuppressWarnings("UnusedDeclaration")
    private static RobolectricContext createRobolectricContext() {
        return new RobolectricContext(WithTestDefaultsRunner.class) {
            @Override
            protected RobolectricConfig createRobolectricConfig() {
                return new RobolectricConfig(resourceFile("TestAndroidManifest.xml"), resourceFile("res"), resourceFile("assets"));
            }
        };
    }

    public WithTestDefaultsRunner(Class<?> testClass) throws InitializationError {
        super(RobolectricContext.bootstrap(WithTestDefaultsRunner.class, testClass));
    }
}
