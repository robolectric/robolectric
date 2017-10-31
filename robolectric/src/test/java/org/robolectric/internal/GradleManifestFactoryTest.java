package org.robolectric.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.annotation.Config;
import org.robolectric.gradleapp.BuildConfig;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.FileFsFile;
import org.robolectric.res.FsFile;

@RunWith(JUnit4.class)
public class GradleManifestFactoryTest {
  @Rule
  public ExpectedException exception = ExpectedException.none();
  private GradleManifestFactory factory;
  private Config.Builder configBuilder;

  @Before
  public void setup() {
    FileFsFile.from("build", "intermediates", "res").getFile().mkdirs();
    FileFsFile.from("build", "intermediates", "assets").getFile().mkdirs();
    FileFsFile.from("build", "intermediates", "manifests", "full").getFile().mkdirs();

    FileFsFile.from("custom_build", "intermediates", "res").getFile().mkdirs();
    FileFsFile.from("custom_build", "intermediates", "assets").getFile().mkdirs();
    FileFsFile.from("custom_build", "intermediates", "manifests").getFile().mkdirs();
    
    configBuilder = Config.Builder.defaults();
    factory = new GradleManifestFactory();
  }

  @After
  public void teardown() throws IOException {
    delete(FileFsFile.from("build", "intermediates", "res").getFile());
    delete(FileFsFile.from("build", "intermediates", "assets").getFile());
    delete(FileFsFile.from("build", "intermediates", "manifests", "full").getFile());
    delete(FileFsFile.from("build", "intermediates", "manifests", "aapt").getFile());
    delete(FileFsFile.from("build", "intermediates", "res", "merged").getFile());

    delete(FileFsFile.from("custom_build", "intermediates", "res").getFile());
    delete(FileFsFile.from("custom_build", "intermediates", "assets").getFile());
    delete(FileFsFile.from("custom_build", "intermediates", "manifests").getFile());
  }

  @Test
  public void getAppManifest_withOverriddenConfigAssetDir_shouldCreateManifest() throws Exception {
    final AndroidManifest manifest = createManifest(
            configBuilder.setConstants(BuildConfig.class)
                    .setAssetDir("../../src/test/resources/assets")
                    .setManifest("GradleManifest.xml")
                    .build());

    assertThat(manifest.getPackageName()).isEqualTo("org.robolectric.gradleapp");
    assertThat(manifest.getResDirectory()).isEqualTo(file("build/intermediates/res/flavor1/type1"));
    assertThat(manifest.getAssetsDirectory()).isEqualTo(file("build/intermediates/../../src/test/resources/assets"));
    assertThat(manifest.getAndroidManifestFile()).isEqualTo(file("build/intermediates/manifests/full/flavor1/type1/GradleManifest.xml"));
  }

  @Test
  public void getAppManifest_withOverriddenConfigManifest_shouldCreateManifest() throws Exception {
    final AndroidManifest manifest = createManifest(
        configBuilder.setConstants(BuildConfig.class)
            .setManifest("TestAndroidManifest.xml")
            .build());

    assertThat(manifest.getAndroidManifestFile().getPath()).isEqualTo(
        getClass().getClassLoader().getResource("TestAndroidManifest.xml").getPath());
    assertThat(manifest.getPackageName()).isEqualTo("org.robolectric.gradleapp");
    assertThat(manifest.getResDirectory()).isEqualTo(file("build/intermediates/res/flavor1/type1"));
    assertThat(manifest.getAssetsDirectory()).isEqualTo(file("build/intermediates/assets/flavor1/type1"));
  }

  @Test
  public void getAppManifest_forApplications_shouldCreateManifest() throws Exception {
    final AndroidManifest manifest = createManifest(
        configBuilder.setConstants(BuildConfig.class)
            .setManifest("GradleManifest.xml").build());

    assertThat(manifest.getPackageName()).isEqualTo("org.robolectric.gradleapp");
    assertThat(manifest.getResDirectory()).isEqualTo(file("build/intermediates/res/flavor1/type1"));
    assertThat(manifest.getAssetsDirectory()).isEqualTo(file("build/intermediates/assets/flavor1/type1"));
    assertThat(manifest.getAndroidManifestFile()).isEqualTo(file("build/intermediates/manifests/full/flavor1/type1/GradleManifest.xml"));
  }

