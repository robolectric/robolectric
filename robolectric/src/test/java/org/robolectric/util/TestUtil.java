package org.robolectric.util;

import org.robolectric.R;
import org.robolectric.internal.SdkConfig;
import org.robolectric.internal.dependency.MavenDependencyResolver;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.Fs;
import org.robolectric.res.FsFile;
import org.robolectric.res.ResourcePath;

import java.io.*;
import java.util.Collection;

import static org.junit.Assert.assertTrue;

public abstract class TestUtil {
  private static ResourcePath SYSTEM_RESOURCE_PATH;
  public static final ResourcePath TEST_RESOURCE_PATH = new ResourcePath(R.class.getPackage().getName(), resourceFile("res"), resourceFile("assets"), R.class);
  public static final String TEST_PACKAGE = R.class.getPackage().getName();
  public static File testDirLocation;

  public static void assertEquals(Collection<?> expected, Collection<?> actual) {
    org.junit.Assert.assertEquals(stringify(expected), stringify(actual));
  }

  public static String stringify(Collection<?> collection) {
    StringBuilder buf = new StringBuilder();
    for (Object o : collection) {
      if (buf.length() > 0) buf.append("\n");
      buf.append(o);
    }
    return buf.toString();
  }

  public static <T> void assertInstanceOf(Class<? extends T> expectedClass, T object) {
    Class actualClass = object.getClass();
    assertTrue(expectedClass + " should be assignable from " + actualClass,
        expectedClass.isAssignableFrom(actualClass));
  }

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
    return new ResourcePath("org.robolectric.lib1", resourceFile("lib1/res"), resourceFile("lib1/assets"), org.robolectric.lib1.R.class);
  }

  public static ResourcePath lib2Resources() {
    return new ResourcePath("org.robolectric.lib2", resourceFile("lib2/res"), resourceFile("lib2/assets"), org.robolectric.lib2.R.class);
  }

  public static ResourcePath lib3Resources() {
    return new ResourcePath("org.robolectric.lib3", resourceFile("lib3/res"), resourceFile("lib3/assets"), org.robolectric.lib3.R.class);
  }

  public static ResourcePath systemResources() {
    if (SYSTEM_RESOURCE_PATH == null) {
      SdkConfig sdkConfig = new SdkConfig(SdkConfig.FALLBACK_SDK_VERSION);
      Fs fs = Fs.fromJar(new MavenDependencyResolver().getLocalArtifactUrl(sdkConfig.getAndroidSdkDependency()));
      SYSTEM_RESOURCE_PATH = new ResourcePath("android", fs.join("res"), fs.join("assets"), android.R.class);
    }
    return SYSTEM_RESOURCE_PATH;
  }

  public static ResourcePath gradleAppResources() {
    return new ResourcePath("org.robolectric.gradleapp", resourceFile("gradle/res/layoutFlavor/menuBuildType"), resourceFile("gradle/assets/layoutFlavor/menuBuildType"), org.robolectric.gradleapp.R.class);
  }

  public static AndroidManifest newConfig(String androidManifestFile) {
    return new AndroidManifest(resourceFile(androidManifestFile), null, null);
  }

  public static String readString(InputStream is) throws IOException {
    Writer writer = new StringWriter();
    char[] buffer = new char[1024];
    try {
      Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
      int n;
      while ((n = reader.read(buffer)) != -1) {
        writer.write(buffer, 0, n);
      }
    } finally {
      is.close();
    }
    return writer.toString();
  }

  public static String joinPath(String... parts) {
    File file = new File(parts[0]);
    for (int i = 1; i < parts.length; i++) {
      String part = parts[i];
      file = new File(file, part);
    }
    return file.getPath();
  }
}
