package org.robolectric.internal;

import static com.google.common.truth.Truth.assertThat;

import java.util.Properties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Config.Builder;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.FileFsFile;

@RunWith(JUnit4.class)
public class DefaultManifestFactoryTest {

  @Test
  public void identify() {
    Properties properties = new Properties();
    properties.put("android_merged_manifest", "/gradle/AndroidManifest.xml");
    properties.put("android_merged_resources", "/gradle/res");
    properties.put("android_merged_assets", "/gradle/assets");
    DefaultManifestFactory factory = new DefaultManifestFactory(properties);
    ManifestIdentifier identifier = factory.identify(Builder.defaults().build());
    AndroidManifest manifest = RobolectricTestRunner.createAndroidManifest(identifier);

    String baseDir = System.getProperty("robolectric-tests.base-dir");
    if (baseDir == null) {
      baseDir = "";
    }

    assertThat(manifest.getAndroidManifestFile())
        .isEqualTo(FileFsFile.from(baseDir + "/gradle/AndroidManifest.xml"));
    assertThat(manifest.getResDirectory())
        .isEqualTo(FileFsFile.from(baseDir + "/gradle/res"));
    assertThat(manifest.getAssetsDirectory())
        .isEqualTo(FileFsFile.from(baseDir + "/gradle/assets"));
    assertThat(manifest.getApkFile()).isNull();
  }

  @Test
  public void identify_withResourceApk() {
    Properties properties = new Properties();
    properties.put("android_merged_manifest", "/gradle/AndroidManifest.xml");
    properties.put("android_merged_resources", "/gradle/res");
    properties.put("android_merged_assets", "/gradle/assets");
    properties.put("android_resource_apk", "/gradle/resources.ap_");
    DefaultManifestFactory factory = new DefaultManifestFactory(properties);
    ManifestIdentifier identifier = factory.identify(Builder.defaults().build());
    AndroidManifest manifest = RobolectricTestRunner.createAndroidManifest(identifier);

    String baseDir = System.getProperty("robolectric-tests.base-dir");
    if (baseDir == null) {
      baseDir = "";
    }

    assertThat(manifest.getAndroidManifestFile())
        .isEqualTo(FileFsFile.from(baseDir + "/gradle/AndroidManifest.xml"));
    assertThat(manifest.getResDirectory())
        .isEqualTo(FileFsFile.from(baseDir + "/gradle/res"));
    assertThat(manifest.getAssetsDirectory())
        .isEqualTo(FileFsFile.from(baseDir + "/gradle/assets"));
    assertThat(manifest.getApkFile())
        .isEqualTo(FileFsFile.from(baseDir + "/gradle/resources.ap_"));
  }

  @Test
  public void identify_withMissingValues() {
    Properties properties = new Properties();
    properties.put("android_merged_manifest", "");
    properties.put("android_merged_assets", "/gradle/assets");
    properties.put("android_resource_apk", "/gradle/resources.ap_");
    DefaultManifestFactory factory = new DefaultManifestFactory(properties);
    ManifestIdentifier identifier = factory.identify(Builder.defaults().build());
    AndroidManifest manifest = RobolectricTestRunner.createAndroidManifest(identifier);

    String baseDir = System.getProperty("robolectric-tests.base-dir");
    if (baseDir == null) {
      baseDir = "";
    }

    assertThat(manifest.getAndroidManifestFile()).isNull();
    assertThat(manifest.getResDirectory()).isNull();
    assertThat(manifest.getAssetsDirectory())
        .isEqualTo(FileFsFile.from(baseDir + "/gradle/assets"));
    assertThat(manifest.getApkFile())
        .isEqualTo(FileFsFile.from(baseDir + "/gradle/resources.ap_"));
  }

  @Test
  public void identify_configNoneShouldBeIgnored() throws Exception {
    Properties properties = new Properties();
    properties.put("android_merged_manifest", "/gradle/AndroidManifest.xml");
    properties.put("android_merged_resources", "/gradle/res");
    properties.put("android_merged_assets", "/gradle/assets");
    properties.put("android_custom_package", "com.example.app");
    DefaultManifestFactory factory = new DefaultManifestFactory(properties);
    ManifestIdentifier identifier = factory.identify(Builder.defaults().setManifest(Config.NONE).build());
    AndroidManifest manifest = RobolectricTestRunner.createAndroidManifest(identifier);

    String baseDir = System.getProperty("robolectric-tests.base-dir");
    if (baseDir == null) {
      baseDir = "";
    }

    assertThat(manifest.getAndroidManifestFile())
        .isEqualTo(FileFsFile.from(baseDir + "/gradle/AndroidManifest.xml"));
    assertThat(manifest.getResDirectory())
        .isEqualTo(FileFsFile.from(baseDir + "/gradle/res"));
    assertThat(manifest.getAssetsDirectory())
        .isEqualTo(FileFsFile.from(baseDir + "/gradle/assets"));
    assertThat(manifest.getRClassName()).isEqualTo("com.example.app.R");
  }

  @Test
  public void identify_packageCanBeOverridenFromConfig() throws Exception {
    Properties properties = new Properties();
    properties.put("android_merged_manifest", "/gradle/AndroidManifest.xml");
    properties.put("android_merged_resources", "/gradle/res");
    properties.put("android_merged_assets", "/gradle/assets");
    DefaultManifestFactory factory = new DefaultManifestFactory(properties);
    ManifestIdentifier identifier = factory.identify(Builder.defaults().setPackageName("overridden.package").build());
    AndroidManifest manifest = RobolectricTestRunner.createAndroidManifest(identifier);

    String baseDir = System.getProperty("robolectric-tests.base-dir");
    if (baseDir == null) {
      baseDir = "";
    }

    assertThat(manifest.getAndroidManifestFile())
        .isEqualTo(FileFsFile.from(baseDir + "/gradle/AndroidManifest.xml"));
    assertThat(manifest.getResDirectory())
        .isEqualTo(FileFsFile.from(baseDir + "/gradle/res"));
    assertThat(manifest.getAssetsDirectory())
        .isEqualTo(FileFsFile.from(baseDir + "/gradle/assets"));
    assertThat(manifest.getRClassName())
        .isEqualTo("overridden.package.R");
  }
}