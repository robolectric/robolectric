package org.robolectric;

import org.junit.Rule;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.rules.ExpectedException;
import org.robolectric.annotation.Config;
import org.robolectric.manifest.AndroidManifest;

import static org.assertj.core.api.Assertions.assertThat;

public class RobolectricGradleTestRunnerTest {
  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Test
  public void getAppManifest_shouldCreateManifest() throws Exception {
    final RobolectricGradleTestRunner runner = new RobolectricGradleTestRunner(ConstantsTest.class);
    final AndroidManifest manifest = runner.getAppManifest(runner.getConfig(ConstantsTest.class.getMethod("withoutAnnotation")));

    assertThat(manifest.getPackageName()).isEqualTo("org.sandwich.foo");
    assertThat(manifest.getResDirectory().getPath()).isEqualTo("build/intermediates/res/flavor1/type1");
    assertThat(manifest.getAssetsDirectory().getPath()).isEqualTo("build/intermediates/assets/flavor1/type1");
    assertThat(manifest.getAndroidManifestFile().getPath()).isEqualTo("build/intermediates/bundles/flavor1/type1/AndroidManifest.xml");
  }

  @Test
  public void getAppManifest_shouldCreateManifestWithMethodOverrides() throws Exception {
    final RobolectricGradleTestRunner runner = new RobolectricGradleTestRunner(ConstantsTest.class);
    final AndroidManifest manifest = runner.getAppManifest(runner.getConfig(ConstantsTest.class.getMethod("withOverrideAnnotation")));

    assertThat(manifest.getPackageName()).isEqualTo("org.sandwich.bar");
    assertThat(manifest.getResDirectory().getPath()).isEqualTo("build/intermediates/res/flavor2/type2");
    assertThat(manifest.getAssetsDirectory().getPath()).isEqualTo("build/intermediates/assets/flavor2/type2");
    assertThat(manifest.getAndroidManifestFile().getPath()).isEqualTo("build/intermediates/bundles/flavor2/type2/AndroidManifest.xml");
  }

  @Test
  public void getAppManifest_shouldThrowException_whenConstantsNotSpecified() throws Exception {
    final RobolectricGradleTestRunner runner = new RobolectricGradleTestRunner(NoConstantsTest.class);
    exception.expect(RuntimeException.class);
    runner.getAppManifest(runner.getConfig(NoConstantsTest.class.getMethod("withoutAnnotation")));
  }

  @Ignore
  @Config(constants = BuildConfig.class)
  public static class ConstantsTest {

    @Test
    public void withoutAnnotation() throws Exception {
    }

    @Test @Config(constants = BuildConfigOverride.class)
    public void withOverrideAnnotation() throws Exception {
    }
  }

  @Ignore
  @Config
  public static class NoConstantsTest {

    @Test
    public void withoutAnnotation() throws Exception {
    }
  }

  public static class BuildConfig {
    public static final String APPLICATION_ID = "org.sandwich.foo";
    public static final String BUILD_TYPE = "type1";
    public static final String FLAVOR = "flavor1";
  }

  public static class BuildConfigOverride {
    public static final String APPLICATION_ID = "org.sandwich.bar";
    public static final String BUILD_TYPE = "type2";
    public static final String FLAVOR = "flavor2";
  }
}