  @Test
  public void getAppManifest_forLibraries_shouldCreateManifest() throws Exception {
    delete(FileFsFile.from("build", "intermediates", "res").getFile());
    delete(FileFsFile.from("build", "intermediates", "assets").getFile());
    delete(FileFsFile.from("build", "intermediates", "manifests", "full").getFile());

    final AndroidManifest manifest = createManifest(
        configBuilder.setConstants(BuildConfig.class)
            .setManifest("GradleManifest.xml").build());

    assertThat(manifest.getPackageName()).isEqualTo("org.robolectric.gradleapp");
    assertThat(manifest.getResDirectory()).isEqualTo(file("build/intermediates/bundles/flavor1/type1/res"));
    assertThat(manifest.getAssetsDirectory()).isEqualTo(file("build/intermediates/bundles/flavor1/type1/assets"));
    assertThat(manifest.getAndroidManifestFile()).isEqualTo(file("build/intermediates/bundles/flavor1/type1/GradleManifest.xml"));
  }

  @Test
  public void getAppManifest_forAaptLibraries_shouldCreateManifest() throws Exception {
    delete(FileFsFile.from("build", "intermediates", "res").getFile());
    delete(FileFsFile.from("build", "intermediates", "assets").getFile());
    delete(FileFsFile.from("build", "intermediates", "manifests", "full").getFile());
    FileFsFile.from("build", "intermediates", "manifests", "aapt").getFile().mkdirs();

    final AndroidManifest manifest = createManifest(
            configBuilder.setConstants(BuildConfig.class)
                .setManifest("GradleManifest.xml").build());

    assertThat(manifest.getPackageName()).isEqualTo("org.robolectric.gradleapp");
    assertThat(manifest.getResDirectory()).isEqualTo(file("build/intermediates/bundles/flavor1/type1/res"));
    assertThat(manifest.getAssetsDirectory()).isEqualTo(file("build/intermediates/bundles/flavor1/type1/assets"));
    assertThat(manifest.getAndroidManifestFile()).isEqualTo(file("build/intermediates/manifests/aapt/flavor1/type1/GradleManifest.xml"));
  }

  @Test
  public void getAppManifest_shouldCreateManifestWithMethodOverrides() throws Exception {
    final AndroidManifest manifest = createManifest(
        configBuilder.setConstants(BuildConfigOverride.class)
            .setManifest("GradleManifest.xml").build());

    assertThat(manifest.getResDirectory()).isEqualTo(file("build/intermediates/res/flavor2/type2"));
    assertThat(manifest.getAssetsDirectory()).isEqualTo(file("build/intermediates/assets/flavor2/type2"));
    assertThat(manifest.getAndroidManifestFile()).isEqualTo(file("build/intermediates/manifests/full/flavor2/type2/GradleManifest.xml"));
  }

  @Test
  public void getAppManifest_withBuildDirOverride_shouldCreateManifest() throws Exception {
    final AndroidManifest manifest = createManifest(
        configBuilder.setConstants(BuildConfig.class)
            .setBuildDir("custom_build")
            .setManifest("GradleManifest.xml").build());

    assertThat(manifest.getPackageName()).isEqualTo("org.robolectric.gradleapp");
    assertThat(manifest.getResDirectory()).isEqualTo(file("custom_build/intermediates/res/flavor1/type1"));
    assertThat(manifest.getAssetsDirectory()).isEqualTo(file("custom_build/intermediates/assets/flavor1/type1"));
    assertThat(manifest.getAndroidManifestFile()).isEqualTo(file("custom_build/intermediates/bundles/flavor1/type1/GradleManifest.xml"));
  }

