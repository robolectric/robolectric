package com.xtremelabs.robolectric;


import org.junit.runners.model.InitializationError;

import static com.xtremelabs.robolectric.util.TestUtil.resourceFile;

public class WithCustomClassListTestRunner extends RobolectricTestRunner {
    @SuppressWarnings("UnusedDeclaration")
    private static RobolectricContext createRobolectricContext() {
        return new RobolectricContext(WithCustomClassListTestRunner.class) {
            @Override
            protected RobolectricConfig createRobolectricConfig() {
                return new RobolectricConfig(resourceFile("TestAndroidManifest.xml"), resourceFile("res"), resourceFile("assets"));
            }
        };
    }

    public WithCustomClassListTestRunner(@SuppressWarnings("rawtypes") Class testClass) throws InitializationError {
        super(RobolectricContext.bootstrap(WithCustomClassListTestRunner.class, testClass));

        addClassOrPackageToInstrument("com.xtremelabs.robolectric.bytecode.AndroidTranslatorClassInstrumentedTest$CustomPaint");
        addClassOrPackageToInstrument("com.xtremelabs.robolectric.bytecode.AndroidTranslatorClassInstrumentedTest$ClassWithPrivateConstructor");
    }
}
