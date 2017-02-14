package org.robolectric.internal;

import edu.emory.mathcs.backport.java.util.Collections;
import org.robolectric.annotation.Config;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.Fs;

import java.util.Properties;

public class DefaultManifestFactory implements ManifestFactory {
  private Properties properties;

  public DefaultManifestFactory(Properties properties) {
    this.properties = properties;
  }

  @Override
  public ManifestIdentifier identify(Config config) {
    return new ManifestIdentifier(
        Fs.fileFromPath(properties.getProperty("android_merged_manifest")),
        Fs.fileFromPath(properties.getProperty("android_merged_resources")),
        Fs.fileFromPath(properties.getProperty("android_merged_assets")),
        null, Collections.emptyList());
  }

  @Override
  public AndroidManifest create(ManifestIdentifier manifestIdentifier) {
    return new AndroidManifest(manifestIdentifier.getManifestFile(), manifestIdentifier.getResDir(), manifestIdentifier.getAssetDir());
  }
}