  @Test
  public void getAppManifest_withPackageNameOverride_shouldCreateManifest() throws Exception {
    final AndroidManifest manifest = createManifest(
        configBuilder.setConstants(BuildConfig.class)
            .setPackageName("fake.package.name")
            .setManifest("GradleManifest.xml").build());

    assertThat(manifest.getPackageName()).isEqualTo("fake.package.name");
    assertThat(manifest.getResDirectory()).isEqualTo(file("build/intermediates/res/flavor1/type1"));
    assertThat(manifest.getAssetsDirectory()).isEqualTo(file("build/intermediates/assets/flavor1/type1"));
    assertThat(manifest.getAndroidManifestFile()).isEqualTo(file("build/intermediates/manifests/full/flavor1/type1/GradleManifest.xml"));
  }

  @Test
  public void getAppManifest_withAbiSplitOverride_shouldCreateManifest() throws Exception {
    final AndroidManifest manifest = createManifest(
        configBuilder.setConstants(BuildConfig.class)
            .setAbiSplit("armeabi")
            .setManifest("GradleManifest.xml").build());

    assertThat(manifest.getPackageName()).isEqualTo("org.robolectric.gradleapp");
    assertThat(manifest.getResDirectory()).isEqualTo(file("build/intermediates/res/flavor1/type1"));
    assertThat(manifest.getAssetsDirectory()).isEqualTo(file("build/intermediates/assets/flavor1/type1"));
    assertThat(manifest.getAndroidManifestFile()).isEqualTo(file("build/intermediates/manifests/full/flavor1/armeabi/type1/GradleManifest.xml"));
  }

  @Test
  public void getAppManifest_withMergedResources_shouldHaveMergedResPath() throws Exception {
    FileFsFile.from("build", "intermediates", "res", "merged").getFile().mkdirs();

    final AndroidManifest manifest = createManifest(
        configBuilder.setConstants(BuildConfig.class)
            .setPackageName("fake.package.name")
            .setManifest("GradleManifest.xml").build());

    assertThat(manifest.getPackageName()).isEqualTo("fake.package.name");
    assertThat(manifest.getResDirectory()).isEqualTo(file("build/intermediates/res/merged/flavor1/type1"));
    assertThat(manifest.getAssetsDirectory()).isEqualTo(file("build/intermediates/assets/flavor1/type1"));
    assertThat(manifest.getAndroidManifestFile()).isEqualTo(file("build/intermediates/manifests/full/flavor1/type1/GradleManifest.xml"));
  }

  @Test
  public void rClassShouldBeInTheSamePackageAsBuildConfig() throws Exception {
    File manifestFile = new File(
        "build/intermediates/manifests/full" +
            org.robolectric.gradleapp.BuildConfig.FLAVOR +
            org.robolectric.gradleapp.BuildConfig.BUILD_TYPE,
        "AndroidManifest.xml");
    manifestFile.getParentFile().mkdirs();

    Files.write("<manifest package=\"something\"/>", manifestFile, Charsets.UTF_8);

    AndroidManifest manifest = createManifest(
        configBuilder.setConstants(BuildConfig.class).build());
    assertThat(manifest.getRClass().getPackage().getName()).isEqualTo("org.robolectric.gradleapp");
  }

  @Test public void identify() throws Exception {
    ManifestIdentifier manifestIdentifier = factory.identify(
        configBuilder.setConstants(BuildConfig.class)
            .setManifest("GradleManifest.xml").build());
    
    assertThat(manifestIdentifier.getManifestFile().toString())
        .isEqualTo("build/intermediates/manifests/full/flavor1/type1/GradleManifest.xml");
    assertThat(manifestIdentifier.getResDir().toString())
        .isEqualTo("build/intermediates/res/flavor1/type1");
  }

  ////////////////////////////////

  private AndroidManifest createManifest(Config config) {
    return factory.create(factory.identify(config));
  }

  private static FsFile file(String path) {
    return FileFsFile.from(path);
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

  public static class BuildConfigOverride {
    public static final String APPLICATION_ID = "org.sandwich.bar";
    public static final String BUILD_TYPE = "type2";
    public static final String FLAVOR = "flavor2";
  }
}
