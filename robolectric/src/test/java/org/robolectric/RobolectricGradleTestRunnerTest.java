package org.robolectric;

import org.junit.*;
import org.junit.rules.ExpectedException;
import org.robolectric.annotation.Config;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.FileFsFile;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class RobolectricGradleTestRunnerTest {
  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Before
  public void setup() {
    FileFsFile.from("build", "intermediates", "res").getFile().mkdirs();
    FileFsFile.from("build", "intermediates", "assets").getFile().mkdirs();
    FileFsFile.from("build", "intermediates", "manifests").getFile().mkdirs();
  }

  @After
  public void teardown() throws IOException {
    delete(FileFsFile.from("build", "intermediates", "res").getFile());
    delete(FileFsFile.from("build", "intermediates", "assets").getFile());
    delete(FileFsFile.from("build", "intermediates", "manifests").getFile());
    delete(FileFsFile.from("build", "intermediates", "res", "merged").getFile());
  }

  private static String convertPath(String path) {
    return path.replace('/', File.separatorChar);
  }
  
  @Test
  public void getAppManifest_forApplications_shouldCreateManifest() throws Exception {
    final RobolectricGradleTestRunner runner = new RobolectricGradleTestRunner(ConstantsTest.class);
    final AndroidManifest manifest = runner.getAppManifest(runner.getConfig(ConstantsTest.class.getMethod("withoutAnnotation")));

    assertThat(manifest.getPackageName()).isEqualTo("org.sandwich.foo");
    assertThat(manifest.getResDirectory().getPath()).isEqualTo(convertPath("build/intermediates/res/flavor1/type1"));
    assertThat(manifest.getAssetsDirectory().getPath()).isEqualTo(convertPath("build/intermediates/assets/flavor1/type1"));
    assertThat(manifest.getAndroidManifestFile().getPath()).isEqualTo(convertPath("build/intermediates/manifests/full/flavor1/type1/AndroidManifest.xml"));
  }

  @Test
  public void getAppManifest_forLibraries_shouldCreateManifest() throws Exception {
    delete(FileFsFile.from("build", "intermediates", "res").getFile());
    delete(FileFsFile.from("build", "intermediates", "assets").getFile());
    delete(FileFsFile.from("build", "intermediates", "manifests").getFile());

    final RobolectricGradleTestRunner runner = new RobolectricGradleTestRunner(ConstantsTest.class);
    final AndroidManifest manifest = runner.getAppManifest(runner.getConfig(ConstantsTest.class.getMethod("withoutAnnotation")));

    assertThat(manifest.getPackageName()).isEqualTo("org.sandwich.foo");
    assertThat(manifest.getResDirectory().getPath()).isEqualTo(convertPath("build/intermediates/bundles/flavor1/type1/res"));
    assertThat(manifest.getAssetsDirectory().getPath()).isEqualTo(convertPath("build/intermediates/bundles/flavor1/type1/assets"));
    assertThat(manifest.getAndroidManifestFile().getPath()).isEqualTo(convertPath("build/intermediates/bundles/flavor1/type1/AndroidManifest.xml"));
  }

  @Test
  public void getAppManifest_shouldCreateManifestWithMethodOverrides() throws Exception {
    final RobolectricGradleTestRunner runner = new RobolectricGradleTestRunner(ConstantsTest.class);
    final AndroidManifest manifest = runner.getAppManifest(runner.getConfig(ConstantsTest.class.getMethod("withOverrideAnnotation")));

    assertThat(manifest.getPackageName()).isEqualTo("org.sandwich.bar");
    assertThat(manifest.getResDirectory().getPath()).isEqualTo(convertPath("build/intermediates/res/flavor2/type2"));
    assertThat(manifest.getAssetsDirectory().getPath()).isEqualTo(convertPath("build/intermediates/assets/flavor2/type2"));
    assertThat(manifest.getAndroidManifestFile().getPath()).isEqualTo(convertPath("build/intermediates/manifests/full/flavor2/type2/AndroidManifest.xml"));
  }

  @Test
  public void getAppManifest_withPackageNameOverride_shouldCreateManifest() throws Exception {
    final RobolectricGradleTestRunner runner = new RobolectricGradleTestRunner(PackageNameTest.class);
    final AndroidManifest manifest = runner.getAppManifest(runner.getConfig(PackageNameTest.class.getMethod("withoutAnnotation")));

    assertThat(manifest.getPackageName()).isEqualTo("fake.package.name");
    assertThat(manifest.getResDirectory().getPath()).isEqualTo(convertPath("build/intermediates/res/flavor1/type1"));
    assertThat(manifest.getAssetsDirectory().getPath()).isEqualTo(convertPath("build/intermediates/assets/flavor1/type1"));
    assertThat(manifest.getAndroidManifestFile().getPath()).isEqualTo(convertPath("build/intermediates/manifests/full/flavor1/type1/AndroidManifest.xml"));
  }

  @Test
  public void getAppManifest_withMergedResources_shouldHaveMergedResPath() throws Exception {
    FileFsFile.from("build", "intermediates", "res", "merged").getFile().mkdirs();

    final RobolectricGradleTestRunner runner = new RobolectricGradleTestRunner(PackageNameTest.class);
    final AndroidManifest manifest = runner.getAppManifest(runner.getConfig(PackageNameTest.class.getMethod("withoutAnnotation")));

    assertThat(manifest.getPackageName()).isEqualTo("fake.package.name");
    assertThat(manifest.getResDirectory().getPath()).isEqualTo(convertPath("build/intermediates/res/merged/flavor1/type1"));
    assertThat(manifest.getAssetsDirectory().getPath()).isEqualTo(convertPath("build/intermediates/assets/flavor1/type1"));
    assertThat(manifest.getAndroidManifestFile().getPath()).isEqualTo(convertPath("build/intermediates/manifests/full/flavor1/type1/AndroidManifest.xml"));
  }

  @Test
  public void getAppManifest_shouldThrowException_whenConstantsNotSpecified() throws Exception {
    final RobolectricGradleTestRunner runner = new RobolectricGradleTestRunner(NoConstantsTest.class);
    exception.expect(RuntimeException.class);
    runner.getAppManifest(runner.getConfig(NoConstantsTest.class.getMethod("withoutAnnotation")));
  }

  private void delete(File file) {
    final File[] files = file.listFiles();
    if (files != null) {
      for (File each : files) {
        delete(each);
      }
    }
    file.delete();
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

  @Ignore
  @Config(constants = BuildConfig.class, packageName = "fake.package.name")
  public static class PackageNameTest {

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
