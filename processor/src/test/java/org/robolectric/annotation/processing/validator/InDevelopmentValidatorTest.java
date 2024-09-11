package org.robolectric.annotation.processing.validator;

import static com.google.common.truth.Truth.assertAbout;
import static org.robolectric.annotation.processing.validator.SingleClassSubject.singleClass;

import com.example.objects.Dummy;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.versioning.AndroidVersions;

@RunWith(JUnit4.class)
public final class InDevelopmentValidatorTest {

  private static String getClassRootDir(Class<?> clazz) {
    // Get the URL representation of the class location
    URL resource = clazz.getResource(clazz.getSimpleName() + ".class");
    if (resource == null) {
      return null; // dynamic class
    }
    if (resource.getProtocol().equals("file")) {
      File path = new File(URI.create(resource.toString()).getPath()).getParentFile();
      int packagePartSize = clazz.getPackageName().split("\\.").length;
      for (int i = 0; i < packagePartSize; i++) {
        path = path.getParentFile();
      }
      return path.getAbsolutePath();
    } else if (resource.getProtocol().equals("jar")) {
      // Extract path to JAR and find root
      String path = resource.getPath().substring(5, resource.getPath().indexOf("!"));
      File jarFile = new File(URI.create(path).getPath());
      return jarFile.getAbsolutePath();
    } else {
      return null; // Not a supported class location
    }
  }

  @Test
  public void implementationWithInDevelopmentCompiles() {
    AndroidVersions.AndroidRelease unreleased =
        AndroidVersions.getUnreleased().stream()
            .min(AndroidVersions.AndroidRelease::compareTo)
            .get();
    final String testClass =
        "org.robolectric.annotation.processing.shadows.ShadowImplementsInDevelopment";
    HashMap<String, String> props = new HashMap<>();
    props.put("org.robolectric.annotation.processing.sdkCheckMode", "ERROR");
    props.put("org.robolectric.annotation.processing.validateCompileSdk", "true");
    assertAbout(singleClass(props, getClassRootDir(Dummy.class), unreleased.getSdkInt()))
        .that(testClass)
        .compilesWithoutError();
  }

  @Test
  public void implementationWithInDevelopmentFailsToCompilesOnLastReleaseDisableInDevelopment() {
    AndroidVersions.AndroidRelease lastRelease =
        AndroidVersions.getReleases().stream().max(AndroidVersions.AndroidRelease::compareTo).get();
    final String testClass =
        "org.robolectric.annotation.processing.shadows.ShadowImplementsInDevelopment";
    HashMap<String, String> props = new HashMap<>();
    props.put("org.robolectric.annotation.processing.sdkCheckMode", "ERROR");
    props.put("org.robolectric.annotation.processing.disableInDevelopment", "true");
    props.put("org.robolectric.annotation.processing.validateCompileSdk", "true");
    assertAbout(singleClass(props, getClassRootDir(Dummy.class), lastRelease.getSdkInt()))
        .that(testClass)
        .failsToCompile()
        .withErrorContaining(
            "No method doSomething() in com.example.objects.Dummy for SDK "
                + lastRelease.getSdkInt())
        .onLine(13);
  }

  @Test
  public void implementationWithoutInDevelopmentFailsToCompiles() {
    AndroidVersions.AndroidRelease unreleased =
        AndroidVersions.getUnreleased().stream()
            .min(AndroidVersions.AndroidRelease::compareTo)
            .get();
    final String testClass =
        "org.robolectric.annotation.processing.shadows.ShadowImplementsInDevelopmentMissing";
    HashMap<String, String> props = new HashMap<>();
    props.put("org.robolectric.annotation.processing.sdkCheckMode", "ERROR");
    props.put("org.robolectric.annotation.processing.validateCompileSdk", "true");
    assertAbout(singleClass(props, getClassRootDir(Dummy.class), unreleased.getSdkInt()))
        .that(testClass)
        .failsToCompile()
        .withErrorContaining(
            "No method doSomething() in com.example.objects.Dummy for SDK "
                + unreleased.getSdkInt())
        .onLine(11);
  }

  @Test
  public void implementationWithoutInDevelopmentFailsToCompilesLastRelease() {
    AndroidVersions.AndroidRelease unreleased =
        AndroidVersions.getReleases().stream().min(AndroidVersions.AndroidRelease::compareTo).get();
    final String testClass =
        "org.robolectric.annotation.processing.shadows.ShadowImplementsInDevelopmentMissing";
    HashMap<String, String> props = new HashMap<>();
    props.put("org.robolectric.annotation.processing.sdkCheckMode", "ERROR");
    props.put("org.robolectric.annotation.processing.validateCompileSdk", "true");
    assertAbout(singleClass(props, getClassRootDir(Dummy.class), unreleased.getSdkInt()))
        .that(testClass)
        .failsToCompile()
        .withErrorContaining(
            "No method doSomething() in com.example.objects.Dummy for SDK "
                + unreleased.getSdkInt())
        .onLine(11);
  }
}
