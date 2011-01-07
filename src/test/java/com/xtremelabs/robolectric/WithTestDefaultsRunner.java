package com.xtremelabs.robolectric;

import org.junit.runners.model.InitializationError;

import static com.xtremelabs.robolectric.util.TestUtil.resourceFile;

public class WithTestDefaultsRunner extends RobolectricTestRunner {

    public WithTestDefaultsRunner(Class testClass) throws InitializationError {
        super(testClass, resourceFile("TestAndroidManifest.xml"), resourceFile("res"));
    }
}
