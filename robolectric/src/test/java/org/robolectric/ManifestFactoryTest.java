package org.robolectric;

import static com.google.common.truth.Truth.assertThat;

import java.util.Properties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.annotation.Config;
import org.robolectric.internal.DefaultManifestFactory;
import org.robolectric.internal.ManifestFactory;
import org.robolectric.internal.ManifestIdentifier;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.Fs;

@RunWith(JUnit4.class)
public class ManifestFactoryTest {

  @Test
  public void whenBuildSystemApiPropertiesFileIsPresent_shouldUseDefaultManifestFactory() throws Exception {
    final Properties properties = new Properties();
    properties.setProperty("android_sdk_home", "");
    properties.setProperty("android_merged_manifest", "/path/to/MergedManifest.xml");
    properties.setProperty("android_merged_resources", "/path/to/merged-resources");
    properties.setProperty("android_merged_assets", "/path/to/merged-assets");

    RobolectricTestRunner testRunner =
        new RobolectricTestRunner(ManifestFactoryTest.class) {
          @Override
          protected Properties getBuildSystemApiProperties() {
            return properties;
          }
        };

    String baseDir = System.getProperty("robolectric-tests.base-dir");
    if (baseDir == null) {
      baseDir = "";
    }

    Config.Implementation config = Config.Builder.defaults().build();
    ManifestFactory manifestFactory = testRunner.getManifestFactory(config);
    assertThat(manifestFactory).isInstanceOf(DefaultManifestFactory.class);
    ManifestIdentifier manifestIdentifier = manifestFactory.identify(config);
    assertThat(manifestIdentifier.getManifestFile())
        .isEqualTo(Fs.fileFromPath(baseDir + "/path/to/MergedManifest.xml"));
    assertThat(manifestIdentifier.getResDir())
        .isEqualTo(Fs.fileFromPath(baseDir + "/path/to/merged-resources"));
    assertThat(manifestIdentifier.getAssetDir())
        .isEqualTo(Fs.fileFromPath(baseDir + "/path/to/merged-assets"));
    assertThat(manifestIdentifier.getLibraries()).isEmpty();
    assertThat(manifestIdentifier.getPackageName()).isNull();

    AndroidManifest androidManifest = RobolectricTestRunner
        .createAndroidManifest(manifestIdentifier);
    assertThat(androidManifest.getAndroidManifestFile())
        .isEqualTo(Fs.fileFromPath(baseDir + "/path/to/MergedManifest.xml"));
    assertThat(androidManifest.getResDirectory())
        .isEqualTo(Fs.fileFromPath(baseDir + "/path/to/merged-resources"));
    assertThat(androidManifest.getAssetsDirectory())
        .isEqualTo(Fs.fileFromPath(baseDir + "/path/to/merged-assets"));
  }

  @Test
  public void whenConfigSpecified_overridesValuesFromFile() throws Exception {
    final Properties properties = new Properties();
    properties.setProperty("android_sdk_home", "");
    properties.setProperty("android_merged_manifest", "/path/to/MergedManifest.xml");
    properties.setProperty("android_merged_resources", "/path/to/merged-resources");
    properties.setProperty("android_merged_assets", "/path/to/merged-assets");

    RobolectricTestRunner testRunner =
        new RobolectricTestRunner(ManifestFactoryTest.class) {
          @Override
          protected Properties getBuildSystemApiProperties() {
            return properties;
          }
        };

    String baseDir = System.getProperty("robolectric-tests.base-dir");
    if (baseDir == null) {
      baseDir = "";
    }

    Config.Implementation config = Config.Builder.defaults()
        .setManifest("TestAndroidManifest.xml")
        .setPackageName("another.package")
        .build();
    ManifestFactory manifestFactory = testRunner.getManifestFactory(config);
    assertThat(manifestFactory).isInstanceOf(DefaultManifestFactory.class);
    ManifestIdentifier manifestIdentifier = manifestFactory.identify(config);
    assertThat(manifestIdentifier.getManifestFile())
        .isEqualTo(Fs.fromURL(getClass().getClassLoader().getResource("TestAndroidManifest.xml")));
    assertThat(manifestIdentifier.getResDir())
        .isEqualTo(Fs.fileFromPath(baseDir + "/path/to/merged-resources"));
    assertThat(manifestIdentifier.getAssetDir())
        .isEqualTo(Fs.fileFromPath(baseDir + "/path/to/merged-assets"));
    assertThat(manifestIdentifier.getLibraries()).isEmpty();
    assertThat(manifestIdentifier.getPackageName()).isEqualTo("another.package");
  }
}