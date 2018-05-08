package org.robolectric;

import static com.google.common.collect.ImmutableMap.of;
import static com.google.common.truth.Truth.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.robolectric.annotation.Config.DEFAULT_APPLICATION;

import android.app.Application;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.model.InitializationError;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowView;
import org.robolectric.shadows.ShadowViewGroup;
import org.robolectric.shadows.testing.TestApplication;

@RunWith(JUnit4.class)
public class ConfigMergerTest {

  @Test public void defaultValuesAreMerged() throws Exception {
    assertThat(configFor(Test2.class, "withoutAnnotation",
        new Config.Builder().build()).manifest())
        .isEqualTo("AndroidManifest.xml");
  }

  @Test public void globalValuesAreMerged() throws Exception {
    assertThat(configFor(Test2.class, "withoutAnnotation",
        new Config.Builder().setManifest("ManifestFromGlobal.xml").build()).manifest())
        .isEqualTo("ManifestFromGlobal.xml");
  }

  @Test
  public void whenClassHasConfigAnnotation_getConfig_shouldMergeClassAndMethodConfig() throws Exception {
    assertConfig(
        configFor(Test1.class, "withoutAnnotation"),
        new int[] {1},
        "foo",
        TestFakeApp.class,
        "com.example.test",
        "from-test",
        "test/res",
        "test/assets",
        new Class[] {Test1.class},
        new String[] {"com.example.test1"},
        new String[] {"libs/test"});

    assertConfig(
        configFor(Test1.class, "withDefaultsAnnotation"),
        new int[] {1},
        "foo",
        TestFakeApp.class,
        "com.example.test",
        "from-test",
        "test/res",
        "test/assets",
        new Class[] {Test1.class},
        new String[] {"com.example.test1"},
        new String[] {"libs/test"});

    assertConfig(
        configFor(Test1.class, "withOverrideAnnotation"),
        new int[] {9},
        "furf",
        TestApplication.class,
        "com.example.method",
        "from-method",
        "method/res",
        "method/assets",
        new Class[] {Test1.class, Test2.class},
        new String[] {"com.example.test1", "com.example.method1"},
        new String[] {"libs/method", "libs/test"});
  }

  @Test
  public void whenClassDoesntHaveConfigAnnotation_getConfig_shouldUseMethodConfig() throws Exception {
    assertConfig(
        configFor(Test2.class, "withoutAnnotation"),
        new int[0],
        "AndroidManifest.xml",
        DEFAULT_APPLICATION,
        "",
        "",
        "res",
        "assets",
        new Class[] {},
        new String[] {},
        new String[] {});

    assertConfig(
        configFor(Test2.class, "withDefaultsAnnotation"),
        new int[0],
        "AndroidManifest.xml",
        DEFAULT_APPLICATION,
        "",
        "",
        "res",
        "assets",
        new Class[] {},
        new String[] {},
        new String[] {});

    assertConfig(
        configFor(Test2.class, "withOverrideAnnotation"),
        new int[] {9},
        "furf",
        TestFakeApp.class,
        "com.example.method",
        "from-method",
        "method/res",
        "method/assets",
        new Class[] {Test1.class},
        new String[] {"com.example.method2"},
        new String[] {"libs/method"});
  }

  @Test
  public void whenClassDoesntHaveConfigAnnotation_getConfig_shouldMergeParentClassAndMethodConfig() throws Exception {
    assertConfig(
        configFor(Test5.class, "withoutAnnotation"),
        new int[] {1},
        "foo",
        TestFakeApp.class,
        "com.example.test",
        "from-test",
        "test/res",
        "test/assets",
        new Class[] {Test1.class, Test1.class},
        new String[] {"com.example.test1"},
        new String[] {"libs/test"});

    assertConfig(
        configFor(Test5.class, "withDefaultsAnnotation"),
        new int[] {1},
        "foo",
        TestFakeApp.class,
        "com.example.test",
        "from-test",
        "test/res",
        "test/assets",
        new Class[] {Test1.class, Test1.class},
        new String[] {"com.example.test1"},
        new String[] {"libs/test"});

    assertConfig(
        configFor(Test5.class, "withOverrideAnnotation"),
        new int[] {14},
        "foo",
        TestFakeApp.class,
        "com.example.test",
        "from-method5",
        "test/res",
        "method5/assets",
        new Class[] {Test1.class, Test1.class, Test5.class},
        new String[] {"com.example.test1", "com.example.method5"},
        new String[] {"libs/test"});
  }

