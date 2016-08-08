package org.robolectric;

import android.app.Application;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;

public class GradleConfigFactory {
  private Config config;

  public GradleConfigFactory(Config config) {
    this.config = config;
  }

  public Config buildConfig() {
    return new Config.Implementation(new int[0], Config.DEFAULT_MANIFEST, Config.DEFAULT_QUALIFIERS,
        getBuildType(), getFlavor(), getPackageName(), Config.DEFAULT_ABI_SPLIT,
        Config.DEFAULT_RES_FOLDER, Config.DEFAULT_ASSET_FOLDER, Config.DEFAULT_BUILD_FOLDER, new Class[0],
        new String[0], Application.class, new String[0], Config.DEFAULT_CONSTANTS);
  }

  private String getBuildType() {
    try {
      return ReflectionHelpers.getStaticField(config.constants(), "BUILD_TYPE");
    } catch (Throwable e) {
      return null;
    }
  }

  private String getFlavor() {
    try {
      return ReflectionHelpers.getStaticField(config.constants(), "FLAVOR");
    } catch (Throwable e) {
      return null;
    }
  }

  private String getPackageName() {
    try {
      return ReflectionHelpers.getStaticField(config.constants(), "APPLICATION_ID");
    } catch (Throwable e) {
      return null;
    }
  }
}
