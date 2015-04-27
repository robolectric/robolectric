package org.robolectric;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.model.InitializationError;
import org.robolectric.annotation.Config;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.PackageResourceLoader;
import org.robolectric.res.ResourceLoader;
import org.robolectric.res.ResourcePath;
import org.robolectric.shadows.ShadowView;
import org.robolectric.shadows.ShadowViewGroup;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

public class RobolectricTestRunnerTest {
  @Test
  public void whenClassHasConfigAnnotation_getConfig_shouldMergeClassAndMethodConfig() throws Exception {
    assertConfig(configFor(Test1.class, "withoutAnnotation"),
        1, "foo", "from-test", "test/res", "test/assets", new Class[]{Test1.class}, BuildConfigConstants.class);

    assertConfig(configFor(Test1.class, "withDefaultsAnnotation"),
        1, "foo", "from-test", "test/res", "test/assets", new Class[]{Test1.class}, BuildConfigConstants.class);

    assertConfig(configFor(Test1.class, "withOverrideAnnotation"),
        9, "furf", "from-method", "method/res", "method/assets", new Class[]{Test1.class, Test2.class}, BuildConfigConstants2.class);
  }

  @Test
  public void whenClassDoesntHaveConfigAnnotation_getConfig_shouldUseMethodConfig() throws Exception {
    assertConfig(configFor(Test2.class, "withoutAnnotation"),
        -1, "--default", "", "res", "assets", new Class[]{}, Void.class);

    assertConfig(configFor(Test2.class, "withDefaultsAnnotation"),
        -1, "--default", "", "res", "assets", new Class[]{}, Void.class);

    assertConfig(configFor(Test2.class, "withOverrideAnnotation"),
        9, "furf", "from-method", "method/res", "method/assets", new Class[]{Test1.class}, BuildConfigConstants.class);
  }

  @Test
  public void whenClassDoesntHaveConfigAnnotation_getConfig_shouldMergeParentClassAndMethodConfig() throws Exception {
    assertConfig(configFor(Test5.class, "withoutAnnotation"),
        1, "foo", "from-test", "test/res", "test/assets", new Class[]{Test1.class}, BuildConfigConstants.class);

    assertConfig(configFor(Test5.class, "withDefaultsAnnotation"),
        1, "foo", "from-test", "test/res", "test/assets", new Class[]{Test1.class}, BuildConfigConstants.class);

    assertConfig(configFor(Test5.class, "withOverrideAnnotation"),
        9, "foo", "from-method5", "test/res", "method5/assets", new Class[]{Test1.class, Test5.class}, BuildConfigConstants5.class);
  }

  @Test
  public void whenClassAndParentClassHaveConfigAnnotation_getConfig_shouldMergeParentClassAndMethodConfig() throws Exception {
    assertConfig(configFor(Test6.class, "withoutAnnotation"),
        1, "foo", "from-class6", "class6/res", "test/assets", new Class[]{Test1.class, Test6.class}, BuildConfigConstants5.class);

    assertConfig(configFor(Test6.class, "withDefaultsAnnotation"),
        1, "foo", "from-class6", "class6/res", "test/assets", new Class[]{Test1.class, Test6.class}, BuildConfigConstants5.class);

    assertConfig(configFor(Test6.class, "withOverrideAnnotation"),
        9, "foo", "from-method5", "class6/res", "method5/assets", new Class[]{Test1.class, Test5.class, Test6.class}, BuildConfigConstants5.class);
  }

  @Test
  public void whenClassAndSubclassHaveConfigAnnotation_getConfig_shouldMergeClassSubclassAndMethodConfig() throws Exception {
    assertConfig(configFor(Test3.class, "withoutAnnotation"),
        1, "foo", "from-subclass", "test/res", "test/assets", new Class[]{Test1.class}, BuildConfigConstants.class);

    assertConfig(configFor(Test3.class, "withDefaultsAnnotation"),
        1, "foo", "from-subclass", "test/res", "test/assets", new Class[]{Test1.class}, BuildConfigConstants.class);

    assertConfig(configFor(Test3.class, "withOverrideAnnotation"),
        9, "furf", "from-method", "method/res", "method/assets", new Class[]{Test1.class, Test2.class}, BuildConfigConstants2.class);
  }

  @Test
  public void whenClassDoesntHaveConfigAnnotationButSubclassDoes_getConfig_shouldMergeSubclassAndMethodConfig() throws Exception {
    assertConfig(configFor(Test4.class, "withoutAnnotation"),
        -1, "--default", "from-subclass", "res", "assets", new Class[]{}, Void.class);

    assertConfig(configFor(Test4.class, "withDefaultsAnnotation"),
        -1, "--default", "from-subclass", "res", "assets", new Class[]{}, Void.class);

    assertConfig(configFor(Test4.class, "withOverrideAnnotation"),
        9, "furf", "from-method", "method/res", "method/assets", new Class[]{Test1.class}, BuildConfigConstants.class);
  }

