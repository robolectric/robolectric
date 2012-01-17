package com.xtremelabs.robolectric;


import org.junit.runners.model.InitializationError;

import static com.xtremelabs.robolectric.util.TestUtil.resourceFile;

public class WithCustomClassListTestRunner extends RobolectricTestRunner {

	public WithCustomClassListTestRunner(@SuppressWarnings("rawtypes") Class testClass) throws InitializationError {
        super(testClass, new RobolectricConfig(resourceFile("TestAndroidManifest.xml"), resourceFile("res"), resourceFile("assets")));

        addClassOrPackageToInstrument("com.xtremelabs.robolectric.bytecode.AndroidTranslatorClassIntrumentedTest$CustomPaint");
        addClassOrPackageToInstrument("com.xtremelabs.robolectric.bytecode.AndroidTranslatorClassIntrumentedTest$ClassWithPrivateConstructor");
	}
}
