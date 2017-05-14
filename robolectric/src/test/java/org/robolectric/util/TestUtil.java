package org.robolectric.util;

import javax.annotation.Nonnull;
import org.robolectric.R;
import org.robolectric.internal.SdkConfig;
import org.robolectric.internal.dependency.MavenDependencyResolver;
import org.robolectric.res.Fs;
import org.robolectric.res.FsFile;
import org.robolectric.res.ResourcePath;

import java.io.*;

public abstract class TestUtil {
  private static ResourcePath SYSTEM_RESOURCE_PATH;
  public static final ResourcePath TEST_RESOURCE_PATH = new ResourcePath(R.class, resourceFile("res"), resourceFile("assets"));
  public static final String TEST_PACKAGE = R.class.getPackage().getName();
  private static File testDirLocation;

  public static FsFile resourcesBaseDir() {
    return Fs.newFile(resourcesBaseDirFile());
  }

  private static File resourcesBaseDirFile() {
    if (testDirLocation == null) {
      File testDir = Util.file("src", "test", "resources");
      if (hasTestManifest(testDir)) return testDirLocation = testDir;

      File roboTestDir = Util.file("robolectric", "src", "test", "resources");
      if (hasTestManifest(roboTestDir)) return testDirLocation = roboTestDir;

      File submoduleDir = Util.file("submodules", "robolectric", "src", "test", "resources");
      if (hasTestManifest(submoduleDir)) return testDirLocation = submoduleDir;

      //required for robolectric-sqlite to find resources to test against
      File roboSiblingTestDir = Util.file(new File(new File(".").getAbsolutePath()).getParentFile().getParentFile(), "robolectric", "src", "test", "resources");
      if (hasTestManifest(roboSiblingTestDir)) return testDirLocation = roboSiblingTestDir;

      throw new RuntimeException("can't find your TestAndroidManifest.xml in "
          + testDir.getAbsolutePath() + " or " + roboTestDir.getAbsolutePath() + "\n or " + roboSiblingTestDir.getAbsolutePath());
    } else {
      return testDirLocation;
    }
  }

  private static boolean hasTestManifest(File testDir) {
    return new File(testDir, "TestAndroidManifest.xml").isFile();
  }

  public static FsFile resourceFile(String... pathParts) {
    return resourcesBaseDir().join(pathParts);
  }

  public static ResourcePath testResources() {
    return TEST_RESOURCE_PATH;
  }

  public static ResourcePath lib1Resources() {
    return new ResourcePath(org.robolectric.lib1.R.class, resourceFile("lib1/res"), resourceFile("lib1/assets"));
  }

  public static ResourcePath lib2Resources() {
    return new ResourcePath(org.robolectric.lib2.R.class, resourceFile("lib2/res"), resourceFile("lib2/assets"));
  }

  public static ResourcePath lib3Resources() {
    return new ResourcePath(org.robolectric.lib3.R.class, resourceFile("lib3/res"), resourceFile("lib3/assets"));
  }

  public static ResourcePath systemResources() {
    if (SYSTEM_RESOURCE_PATH == null) {
      SdkConfig sdkConfig = new SdkConfig(SdkConfig.MAX_SDK_VERSION);
      Fs fs = Fs.fromJar(new MavenDependencyResolver().getLocalArtifactUrl(sdkConfig.getAndroidSdkDependency()));
      SYSTEM_RESOURCE_PATH = new ResourcePath(android.R.class, fs.join("res"), fs.join("assets"));
    }
    return SYSTEM_RESOURCE_PATH;
  }

  @Nonnull
  public static ResourcePath sdkResources(int apiLevel) {
    Fs sdkResFs = Fs.fromJar(new MavenDependencyResolver().getLocalArtifactUrl(new SdkConfig(apiLevel).getAndroidSdkDependency()));
    return new ResourcePath(null, sdkResFs.join("res"), null, null);
  }


  public static ResourcePath gradleAppResources() {
    return new ResourcePath(org.robolectric.gradleapp.R.class, resourceFile("gradle/res/layoutFlavor/menuBuildType"), resourceFile("gradle/assets/layoutFlavor/menuBuildType"));
  }
}
