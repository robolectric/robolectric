package com.xtremelabs.robolectric;

import org.junit.runners.model.InitializationError;

import static com.xtremelabs.robolectric.util.TestUtil.resourceFile;

public class WithTestDefaultsRunner extends RobolectricTestRunner {
    public WithTestDefaultsRunner(Class<?> testClass) throws InitializationError {
        super(RobolectricContext.bootstrap(WithTestDefaultsRunner.class, testClass, new RobolectricContext.Factory() {
            @Override
            public RobolectricContext create() {
                return new RobolectricContext() {
                    @Override
                    protected RobolectricConfig createRobolectricConfig() {
                        return new RobolectricConfig(resourceFile("TestAndroidManifest.xml"), resourceFile("res"), resourceFile("assets"));
                    }
                };
            }
        }));
    }
}
