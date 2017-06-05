package org.robolectric;

import org.junit.runners.model.InitializationError;

import java.io.File;

import static org.robolectric.util.TestUtil.resourceFile;

public class TestingRobolectricFragmentTestRunner extends RobolectricFragmentTestRunner {
    public TestingRobolectricFragmentTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    @Override
    protected AndroidManifest createAppManifest(File baseDir) {
        return new AndroidManifest(resourceFile("TestAndroidManifest.xml"), resourceFile("res"), resourceFile("assets"));
    }
}