  @Test
  public void whenClassAndParentClassHaveConfigAnnotation_getConfig_shouldMergeParentClassAndMethodConfig() throws Exception {
    assertConfig(
        configFor(Test6.class, "withoutAnnotation"),
        new int[] {1},
        "foo",
        TestFakeApp.class,
        "com.example.test",
        "from-class6",
        "class6/res",
        "test/assets",
        new Class[] {Test1.class, Test1.class, Test6.class},
        new String[] {"com.example.test1", "com.example.test6"},
        new String[] {"libs/test"});

    assertConfig(
        configFor(Test6.class, "withDefaultsAnnotation"),
        new int[] {1},
        "foo",
        TestFakeApp.class,
        "com.example.test",
        "from-class6",
        "class6/res",
        "test/assets",
        new Class[] {Test1.class, Test1.class, Test6.class},
        new String[] {"com.example.test1", "com.example.test6"},
        new String[] {"libs/test"});

    assertConfig(
        configFor(Test6.class, "withOverrideAnnotation"),
        new int[] {14},
        "foo",
        TestFakeApp.class,
        "com.example.test",
        "from-method5",
        "class6/res",
        "method5/assets",
        new Class[] {Test1.class, Test1.class, Test6.class, Test5.class},
        new String[] {"com.example.test1", "com.example.method5", "com.example.test6"},
        new String[] {"libs/test"});
  }

  @Test
  public void whenClassAndSubclassHaveConfigAnnotation_getConfig_shouldMergeClassSubclassAndMethodConfig() throws Exception {
    assertConfig(
        configFor(Test3.class, "withoutAnnotation"),
        new int[] {1},
        "foo",
        TestFakeApp.class,
        "com.example.test",
        "from-subclass",
        "test/res",
        "test/assets",
        new Class[] {Test1.class},
        new String[] {"com.example.test1"},
        new String[] {"libs/test"});

    assertConfig(
        configFor(Test3.class, "withDefaultsAnnotation"),
        new int[] {1},
        "foo",
        TestFakeApp.class,
        "com.example.test",
        "from-subclass",
        "test/res",
        "test/assets",
        new Class[] {Test1.class},
        new String[] {"com.example.test1"},
        new String[] {"libs/test"});

    assertConfig(
        configFor(Test3.class, "withOverrideAnnotation"),
        new int[] {9},
        "furf",
        TestApplication.class,
        "com.example.method",
        "from-method",
        "method/res",
        "method/assets",
        new Class[] {Test1.class, Test2.class},
        new String[] {"com.example.test1", "com.example.method1"},
        new String[] {"libs/method", "libs/test"});
  }

  @Test
  public void whenClassDoesntHaveConfigAnnotationButSubclassDoes_getConfig_shouldMergeSubclassAndMethodConfig() throws Exception {
    assertConfig(
        configFor(Test4.class, "withoutAnnotation"),
        new int[0],
        "AndroidManifest.xml",
        DEFAULT_APPLICATION,
        "",
        "from-subclass",
        "res",
        "assets",
        new Class[] {},
        new String[] {},
        new String[] {});

    assertConfig(
        configFor(Test4.class, "withDefaultsAnnotation"),
        new int[0],
        "AndroidManifest.xml",
        DEFAULT_APPLICATION,
        "",
        "from-subclass",
        "res",
        "assets",
        new Class[] {},
        new String[] {},
        new String[] {});

    assertConfig(
        configFor(Test4.class, "withOverrideAnnotation"),
        new int[] {9},
        "furf",
        TestFakeApp.class,
        "com.example.method",
        "from-method",
        "method/res",
        "method/assets",
        new Class[] {Test1.class},
        new String[] {"com.example.method2"},
        new String[] {"libs/method"});
  }

