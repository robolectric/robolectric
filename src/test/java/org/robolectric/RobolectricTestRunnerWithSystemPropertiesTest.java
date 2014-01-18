package org.robolectric;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.model.InitializationError;
import org.robolectric.annotation.Config;
import org.robolectric.res.FsFile;
import org.robolectric.util.TestUtil;

import java.util.Arrays;
import java.util.Collection;

import static java.util.Arrays.asList;
import static org.fest.reflect.core.Reflection.method;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeNotNull;
import static org.robolectric.util.TestUtil.joinPath;
import static org.robolectric.util.TestUtil.resourceFile;

@RunWith(Parameterized.class)
public class RobolectricTestRunnerWithSystemPropertiesTest {
  private final Class<?> testClass;
  private final String testMethod;
  private final boolean expectValuesFromProperties;

  private final static FsFile manifestFile = resourceFile("mavenapp/AndroidManifest.xml");
  private final static FsFile resDir = resourceFile("mavenapp/src/main/resources");
  private final static FsFile assetsDir = resourceFile("mavenapp/src/main/assets");
  private final static FsFile projectPropertiesFile = resourceFile("mavenapp/project.properties");

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {
        {RobolectricTestRunnerTest.Test1.class, "withoutAnnotation", true},
        {RobolectricTestRunnerTest.Test1.class, "withDefaultsAnnotation", true},
        {RobolectricTestRunnerTest.Test1.class, "withOverrideAnnotation", false},
        {RobolectricTestRunnerTest.Test2.class, "withoutAnnotation", true},
        {RobolectricTestRunnerTest.Test2.class, "withDefaultsAnnotation", true},
        {RobolectricTestRunnerTest.Test2.class, "withOverrideAnnotation", false},
    });
  }

  public RobolectricTestRunnerWithSystemPropertiesTest(Class<?> testClass, String testMethod, boolean propValues) {
    this.testClass = testClass;
    this.testMethod = testMethod;
    this.expectValuesFromProperties = propValues;
  }

  @Before
  public void setUp() {
    System.setProperty("android.manifest", manifestFile.getPath());
    System.setProperty("android.resources", resDir.getPath());
    System.setProperty("android.assets", assetsDir.getPath());
    System.setProperty("android.project.properties", projectPropertiesFile.getPath());
  }

  @After
  public void tearDown() {
    System.clearProperty("android.manifest");
    System.clearProperty("android.resources");
    System.clearProperty("android.assets");
    System.clearProperty("android.project.properties");
  }

  @Test
  public void shouldReadManifestPropertiesFromSystemPropertiesIfTheyAreSet() throws InitializationError {
    RobolectricTestRunner testRunner = new RobolectricTestRunner(testClass);
    Config config = testRunner.getConfig(method(testMethod).withParameterTypes().in(testClass).info());
    AndroidManifest manifest = testRunner.getAppManifest(config);

    // skip test if there is no manifest beneath
    assumeNotNull(manifest);

    assertEquals(expectValuesFromProperties, manifest.getResDirectory().equals(resDir));
    assertEquals(expectValuesFromProperties, manifest.getAssetsDirectory().equals(assetsDir));
    assertEquals(expectValuesFromProperties, manifest.getApplicationName().equals("MavenApp"));
    if (expectValuesFromProperties) {
      TestUtil.assertEquals(
          asList(
              joinPath("./src/test/resources/mavenapp/src/main/resources"),
              joinPath("./src/test/resources/mavenapp/../lib1/res"),
              joinPath("./src/test/resources/mavenapp/../lib1/../lib3/res")
          ),
          TestUtil.stringify(manifest.getIncludedResourcePaths())
      );
    }
  }
}
