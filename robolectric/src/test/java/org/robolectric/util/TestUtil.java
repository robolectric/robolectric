package org.robolectric.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

import android.app.Application;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.robolectric.R;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.internal.SdkConfig;
import org.robolectric.internal.dependency.DependencyResolver;
import org.robolectric.internal.dependency.LocalDependencyResolver;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.Fs;
import org.robolectric.res.FsFile;
import org.robolectric.res.ResourcePath;

public abstract class TestUtil {
  private static ResourcePath SYSTEM_RESOURCE_PATH;
  public static final ResourcePath TEST_RESOURCE_PATH = new ResourcePath(R.class, resourceFile("res"), resourceFile("assets"));
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

  public static String joinPath(String... parts) {
    File file = new File(parts[0]);
    for (int i = 1; i < parts.length; i++) {
      String part = parts[i];
      file = new File(file, part);
    }
    return file.getPath();
  }

  public static File newFile(File file, String contents) throws IOException {
    FileWriter fileWriter = new FileWriter(file);
    try {
      fileWriter.write(contents);
    } finally {
      fileWriter.close();
    }
    return file;
  }

  public static String stringify(Config config) {
    int[] sdk = config.sdk();
    String manifest = config.manifest();
    Class<? extends Application> application = config.application();
    String packageName = config.packageName();
    String qualifiers = config.qualifiers();
    String resourceDir = config.resourceDir();
    String assetsDir = config.assetDir();
    Class<?>[] shadows = config.shadows();
    String[] instrumentedPackages = config.instrumentedPackages();
    String[] libraries = config.libraries();
    Class<?> constants = config.constants();
    return stringify(sdk, manifest, application, packageName, qualifiers, resourceDir, assetsDir, shadows, instrumentedPackages, libraries, constants);
  }

  public static String stringify(int[] sdk, String manifest, Class<? extends Application> application, String packageName, String qualifiers, String resourceDir, String assetsDir, Class<?>[] shadows, String[] instrumentedPackages, String[] libraries, Class<?> constants) {
      String[] stringClasses = new String[shadows.length];
      for (int i = 0; i < stringClasses.length; i++) {
          stringClasses[i] = shadows[i].toString();
      }

      Arrays.sort(stringClasses);

      String[] sortedLibraries = libraries.clone();
      Arrays.sort(sortedLibraries);

      String[] sortedInstrumentedPackages = instrumentedPackages.clone();
      Arrays.sort(sortedInstrumentedPackages);

      return "sdk=" + Arrays.toString(sdk) + "\n" +
        "manifest=" + manifest + "\n" +
        "application=" + application + "\n" +
        "packageName=" + packageName + "\n" +
        "qualifiers=" + qualifiers + "\n" +
        "resourceDir=" + resourceDir + "\n" +
        "assetDir=" + assetsDir + "\n" +
        "shadows=" + Arrays.toString(stringClasses) + "\n" +
        "instrumentedPackages" + Arrays.toString(sortedInstrumentedPackages) + "\n" +
        "libraries=" + Arrays.toString(sortedLibraries) + "\n" +
        "constants=" + constants;
  }

  public static void assertStringsInclude(List<String> list, String... expectedStrings) {
    List<String> original = new ArrayList<>(list);
    for (String expectedEvent : expectedStrings) {
      int index = list.indexOf(expectedEvent);
      if (index == -1) {
        assertThat(original).containsExactly(expectedStrings);
      }
      list.subList(0, index + 1).clear();
    }
  }

  private static DependencyResolver getDependencyResolver() {
    if (Boolean.getBoolean("robolectric.offline")) {
      String dependencyDir = System.getProperty("robolectric.dependency.dir", ".");
      return new LocalDependencyResolver(new File(dependencyDir));
    } else {
      Class<?> mavenDependencyResolverClass = ReflectionHelpers.loadClass(RobolectricTestRunner.class.getClassLoader(),
              "org.robolectric.internal.dependency.MavenDependencyResolver");
      return (DependencyResolver) ReflectionHelpers.callConstructor(mavenDependencyResolverClass);
    }
  }
}