  @Test
  public void shouldLoadDefaultsFromGlobalPropertiesFile() throws Exception {
    String properties =
        "sdk: 432\n"
            + "manifest: --none\n"
            + "qualifiers: from-properties-file\n"
            + "resourceDir: from/properties/file/res\n"
            + "assetDir: from/properties/file/assets\n"
            + "shadows: org.robolectric.shadows.ShadowView, org.robolectric.shadows.ShadowViewGroup\n"
            + "application: org.robolectric.TestFakeApp\n"
            + "packageName: com.example.test\n"
            + "instrumentedPackages: com.example.test1, com.example.test2\n"
            + "libraries: libs/test, libs/test2";

    assertConfig(
        configFor(Test2.class, "withoutAnnotation", of("robolectric.properties", properties)),
        new int[] {432},
        "--none",
        TestFakeApp.class,
        "com.example.test",
        "from-properties-file",
        "from/properties/file/res",
        "from/properties/file/assets",
        new Class[] {ShadowView.class, ShadowViewGroup.class},
        new String[] {"com.example.test1", "com.example.test2"},
        new String[] {"libs/test", "libs/test2"});
  }

  @Test
  public void shouldMergeConfigFromTestClassPackageProperties() throws Exception {
    assertConfig(
        configFor(
            Test2.class,
            "withoutAnnotation",
            of("org/robolectric/robolectric.properties", "sdk: 432\n")),
        new int[] {432},
        "AndroidManifest.xml",
        DEFAULT_APPLICATION,
        "",
        "",
        "res",
        "assets",
        new Class[] {},
        new String[] {},
        new String[] {});
  }

  @Test
  public void shouldMergeConfigUpPackageHierarchy() throws Exception {
    assertConfig(
        configFor(
            Test2.class,
            "withoutAnnotation",
            of(
                "org/robolectric/robolectric.properties",
                    "qualifiers: from-org-robolectric\nlibraries: FromOrgRobolectric\n",
                "org/robolectric.properties",
                    "sdk: 123\nqualifiers: from-org\nlibraries: FromOrg\n",
                "robolectric.properties",
                    "sdk: 456\nqualifiers: from-top-level\nlibraries: FromTopLevel\n")),
        new int[] {123},
        "AndroidManifest.xml",
        DEFAULT_APPLICATION,
        "",
        "from-org-robolectric",
        "res",
        "assets",
        new Class[] {},
        new String[] {},
        new String[] {"FromOrgRobolectric", "FromOrg", "FromTopLevel"});
  }

  @Test
  public void withEmptyShadowList_shouldLoadDefaultsFromGlobalPropertiesFile() throws Exception {
    assertConfig(
        configFor(Test2.class, "withoutAnnotation", of("robolectric.properties", "shadows:")),
        new int[0],
        "AndroidManifest.xml",
        DEFAULT_APPLICATION,
        "",
        "",
        "res",
        "assets",
        new Class[] {},
        new String[] {},
        new String[] {});
  }

  @Test public void testPackageHierarchyOf() throws Exception {
    assertThat(new ConfigMerger().packageHierarchyOf(ConfigMergerTest.class))
        .containsExactly("org.robolectric", "org", "");
  }

  /////////////////////////////

  private Config configFor(Class<?> testClass, String methodName, final Map<String, String> configProperties) throws InitializationError {
    return configFor(testClass, methodName, configProperties, Config.Builder.defaults().build());
  }

  private Config configFor(Class<?> testClass, String methodName) throws InitializationError {
    Config.Implementation globalConfig = Config.Builder.defaults().build();
    return configFor(testClass, methodName, globalConfig);
  }

