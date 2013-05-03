package org.robolectric;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.model.InitializationError;
import org.robolectric.annotation.Config;

import java.util.Arrays;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.reflect.core.Reflection.method;

public class RobolectricTestRunnerTest {
    @Test public void whenClassHasConfigAnnotation_getConfig_shouldMergeClassAndMethodConfig() throws Exception {
        assertConfig(configFor(Test1.class, "withoutAnnotation"),
                1, "foo", "from-test", 2, new Class[]{Test1.class});

        assertConfig(configFor(Test1.class, "withDefaultsAnnotation"),
                1, "foo", "from-test", 2, new Class[]{Test1.class});

        assertConfig(configFor(Test1.class, "withOverrideAnnotation"),
                9, "furf", "from-method", 8, new Class[]{Test1.class, Test2.class});
    }

    @Test public void whenClassDoesntHaveConfigAnnotation_getConfig_shouldUseMethodConfig() throws Exception {
        assertConfig(configFor(Test2.class, "withoutAnnotation"),
                -1, "--default", "", -1, new Class[]{});

        assertConfig(configFor(Test2.class, "withDefaultsAnnotation"),
                -1, "--default", "", -1, new Class[]{});

        assertConfig(configFor(Test2.class, "withOverrideAnnotation"),
                9, "furf", "from-method", 8, new Class[]{Test1.class});
    }

    private Config configFor(Class<?> testClass, String methodName) throws InitializationError {
        return new RobolectricTestRunner(testClass)
                    .getConfig(method(methodName).withParameterTypes().in(testClass).info());
    }

    private void assertConfig(Config config, int emulateSdk, String manifest, String qualifiers, int reportSdk, Class[] shadows) {
        assertThat(stringify(config)).isEqualTo(stringify(emulateSdk, manifest, qualifiers, reportSdk, shadows));
    }

    @Ignore @Config(emulateSdk = 1, manifest = "foo", reportSdk = 2, shadows = Test1.class, qualifiers = "from-test")
    public static class Test1 {
        @Test public void withoutAnnotation() throws Exception {
        }

        @Config
        @Test public void withDefaultsAnnotation() throws Exception {
        }

        @Config(emulateSdk = 9, manifest = "furf", reportSdk = 8, shadows = Test2.class, qualifiers = "from-method")
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

        @Config(emulateSdk = 9, manifest = "furf", reportSdk = 8, shadows = Test1.class, qualifiers = "from-method")
        @Test public void withOverrideAnnotation() throws Exception {
        }
    }

    private String stringify(Config config) {
        int emulateSdk = config.emulateSdk();
        String manifest = config.manifest();
        String qualifiers = config.qualifiers();
        int reportSdk = config.reportSdk();
        Class<?>[] shadows = config.shadows();
        return stringify(emulateSdk, manifest, qualifiers, reportSdk, shadows);
    }

    private String stringify(int emulateSdk, String manifest, String qualifiers, int reportSdk, Class<?>[] shadows) {
        return "emulateSdk=" + emulateSdk + "\n" +
                "manifest=" + manifest + "\n" +
                "qualifiers=" + qualifiers + "\n" +
                "reportSdk=" + reportSdk + "\n" +
                "shadows=" + Arrays.toString(shadows);
    }
}
