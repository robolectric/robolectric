package com.xtremelabs.robolectric;


import static com.xtremelabs.robolectric.util.TestUtil.resourceFile;

import java.util.ArrayList;

import org.junit.runners.model.InitializationError;

import com.xtremelabs.robolectric.bytecode.RobolectricClassLoader;
import com.xtremelabs.robolectric.bytecode.ShadowWrangler;

public class WithCustomClassListTestRunner extends RobolectricTestRunner {

	public WithCustomClassListTestRunner(@SuppressWarnings("rawtypes") Class testClass) throws InitializationError {
		this(testClass, new RobolectricConfig(resourceFile("TestAndroidManifest.xml"), resourceFile("res"), resourceFile("assets")));
	}

	public WithCustomClassListTestRunner(Class<?> testClass, RobolectricConfig robolectricConfig) throws InitializationError {
			super(testClass,
				ShadowWrangler.getInstance(),
				isInstrumented() ? null : new RobolectricClassLoader(ShadowWrangler.getInstance(), populateList()), 
				isInstrumented() ? null : robolectricConfig);
	}
	
	private static ArrayList<String> populateList() {
		ArrayList<String> testList = new ArrayList<String>();
		testList.add("com.xtremelabs.robolectric.bytecode.AndroidTranslatorClassIntrumentedTest$CustomPaint");
		return testList;
	}
}