  private Config configFor(Class<?> testClass, String methodName, Config.Implementation globalConfig) throws InitializationError {
    return configFor(testClass, methodName, new HashMap<>(), globalConfig);
  }

  private Config configFor(Class<?> testClass, String methodName, final Map<String, String> configProperties, Config.Implementation globalConfig) throws InitializationError {
    Method info = getMethod(testClass, methodName);
    return new ConfigMerger() {
      @Override
      InputStream getResourceAsStream(String resourceName) {
        String properties = configProperties.get(resourceName);
        return properties == null ? null : new ByteArrayInputStream(properties.getBytes(UTF_8));
      }
    }.getConfig(testClass, info, globalConfig);
  }

  private static Method getMethod(Class<?> testClass, String methodName) {
    try {
      return testClass.getMethod(methodName);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  private static void assertConfig(
      Config config,
      int[] sdk,
      String manifest,
      Class<? extends Application> application,
      String packageName,
      String qualifiers,
      String resourceDir,
      String assetsDir,
      Class<?>[] shadows,
      String[] instrumentedPackages,
      String[] libraries) {
    assertThat(config.sdk()).isEqualTo(sdk);
    assertThat(config.manifest()).isEqualTo(manifest);
    assertThat(config.application()).isEqualTo(application);
    assertThat(config.packageName()).isEqualTo(packageName);
    assertThat(config.qualifiers()).isEqualTo(qualifiers);
    assertThat(config.resourceDir()).isEqualTo(resourceDir);
    assertThat(config.assetDir()).isEqualTo(assetsDir);
    assertThat(config.shadows()).asList().containsAllIn(shadows).inOrder();
    assertThat(config.instrumentedPackages()).asList().containsAllIn(instrumentedPackages);
    assertThat(config.libraries()).asList().containsAllIn(libraries);
  }

  @Ignore
  @Config(
      sdk = 1,
      manifest = "foo",
      application = TestFakeApp.class,
      packageName = "com.example.test",
      shadows = Test1.class,
      instrumentedPackages = "com.example.test1",
      libraries = "libs/test",
      qualifiers = "from-test",
      resourceDir = "test/res",
      assetDir = "test/assets")
  public static class Test1 {
    @Test
    public void withoutAnnotation() throws Exception {
    }

    @Test
    @Config
    public void withDefaultsAnnotation() throws Exception {
    }

    @Test
    @Config(
        sdk = 9,
        manifest = "furf",
        application = TestApplication.class,
        packageName = "com.example.method",
        shadows = Test2.class,
        instrumentedPackages = "com.example.method1",
        libraries = "libs/method",
        qualifiers = "from-method",
        resourceDir = "method/res",
        assetDir = "method/assets")
    public void withOverrideAnnotation() throws Exception {}
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
    @Config(
        sdk = 9,
        manifest = "furf",
        application = TestFakeApp.class,
        packageName = "com.example.method",
        shadows = Test1.class,
        instrumentedPackages = "com.example.method2",
        libraries = "libs/method",
        qualifiers = "from-method",
        resourceDir = "method/res",
        assetDir = "method/assets")
    public void withOverrideAnnotation() throws Exception {}
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
    @Override
    @Test
    public void withoutAnnotation() throws Exception {
    }

    @Override
    @Test
    @Config
    public void withDefaultsAnnotation() throws Exception {
    }

    @Override
    @Test
    @Config(
        sdk = 14,
        shadows = Test5.class,
        instrumentedPackages = "com.example.method5",
        packageName = "com.example.test",
        qualifiers = "from-method5",
        assetDir = "method5/assets")
    public void withOverrideAnnotation() throws Exception {}
  }

  @Ignore
  @Config(
      qualifiers = "from-class6",
      shadows = Test6.class,
      instrumentedPackages = "com.example.test6",
      resourceDir = "class6/res")
  public static class Test6 extends Test5 {}
}
