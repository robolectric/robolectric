package com.xtremelabs.robolectric;

import com.xtremelabs.robolectric.bytecode.AndroidTranslatorClassInstrumentedTest;
import com.xtremelabs.robolectric.bytecode.Setup;
import javassist.CtClass;
import org.junit.runners.model.InitializationError;

import java.lang.reflect.Method;

import static com.xtremelabs.robolectric.util.TestUtil.resourceFile;

public class TestRunners {
    public static class WithCustomClassList extends RobolectricTestRunner {
        public WithCustomClassList(@SuppressWarnings("rawtypes") Class testClass) throws InitializationError {
            super(RobolectricContext.bootstrap(WithCustomClassList.class, testClass, new RobolectricContext.Factory() {
                @Override
                public RobolectricContext create() {
                    return new RobolectricContext() {
                        @Override
                        protected AndroidManifest createAppManifest() {
                            return new AndroidManifest(resourceFile("TestAndroidManifest.xml"), resourceFile("res"), resourceFile("assets"));
                        }

                        @Override
                        public Setup createSetup() {
                            return new Setup() {
                                @Override
                                public boolean shouldInstrument(CtClass ctClass) {
                                    String name = ctClass.getName();
                                    if (name.equals(AndroidTranslatorClassInstrumentedTest.CustomPaint.class.getName())
                                            || name.equals(AndroidTranslatorClassInstrumentedTest.ClassWithPrivateConstructor.class.getName())) {
                                        return true;
                                    }
                                    return super.shouldInstrument(ctClass);
                                }
                            };
                        }
                    };
                }
            }));
        }
    }

    public static class WithoutDefaults extends RobolectricTestRunner {
        public WithoutDefaults(Class<?> testClass) throws InitializationError {
            super(RobolectricContext.bootstrap(WithoutDefaults.class, testClass, new RobolectricContext.Factory() {
                @Override
                public RobolectricContext create() {
                    return new RobolectricContext();
                }
            }));
        }

        @Override protected void configureShadows(Method testMethod) {
            // Don't do any class binding, because that's what we're trying to test here.
        }

        @Override
        public void setupApplicationState(Method testMethod) {
            // Don't do any resource loading or app init, because that's what we're trying to test here.
        }
    }

    public static class WithDefaults extends RobolectricTestRunner {
        public WithDefaults(Class<?> testClass) throws InitializationError {
            super(RobolectricContext.bootstrap(WithDefaults.class, testClass, new RobolectricContext.Factory() {
                @Override
                public RobolectricContext create() {
                    return new RobolectricContext() {
                        @Override
                        protected AndroidManifest createAppManifest() {
                            return new AndroidManifest(resourceFile("TestAndroidManifest.xml"), resourceFile("res"), resourceFile("assets"));
                        }
                    };
                }
            }));
        }
    }

    public static class RealApisWithDefaults extends RobolectricTestRunner {
        public RealApisWithDefaults(Class<?> testClass) throws InitializationError {
            super(RobolectricContext.bootstrap(RealApisWithDefaults.class, testClass, new RobolectricContext.Factory() {
                @Override
                public RobolectricContext create() {
                    return new RobolectricContext() {
                        @Override
                        protected AndroidManifest createAppManifest() {
                            return new AndroidManifest(resourceFile("TestAndroidManifest.xml"), resourceFile("res"), resourceFile("assets"));
                        }

                        @Override
                        public Setup createSetup() {
                            return new Setup() {
                                @Override
                                public boolean invokeApiMethodBodiesWhenShadowMethodIsMissing(Class clazz, String methodName, Class<?>[] paramClasses) {
                                    return true;
                                }
                            };
                        }
                    };
                }
            }));
        }
    }

    public static class RealApisWithoutDefaults extends RobolectricTestRunner {
        public RealApisWithoutDefaults(Class<?> testClass) throws InitializationError {
            super(RobolectricContext.bootstrap(RealApisWithoutDefaults.class, testClass, new RobolectricContext.Factory() {
                @Override
                public RobolectricContext create() {
                    return new RobolectricContext() {
                        @Override
                        public Setup createSetup() {
                            return new Setup() {
                                @Override
                                public boolean invokeApiMethodBodiesWhenShadowMethodIsMissing(Class clazz, String methodName, Class<?>[] paramClasses) {
                                    return true;
                                }
                            };
                        }
                    };
                }
            }));
        }

        @Override protected void configureShadows(Method testMethod) {
            // Don't do any class binding, because that's what we're trying to test here.
        }

        @Override
        public void setupApplicationState(Method testMethod) {
            // Don't do any resource loading or app init, because that's what we're trying to test here.
        }
    }
}
