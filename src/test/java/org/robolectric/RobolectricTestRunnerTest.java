package org.robolectric;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.model.InitializationError;
import org.robolectric.annotation.Config;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.reflect.core.Reflection.method;

public class RobolectricTestRunnerTest {
    @Test public void getConfig_shouldMergeClassAndMethodConfig() throws Exception {
        assertConfig(configFor(Test1.class, "withoutAnnotation"),
                1, 2, new Class[]{Test1.class}, "from-test");

        assertConfig(configFor(Test1.class, "withDefaultsAnnotation"),
                1, 2, new Class[]{Test1.class}, "from-test");

        assertConfig(configFor(Test1.class, "withOverrideAnnotation"),
                9, 8, new Class[]{Test1.class, Test2.class}, "from-method");
    }

    private Config configFor(Class<?> testClass, String methodName) throws InitializationError {
        return new RobolectricTestRunner(testClass)
                    .getConfig(method(methodName).withParameterTypes().in(Test1.class).info());
    }

    private void assertConfig(Config config, int emulateSdk, int reportSdk, Class[] shadows, String qualifiers) {
        assertThat(config.emulateSdk()).isEqualTo(emulateSdk);
        assertThat(config.reportSdk()).isEqualTo(reportSdk);
        assertThat(config.shadows()).isEqualTo(shadows);
        assertThat(config.qualifiers()).isEqualTo(qualifiers);
    }

    @Ignore @Config(emulateSdk = 1, reportSdk = 2, shadows = Test1.class, qualifiers = "from-test")
    public static class Test1 {
        @Test public void withoutAnnotation() throws Exception {
        }

        @Config
        @Test public void withDefaultsAnnotation() throws Exception {
        }

        @Config(emulateSdk = 9, reportSdk = 8, shadows = Test2.class, qualifiers = "from-method")
        @Test public void withOverrideAnnotation() throws Exception {
        }
    }

    @Ignore
    public static class Test2 {
        @Test public void withoutAnnotation() throws Exception {
        }

        @Config
        @Test public void withDefaultsAnnotation() throws Exception {
        }

        @Config(emulateSdk = 9, reportSdk = 8, shadows = Test1.class, qualifiers = "from-method")
        @Test public void withOverrideAnnotation() throws Exception {
        }
    }
}

