package org.robolectric.annotation.processing;

import static com.google.testing.compile.JavaFileObjects.forResource;
import static org.robolectric.annotation.processing.RobolectricProcessor.JSON_DOCS_DIR;
import static org.robolectric.annotation.processing.RobolectricProcessor.PACKAGE_OPT;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import javax.tools.JavaFileObject;

public class Utils {

  public static final ImmutableMap<String, String> DEFAULT_OPTS =
      ImmutableMap.of(
          PACKAGE_OPT, "org.robolectric",
          JSON_DOCS_DIR, Files.createTempDir().toString());

  public static final JavaFileObject SHADOW_PROVIDER_SOURCE = forResource("mock-source/org/robolectric/internal/ShadowProvider.java");
  public static final JavaFileObject SHADOW_EXTRACTOR_SOURCE = forResource("mock-source/org/robolectric/shadow/api/Shadow.java");

  public static String toResourcePath(String clazzName) {
    return clazzName.replace('.', '/') + ".java";
  }
}
