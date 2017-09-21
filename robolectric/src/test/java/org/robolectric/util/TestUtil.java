package org.robolectric.util;

import org.robolectric.R;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.internal.SdkConfig;
import org.robolectric.internal.dependency.DependencyResolver;
import org.robolectric.internal.dependency.LocalDependencyResolver;
import org.robolectric.internal.dependency.PropertiesDependencyResolver;
import org.robolectric.manifest.AndroidManifest;
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
      String baseDir = System.getProperty("robolectric-tests.base-dir");
      if (baseDir != null) {
        return testDirLocation = new File(baseDir, "src/test/resources");
      }

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
      Fs fs = Fs.fromJar(getDependencyResolver().getLocalArtifactUrl(sdkConfig.getAndroidSdkDependency()));
      SYSTEM_RESOURCE_PATH = new ResourcePath(android.R.class, fs.join("res"), fs.join("assets"));
    }
    return SYSTEM_RESOURCE_PATH;
  }

  public static ResourcePath sdkResources(int apiLevel) {
    Fs sdkResFs = Fs.fromJar(getDependencyResolver().getLocalArtifactUrl(new SdkConfig(apiLevel).getAndroidSdkDependency()));
    return new ResourcePath(null, sdkResFs.join("res"), null, null);
  }

  public static ResourcePath gradleAppResources() {
    return new ResourcePath(org.robolectric.gradleapp.R.class, resourceFile("gradle/res/layoutFlavor/menuBuildType"), resourceFile("gradle/assets/layoutFlavor/menuBuildType"));
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

  private static DependencyResolver getDependencyResolver() {

    if (Boolean.getBoolean("robolectric.offline")) {
      String propPath = System.getProperty("robolectric-deps.properties");
      if (propPath != null) {
        try {
          return new PropertiesDependencyResolver(
              Fs.newFile(propPath),
              null);
        } catch (IOException e) {
          throw new RuntimeException("couldn't read dependencies" , e);
        }
      } else {
        String dependencyDir = System.getProperty("robolectric.dependency.dir", ".");
        return new LocalDependencyResolver(new File(dependencyDir));
      }
    } else {
      Class<?> mavenDependencyResolverClass = ReflectionHelpers.loadClass(RobolectricTestRunner.class.getClassLoader(),
              "org.robolectric.internal.dependency.MavenDependencyResolver");
      return (DependencyResolver) ReflectionHelpers.callConstructor(mavenDependencyResolverClass);
    }
  }
}
