package com.xtremelabs.robolectric;


import org.junit.runners.model.InitializationError;

import static com.xtremelabs.robolectric.util.TestUtil.resourceFile;

public class WithCustomClassListTestRunner extends RobolectricTestRunner {
    public WithCustomClassListTestRunner(@SuppressWarnings("rawtypes") Class testClass) throws InitializationError {
        super(RobolectricContext.bootstrap(WithCustomClassListTestRunner.class, testClass, new RobolectricContext.Factory() {
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

        addClassOrPackageToInstrument("com.xtremelabs.robolectric.bytecode.AndroidTranslatorClassInstrumentedTest$CustomPaint");
        addClassOrPackageToInstrument("com.xtremelabs.robolectric.bytecode.AndroidTranslatorClassInstrumentedTest$ClassWithPrivateConstructor");
    }
}
