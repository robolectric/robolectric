package com.xtremelabs.robolectric;

import static com.xtremelabs.robolectric.util.TestUtil.resourceFile;

import org.junit.runners.model.InitializationError;

import com.xtremelabs.robolectric.bytecode.ClassHandler;
import com.xtremelabs.robolectric.bytecode.RobolectricClassLoader;
import com.xtremelabs.robolectric.util.DatabaseConfig.DatabaseMap;

public class WithTestDefaultsRunner extends RobolectricTestRunner {
	
	public WithTestDefaultsRunner(Class<?> testClass) throws InitializationError {
        super(testClass, new RobolectricConfig(resourceFile("TestAndroidManifest.xml"), resourceFile("res"), resourceFile("assets")));
    }
	public WithTestDefaultsRunner(Class<?> testClass,DatabaseMap databaseMap) throws InitializationError {
		super(testClass, new RobolectricConfig(resourceFile("TestAndroidManifest.xml"), resourceFile("res"), resourceFile("assets")), databaseMap);
	}

}
