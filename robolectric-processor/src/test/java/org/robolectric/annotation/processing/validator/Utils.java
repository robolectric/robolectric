package org.robolectric.annotation.processing.validator;

import static com.google.testing.compile.JavaFileObjects.forResource;
import static org.truth0.Truth.ASSERT;

import javax.tools.JavaFileObject;

import com.google.testing.compile.CompileTester.UnsuccessfulCompilationClause;

public class Utils {
  // So that we can test compilation without Robolectric.java included, this has not been put in its
  // "proper" directory. Otherwise the compile-testing suite is "smart" enough to find it even though
  // it hasn't been specified on the input file list.
  public static final JavaFileObject ROBO_SOURCE = forResource("mock-source/Robolectric.java");
  public static final JavaFileObject SHADOW_PROVIDER_SOURCE = forResource("mock-source/org/robolectric/internal/ShadowProvider.java");
  public static final JavaFileObject SHADOW_EXTRACTOR_SOURCE = forResource("mock-source/org/robolectric/internal/ShadowExtractor.java");

  public static String toResourcePath(String clazzName) {
    return clazzName.replace('.', '/') + ".java";
  }
  
  public static void assertWithoutErrorContaining(UnsuccessfulCompilationClause c, String err) {
    try {
      c.withErrorContaining(err);
    } catch (AssertionError e) {
      return;
    }
    ASSERT.fail("Shouldn't have found any errors containing " + err + ", but we did");
  }
}
