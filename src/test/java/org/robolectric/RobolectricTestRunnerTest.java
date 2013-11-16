package org.robolectric;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.model.InitializationError;
import org.robolectric.annotation.Config;
import org.robolectric.res.PackageResourceLoader;
import org.robolectric.res.ResourceLoader;
import org.robolectric.res.ResourcePath;
import org.robolectric.shadows.ShadowView;
import org.robolectric.shadows.ShadowViewGroup;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Properties;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.reflect.core.Reflection.method;

public class RobolectricTestRunnerTest {
  @Test public void whenClassHasConfigAnnotation_getConfig_shouldMergeClassAndMethodConfig() throws Exception {
    assertConfig(configFor(Test1.class, "withoutAnnotation"),
        1, "foo", "from-test", new Class[]{Test1.class});

    assertConfig(configFor(Test1.class, "withDefaultsAnnotation"),
        1, "foo", "from-test", new Class[]{Test1.class});

    assertConfig(configFor(Test1.class, "withOverrideAnnotation"),
        9, "furf", "from-method", new Class[]{Test1.class, Test2.class});
  }

  @Test public void whenClassDoesntHaveConfigAnnotation_getConfig_shouldUseMethodConfig() throws Exception {
    assertConfig(configFor(Test2.class, "withoutAnnotation"),
        -1, "--default", "", new Class[]{});

    assertConfig(configFor(Test2.class, "withDefaultsAnnotation"),
        -1, "--default", "", new Class[]{});

    assertConfig(configFor(Test2.class, "withOverrideAnnotation"),
        9, "furf", "from-method", new Class[]{Test1.class});
  }

  @Test public void shouldLoadDefaultsFromPropertiesFile() throws Exception {
    Properties properties = properties(
        "sdk: 432\n" +
            "manifest: --none\n" +
            "qualifiers: from-properties-file\n" +
            "shadows: org.robolectric.shadows.ShadowView, org.robolectric.shadows.ShadowViewGroup\n");
    assertConfig(configFor(Test2.class, "withoutAnnotation", properties),
        432, "--none", "from-properties-file", new Class[] {ShadowView.class, ShadowViewGroup.class});
  }

  @Test public void withEmptyShadowList_shouldLoadDefaultsFromPropertiesFile() throws Exception {
    Properties properties = properties("shadows:");
    assertConfig(configFor(Test2.class, "withoutAnnotation", properties),
        -1, "--default", "", new Class[] {});
  }

  @Test public void rememberThatSomeTestRunnerMethodsShouldBeOverridable() throws Exception {
    // super weak test for now, just remember not to make these methods static!

    //noinspection UnusedDeclaration
    class CustomTestRunner extends RobolectricTestRunner {
      public CustomTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
      }

      @Override public PackageResourceLoader createResourceLoader(ResourcePath resourcePath) {
        return super.createResourceLoader(resourcePath);
      }

      @Override
      protected ResourceLoader createAppResourceLoader(ResourceLoader systemResourceLoader,
          AndroidManifest appManifest) {
        return super.createAppResourceLoader(systemResourceLoader, appManifest);
      }
    }
  }

  private Config configFor(Class<?> testClass, String methodName, final Properties configProperties) throws InitializationError {
    return new RobolectricTestRunner(testClass) {
      @Override protected Properties getConfigProperties() {
        return configProperties;
      }
    }.getConfig(method(methodName).withParameterTypes().in(testClass).info());
  }

  private Config configFor(Class<?> testClass, String methodName) throws InitializationError {
    return new RobolectricTestRunner(testClass)
          .getConfig(method(methodName).withParameterTypes().in(testClass).info());
  }

  private void assertConfig(Config config, int emulateSdk, String manifest, String qualifiers, Class[] shadows) {
    assertThat(stringify(config)).isEqualTo(stringify(emulateSdk, manifest, qualifiers, shadows));
  }

  @Ignore @Config(sdk = 1, manifest = "foo", shadows = Test1.class, qualifiers = "from-test")
  public static class Test1 {
    @Test public void withoutAnnotation() throws Exception {
    }

    @Config
    @Test public void withDefaultsAnnotation() throws Exception {
    }

    @Config(sdk = 9, manifest = "furf", shadows = Test2.class, qualifiers = "from-method")
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

    @Config(sdk = 9, manifest = "furf", shadows = Test1.class, qualifiers = "from-method")
    @Test public void withOverrideAnnotation() throws Exception {
    }
  }

  private String stringify(Config config) {
    int emulateSdk = config.sdk();
    String manifest = config.manifest();
    String qualifiers = config.qualifiers();
    Class<?>[] shadows = config.shadows();
    return stringify(emulateSdk, manifest, qualifiers, shadows);
  }

  private String stringify(int emulateSdk, String manifest, String qualifiers, Class<?>[] shadows) {
    return "sdk=" + emulateSdk + "\n" +
        "manifest=" + manifest + "\n" +
        "qualifiers=" + qualifiers + "\n" +
        "shadows=" + Arrays.toString(shadows);
  }

  private Properties properties(String s) throws IOException {
    StringReader reader = new StringReader(s);
    Properties properties = new Properties();
    properties.load(reader);
    return properties;
  }
}
