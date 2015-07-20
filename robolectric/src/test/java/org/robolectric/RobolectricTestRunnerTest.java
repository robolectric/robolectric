package org.robolectric;

import android.app.Application;

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
        new int[] {1}, "foo", TestFakeApp.class, "com.example.test", "from-test", "test/res", "test/assets", new Class[]{Test1.class}, new String[]{"libs/test"}, BuildConfigConstants.class);

    assertConfig(configFor(Test1.class, "withDefaultsAnnotation"),
        new int[] {1}, "foo", TestFakeApp.class, "com.example.test", "from-test", "test/res", "test/assets", new Class[]{Test1.class}, new String[]{"libs/test"}, BuildConfigConstants.class);

    assertConfig(configFor(Test1.class, "withOverrideAnnotation"),
        new int[] {9}, "furf", TestApplication.class, "com.example.method", "from-method", "method/res", "method/assets", new Class[]{Test1.class, Test2.class}, new String[]{"libs/method", "libs/test"}, BuildConfigConstants2.class);
  }

  @Test
  public void whenClassDoesntHaveConfigAnnotation_getConfig_shouldUseMethodConfig() throws Exception {
    assertConfig(configFor(Test2.class, "withoutAnnotation"),
        new int[0], "--default", Application.class, "", "", "res", "assets", new Class[]{}, new String[]{}, Void.class);

    assertConfig(configFor(Test2.class, "withDefaultsAnnotation"),
        new int[0], "--default", Application.class, "", "", "res", "assets", new Class[]{}, new String[]{}, Void.class);

    assertConfig(configFor(Test2.class, "withOverrideAnnotation"),
        new int[] {9}, "furf", TestFakeApp.class, "com.example.method", "from-method", "method/res", "method/assets", new Class[]{Test1.class}, new String[]{"libs/method"}, BuildConfigConstants.class);
  }

  @Test
  public void whenClassDoesntHaveConfigAnnotation_getConfig_shouldMergeParentClassAndMethodConfig() throws Exception {
    assertConfig(configFor(Test5.class, "withoutAnnotation"),
        new int[] {1}, "foo", TestFakeApp.class, "com.example.test", "from-test", "test/res", "test/assets", new Class[]{Test1.class}, new String[]{"libs/test"}, BuildConfigConstants.class);

    assertConfig(configFor(Test5.class, "withDefaultsAnnotation"),
        new int[] {1}, "foo", TestFakeApp.class, "com.example.test", "from-test", "test/res", "test/assets", new Class[]{Test1.class}, new String[]{"libs/test"}, BuildConfigConstants.class);

    assertConfig(configFor(Test5.class, "withOverrideAnnotation"),
        new int[] {14}, "foo", TestFakeApp.class, "com.example.test", "from-method5", "test/res", "method5/assets", new Class[]{Test1.class, Test5.class}, new String[]{"libs/test"}, BuildConfigConstants5.class);
  }

  @Test
  public void whenClassAndParentClassHaveConfigAnnotation_getConfig_shouldMergeParentClassAndMethodConfig() throws Exception {
    assertConfig(configFor(Test6.class, "withoutAnnotation"),
        new int[] {1}, "foo", TestFakeApp.class, "com.example.test", "from-class6", "class6/res", "test/assets", new Class[]{Test1.class, Test6.class}, new String[]{"libs/test"}, BuildConfigConstants6.class);

    assertConfig(configFor(Test6.class, "withDefaultsAnnotation"),
        new int[] {1}, "foo", TestFakeApp.class, "com.example.test", "from-class6", "class6/res", "test/assets", new Class[]{Test1.class, Test6.class}, new String[]{"libs/test"}, BuildConfigConstants6.class);

    assertConfig(configFor(Test6.class, "withOverrideAnnotation"),
        new int[] {14}, "foo", TestFakeApp.class, "com.example.test", "from-method5", "class6/res", "method5/assets", new Class[]{Test1.class, Test5.class, Test6.class}, new String[]{"libs/test"}, BuildConfigConstants5.class);
  }

  @Test
  public void whenClassAndSubclassHaveConfigAnnotation_getConfig_shouldMergeClassSubclassAndMethodConfig() throws Exception {
    assertConfig(configFor(Test3.class, "withoutAnnotation"),
        new int[] {1}, "foo", TestFakeApp.class, "com.example.test", "from-subclass", "test/res", "test/assets", new Class[]{Test1.class}, new String[]{"libs/test"}, BuildConfigConstants.class);

    assertConfig(configFor(Test3.class, "withDefaultsAnnotation"),
        new int[] {1}, "foo", TestFakeApp.class, "com.example.test", "from-subclass", "test/res", "test/assets", new Class[]{Test1.class}, new String[]{"libs/test"}, BuildConfigConstants.class);

    assertConfig(configFor(Test3.class, "withOverrideAnnotation"),
        new int[] {9},"furf", TestApplication.class, "com.example.method", "from-method", "method/res", "method/assets", new Class[]{Test1.class, Test2.class}, new String[]{"libs/method", "libs/test"}, BuildConfigConstants2.class);
  }

  @Test
  public void whenClassDoesntHaveConfigAnnotationButSubclassDoes_getConfig_shouldMergeSubclassAndMethodConfig() throws Exception {
    assertConfig(configFor(Test4.class, "withoutAnnotation"),
        new int[0],  "--default", Application.class, "", "from-subclass", "res", "assets", new Class[]{}, new String[]{}, Void.class);

    assertConfig(configFor(Test4.class, "withDefaultsAnnotation"),
        new int[0],  "--default", Application.class, "", "from-subclass", "res", "assets", new Class[]{}, new String[]{}, Void.class);

    assertConfig(configFor(Test4.class, "withOverrideAnnotation"),
        new int[] {9}, "furf", TestFakeApp.class, "com.example.method", "from-method", "method/res", "method/assets", new Class[]{Test1.class}, new String[]{"libs/method"}, BuildConfigConstants.class);
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
            "application: org.robolectric.TestFakeApp\n" +
            "packageName: com.example.test\n" +
            "libraries: libs/test, libs/test2\n" +
            "constants: org.robolectric.RobolectricTestRunnerTest$BuildConfigConstants3");

    assertConfig(configFor(Test2.class, "withoutAnnotation", properties),
        new int[] {432}, "--none", TestFakeApp.class, "com.example.test", "from-properties-file", "from/properties/file/res", "from/properties/file/assets", new Class[] {ShadowView.class, ShadowViewGroup.class}, new String[]{"libs/test", "libs/test2"}, BuildConfigConstants3.class);
  }

  @Test
  public void withEmptyShadowList_shouldLoadDefaultsFromPropertiesFile() throws Exception {
    Properties properties = properties("shadows:");
    assertConfig(configFor(Test2.class, "withoutAnnotation", properties), new int[0],  "--default", Application.class, "", "", "res", "assets", new Class[] {}, new String[]{}, null);
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

  private void assertConfig(Config config, int[] sdk, String manifest, Class<? extends Application> application, String packageName, String qualifiers, String resourceDir, String assetsDir, Class<?>[] shadows, String[] libraries, Class<?> constants) {
    assertThat(stringify(config)).isEqualTo(stringify(sdk, manifest, application, packageName, qualifiers, resourceDir, assetsDir, shadows, libraries, constants));
  }

  @Ignore
  @Config(sdk = 1, manifest = "foo", application = TestFakeApp.class, packageName = "com.example.test", shadows = Test1.class, libraries = "libs/test", qualifiers = "from-test", resourceDir = "test/res", assetDir = "test/assets", constants = BuildConfigConstants.class)
  public static class Test1 {
    @Test
    public void withoutAnnotation() throws Exception {
    }

    @Test
    @Config
    public void withDefaultsAnnotation() throws Exception {
    }

    @Test
    @Config(sdk = 9, manifest = "furf", application = TestApplication.class, packageName = "com.example.method", shadows = Test2.class, libraries = "libs/method", qualifiers = "from-method", resourceDir = "method/res", assetDir = "method/assets", constants = BuildConfigConstants2.class)
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
    @Config(sdk = 9, manifest = "furf", application = TestFakeApp.class, packageName = "com.example.method", shadows = Test1.class, libraries = "libs/method", qualifiers = "from-method", resourceDir = "method/res", assetDir = "method/assets", constants = BuildConfigConstants.class)
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
    @Config(sdk = 14, shadows = Test5.class, qualifiers = "from-method5", assetDir = "method5/assets", constants = BuildConfigConstants5.class)
    public void withOverrideAnnotation() throws Exception {
    }
  }

  public static class BuildConfigConstants {}
  public static class BuildConfigConstants2 {}
  public static class BuildConfigConstants3 {}
  public static class BuildConfigConstants4 {}
  public static class BuildConfigConstants5 {}
  public static class BuildConfigConstants6 {}


  @Ignore
  @Config(qualifiers = "from-class6", shadows = Test6.class, resourceDir = "class6/res", constants = BuildConfigConstants6.class)
  public static class Test6 extends Test5 {
  }

  private String stringify(Config config) {
    int[] sdk = config.sdk();
    String manifest = config.manifest();
    Class<? extends Application> application = config.application();
    String packageName = config.packageName();
    String qualifiers = config.qualifiers();
    String resourceDir = config.resourceDir();
    String assetsDir = config.assetDir();
    Class<?>[] shadows = config.shadows();
    String[] libraries = config.libraries();
    Class<?> constants = config.constants();
    return stringify(sdk, manifest, application, packageName, qualifiers, resourceDir, assetsDir, shadows, libraries, constants);
  }

  private String stringify(int[] sdk, String manifest, Class<? extends Application> application, String packageName, String qualifiers, String resourceDir, String assetsDir, Class<?>[] shadows, String[] libraries, Class<?> constants) {
      String[] stringClasses = new String[shadows.length];
      for (int i = 0; i < stringClasses.length; i++) {
          stringClasses[i] = shadows[i].toString();
      }

      Arrays.sort(stringClasses);

      String[] sortedLibraries = libraries.clone();
      Arrays.sort(sortedLibraries);

      return "sdk=" + Arrays.toString(sdk) + "\n" +
        "manifest=" + manifest + "\n" +
        "application=" + application + "\n" +
        "packageName=" + packageName + "\n" +
        "qualifiers=" + qualifiers + "\n" +
        "resourceDir=" + resourceDir + "\n" +
        "assetDir=" + assetsDir + "\n" +
        "shadows=" + Arrays.toString(stringClasses) + "\n" +
        "libraries=" + Arrays.toString(sortedLibraries) + "\n" +
        "constants=" + constants;
  }

  private Properties properties(String s) throws IOException {
    StringReader reader = new StringReader(s);
    Properties properties = new Properties();
    properties.load(reader);
    return properties;
  }
}