  @Test
  public void shouldLoadDefaultsFromPropertiesFile() throws Exception {
    Properties properties = properties(
        "sdk: 432\n" +
            "manifest: --none\n" +
            "qualifiers: from-properties-file\n" +
            "resourceDir: from/properties/file/res\n" +
            "assetDir: from/properties/file/assets\n" +
            "shadows: org.robolectric.shadows.ShadowView, org.robolectric.shadows.ShadowViewGroup\n" +
            "application: org.robolectric.TestFakeApp");
    
    assertConfig(configFor(Test7.class, "withoutAnnotation", properties),
        432, "--none", "from-properties-file", "from/properties/file/res", "from/properties/file/assets", new Class[] {ShadowView.class, ShadowViewGroup.class}, BuildConfigConstants3.class);
  }

  @Test
  public void withEmptyShadowList_shouldLoadDefaultsFromPropertiesFile() throws Exception {
    Properties properties = properties("shadows:");
    assertConfig(configFor(Test7.class, "withoutAnnotation", properties), -1, "--default", "", "res", "assets", new Class[] {}, BuildConfigConstants3.class);
  }

  @Test
  public void rememberThatSomeTestRunnerMethodsShouldBeOverridable() throws Exception {
    @SuppressWarnings("unused")
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
    Method info;
    try {
      info = testClass.getMethod(methodName);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
    return new RobolectricTestRunner(testClass) {
      @Override protected Properties getConfigProperties() {
        return configProperties;
      }
    }.getConfig(info);
  }

  private Config configFor(Class<?> testClass, String methodName) throws InitializationError {
    Method info;
    try {
      info = testClass.getMethod(methodName);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
    return new RobolectricTestRunner(testClass).getConfig(info);
  }

  private void assertConfig(Config config, int sdk, String manifest, String qualifiers, String resourceDir, String assetsDir, Class[] shadows, Class constants) {
    assertThat(stringify(config)).isEqualTo(stringify(sdk, manifest, qualifiers, resourceDir, assetsDir, shadows, constants));
  }

  @Ignore
  @Config(sdk = 1, manifest = "foo", shadows = Test1.class, qualifiers = "from-test", resourceDir = "test/res", assetDir = "test/assets", constants = BuildConfigConstants.class)
  public static class Test1 {
    @Test
    public void withoutAnnotation() throws Exception {
    }

    @Test
    @Config
    public void withDefaultsAnnotation() throws Exception {
    }

    @Test
    @Config(sdk = 9, manifest = "furf", shadows = Test2.class, qualifiers = "from-method", resourceDir = "method/res", assetDir = "method/assets", constants = BuildConfigConstants2.class)
    public void withOverrideAnnotation() throws Exception {
    }
  }

  @Ignore
  public static class Test2 {
    @Test
    public void withoutAnnotation() throws Exception {
    }

    @Test
    @Config
    public void withDefaultsAnnotation() throws Exception {
    }

    @Test
    @Config(sdk = 9, manifest = "furf", shadows = Test1.class, qualifiers = "from-method", resourceDir = "method/res", assetDir = "method/assets", constants = BuildConfigConstants.class)
    public void withOverrideAnnotation() throws Exception {
    }
  }

  @Ignore
  @Config(qualifiers = "from-subclass")
  public static class Test3 extends Test1 {
  }

  @Ignore
  @Config(qualifiers = "from-subclass")
  public static class Test4 extends Test2 {
  }

  @Ignore
  public static class Test5 extends Test1 {
    @Test
    public void withoutAnnotation() throws Exception {
    }

    @Test
    @Config
    public void withDefaultsAnnotation() throws Exception {
    }

    @Test
    @Config(sdk = 9, shadows = Test5.class, qualifiers = "from-method5", assetDir = "method5/assets", constants = BuildConfigConstants5.class)
    public void withOverrideAnnotation() throws Exception {
    }
  }

  public static class BuildConfigConstants {}
  public static class BuildConfigConstants2 {}
  public static class BuildConfigConstants3 {}
  public static class BuildConfigConstants4 {}
  public static class BuildConfigConstants5 {}


  @Ignore
  @Config(qualifiers = "from-class6", shadows = Test6.class, resourceDir = "class6/res", constants = BuildConfigConstants5.class)
  public static class Test6 extends Test5 {
  }

  @Ignore
  @Config(constants = BuildConfigConstants3.class)
  public static class Test7 extends Test2 {
  }

  private String stringify(Config config) {
    int sdk = config.sdk();
    String manifest = config.manifest();
    String qualifiers = config.qualifiers();
    String resourceDir = config.resourceDir();
    String assetsDir = config.assetDir();
    Class<?>[] shadows = config.shadows();
    Class constants = config.constants();
    return stringify(sdk, manifest, qualifiers, resourceDir, assetsDir, shadows, constants);
  }

  private String stringify(int sdk, String manifest, String qualifiers, String resourceDir, String assetsDir, Class<?>[] shadows, Class constants) {
      String[] stringClasses = new String[shadows.length];
      for (int i = 0; i < stringClasses.length; i++) {
          stringClasses[i] = shadows[i].toString();
      }

      Arrays.sort(stringClasses);

      return "sdk=" + sdk + "\n" +
        "manifest=" + manifest + "\n" +
        "qualifiers=" + qualifiers + "\n" +
        "resourceDir=" + resourceDir + "\n" +
        "assetDir=" + assetsDir + "\n" +
        "shadows=" +  Arrays.toString(stringClasses) + "\n" +
        "constants=" + constants.toString();
  }

  private Properties properties(String s) throws IOException {
    StringReader reader = new StringReader(s);
    Properties properties = new Properties();
    properties.load(reader);
    return properties;
  }
}
