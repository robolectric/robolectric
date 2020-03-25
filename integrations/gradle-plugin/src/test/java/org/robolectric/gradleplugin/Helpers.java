package org.robolectric.gradleplugin;

import static org.junit.Assert.fail;

import java.io.File;
import org.jetbrains.annotations.NotNull;

public class Helpers {
  public static String findAndroidSdkRoot() {
    String androidSdkRoot = System.getenv("ANDROID_SDK_ROOT");
    if (androidSdkRoot == null) {
      fail("ANDROID_SDK_ROOT should've been set by gradle-plugin's build.gradle.");
    }
    return androidSdkRoot;
  }

  @NotNull
  public static File findTestProject() {
    return new File(Helpers.class.getResource("/testProject").getFile());
  }
}
