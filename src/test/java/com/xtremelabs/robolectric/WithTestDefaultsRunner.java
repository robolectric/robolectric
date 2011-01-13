package com.xtremelabs.robolectric;

import org.junit.runners.model.InitializationError;

import static com.xtremelabs.robolectric.util.TestUtil.resourceFile;

public class WithTestDefaultsRunner extends RobolectricTestRunner {

    public WithTestDefaultsRunner(Class testClass) throws InitializationError {
        super(testClass, new RobolectricConfig(resourceFile("TestAndroidManifest.xml"), resourceFile("res"), resourceFile("assets")));
    }
}
