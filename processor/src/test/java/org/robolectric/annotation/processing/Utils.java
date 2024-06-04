package org.robolectric.annotation.processing;

import static com.google.testing.compile.JavaFileObjects.forResource;
import static org.robolectric.annotation.processing.RobolectricProcessor.JSON_DOCS_DIR;
import static org.robolectric.annotation.processing.RobolectricProcessor.PACKAGE_OPT;
import static org.robolectric.annotation.processing.RobolectricProcessor.SDK_CHECK_MODE;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import java.io.File;
import java.net.URI;
import java.net.URL;
import javax.tools.JavaFileObject;

public class Utils {

  public static final ImmutableMap<String, String> DEFAULT_OPTS =
      ImmutableMap.<String, String>builder()
          .put(PACKAGE_OPT, "org.robolectric")
          .put(JSON_DOCS_DIR, Files.createTempDir().toString())
          .put(SDK_CHECK_MODE, "OFF")
          .build();

  public static final JavaFileObject SHADOW_PROVIDER_SOURCE =
      forResource("mock-source/org/robolectric/internal/ShadowProvider.java");
  public static final JavaFileObject SHADOW_EXTRACTOR_SOURCE =
      forResource("mock-source/org/robolectric/shadow/api/Shadow.java");

  public static String toResourcePath(String clazzName) {
    return clazzName.replace('.', '/') + ".java";
  }

  public static String getClassRootDir(Class<?> clazz) {
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
}
