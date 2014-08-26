package org.robolectric.annotation.processing;

import static org.truth0.Truth.ASSERT;

import com.google.testing.compile.CompileTester.UnsuccessfulCompilationClause;

public class Utils {
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
