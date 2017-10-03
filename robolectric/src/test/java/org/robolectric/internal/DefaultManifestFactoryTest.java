package org.robolectric.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Properties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Config.Builder;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.FileFsFile;

@RunWith(JUnit4.class)
public class DefaultManifestFactoryTest {

  @Test
  public void identify() {
    Properties properties = new Properties();
    properties.put("android_merged_manifest", "gradle/AndroidManifest.xml");
    properties.put("android_merged_resources", "gradle/res");
    properties.put("android_merged_assets", "gradle/assets");
    DefaultManifestFactory factory = new DefaultManifestFactory(properties);
    ManifestIdentifier identifier = factory.identify(Builder.defaults().build());
    AndroidManifest manifest = factory.create(identifier);

    assertThat(manifest.getAndroidManifestFile())
        .isEqualTo(FileFsFile.from("gradle/AndroidManifest.xml"));
    assertThat(manifest.getResDirectory())
        .isEqualTo(FileFsFile.from("gradle/res"));
    assertThat(manifest.getAssetsDirectory())
        .isEqualTo(FileFsFile.from("gradle/assets"));
  }

  @Test
  public void identify_configNoneShouldBeIgnored() throws Exception {
    Properties properties = new Properties();
    properties.put("android_merged_manifest", "gradle/AndroidManifest.xml");
    properties.put("android_merged_resources", "gradle/res");
    properties.put("android_merged_assets", "gradle/assets");
    properties.put("android_custom_package", "com.example.app");
    DefaultManifestFactory factory = new DefaultManifestFactory(properties);
    ManifestIdentifier identifier = factory.identify(Builder.defaults().setManifest(Config.NONE).build());
    AndroidManifest manifest = factory.create(identifier);

    assertThat(manifest.getAndroidManifestFile())
        .isEqualTo(FileFsFile.from("gradle/AndroidManifest.xml"));
    assertThat(manifest.getResDirectory())
        .isEqualTo(FileFsFile.from("gradle/res"));
    assertThat(manifest.getAssetsDirectory())
        .isEqualTo(FileFsFile.from("gradle/assets"));
    assertThat(manifest.getRClassName()).isEqualTo("com.example.app.R");
  }

  @Test
  public void identify_packageCanBeOverridenFromConfig() throws Exception {
    Properties properties = new Properties();
    properties.put("android_merged_manifest", "gradle/AndroidManifest.xml");
    properties.put("android_merged_resources", "gradle/res");
    properties.put("android_merged_assets", "gradle/assets");
    DefaultManifestFactory factory = new DefaultManifestFactory(properties);
    ManifestIdentifier identifier = factory.identify(Builder.defaults().setPackageName("overridden.package").build());
    AndroidManifest manifest = factory.create(identifier);

    assertThat(manifest.getAndroidManifestFile())
        .isEqualTo(FileFsFile.from("gradle/AndroidManifest.xml"));
    assertThat(manifest.getResDirectory())
        .isEqualTo(FileFsFile.from("gradle/res"));
    assertThat(manifest.getAssetsDirectory())
        .isEqualTo(FileFsFile.from("gradle/assets"));
    assertThat(manifest.getRClassName())
        .isEqualTo("overridden.package.R");
  }
}
