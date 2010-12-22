package com.xtremelabs.robolectric;

import org.junit.runners.model.InitializationError;

import java.io.File;

public class WithTestDefaultsRunner extends RobolectricTestRunner {
    private static File testDirLocation;

    public WithTestDefaultsRunner(Class testClass) throws InitializationError {
        super(testClass, new File(baseDir(), "TestAndroidManifest.xml"), new File(baseDir(), "res"));
    }

    private static File baseDir() {
        if (testDirLocation == null) {
            testDirLocation = new File("test");
            if (!testDirLocation.isDirectory()) {
                testDirLocation = new File("robolectric", "test");
            }
            if (!testDirLocation.isDirectory()) {
                throw new RuntimeException("can't find your TestAndroidManifest.xml");
            }
        }
        return testDirLocation;
    }
}
