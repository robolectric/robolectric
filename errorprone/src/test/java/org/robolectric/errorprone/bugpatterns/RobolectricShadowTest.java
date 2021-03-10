package org.robolectric.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** @author christianw@google.com (Christian Williams) */
@RunWith(JUnit4.class)
public class RobolectricShadowTest {
  private BugCheckerRefactoringTestHelper testHelper;

  @Before
  public void setUp() {
    this.testHelper =
        BugCheckerRefactoringTestHelper.newInstance(RobolectricShadow.class, getClass());
  }

  @Test
  public void implMethodsShouldBeProtected() throws IOException {
    testHelper
        .addInputLines(
            "in/SomeShadow.java",
            "import org.robolectric.annotation.HiddenApi;",
            "import org.robolectric.annotation.Implementation;",
            "import org.robolectric.annotation.Implements;",
            "",
            "@Implements(Object.class)",
            "public class SomeShadow {",
            "  @Implementation public void publicMethod() {}",
            "  @Implementation @HiddenApi public void publicHiddenMethod() {}",
            "  @Implementation protected void protectedMethod() {}",
            "  @Implementation void packageMethod() {}",
            "  @Implementation private void privateMethod() {}",
            "}")
        .addOutputLines(
            "in/SomeShadow.java",
            "import org.robolectric.annotation.HiddenApi;",
            "import org.robolectric.annotation.Implementation;",
            "import org.robolectric.annotation.Implements;",
            "",
            "@Implements(Object.class)",
            "public class SomeShadow {",
            "  @Implementation protected void publicMethod() {}",
            "  @Implementation @HiddenApi public void publicHiddenMethod() {}",
            "  @Implementation protected void protectedMethod() {}",
            "  @Implementation protected void packageMethod() {}",
            "  @Implementation protected void privateMethod() {}",
            "}")
        .doTest();
  }

  @Test
  public void implMethodsNotProtectedForClassesNotInAndroidSdk() throws IOException {
    testHelper
        .addInputLines(
            "in/SomeShadow.java",
            "import org.robolectric.annotation.HiddenApi;",
            "import org.robolectric.annotation.Implementation;",
            "import org.robolectric.annotation.Implements;",
            "",
            "@Implements(value = Object.class, isInAndroidSdk = false)",
            "public class SomeShadow {",
            "  @Implementation public void publicMethod() {}",
            "  @Implementation @HiddenApi public void publicHiddenMethod() {}",
            "  @Implementation protected void protectedMethod() {}",
            "  @Implementation void packageMethod() {}",
            "  @Implementation private void privateMethod() {}",
            "}")
        .addOutputLines(
            "in/SomeShadow.java",
            "import org.robolectric.annotation.HiddenApi;",
            "import org.robolectric.annotation.Implementation;",
            "import org.robolectric.annotation.Implements;",
            "",
            "@Implements(value = Object.class, isInAndroidSdk = false)",
            "public class SomeShadow {",
            "  @Implementation public void publicMethod() {}",
            "  @Implementation @HiddenApi public void publicHiddenMethod() {}",
            "  @Implementation protected void protectedMethod() {}",
            "  @Implementation void packageMethod() {}",
            "  @Implementation private void privateMethod() {}",
            "}")
        .doTest();
  }

  @Test
  public void implMethodJavadocShouldBeMarkdown() throws Exception {
    testHelper
        .addInputLines(
            "in/SomeShadow.java",
            "import org.robolectric.annotation.Implementation;",
            "import org.robolectric.annotation.Implements;",
            "",
            "@Implements(Object.class)",
            "public class SomeShadow {",
            "  /**",
            "   * <p>Should be markdown!</p>",
            "   */",
            "  @Implementation public void aMethod() {}",
            "}")
        .addOutputLines(
            "in/SomeShadow.java",
            "import org.robolectric.annotation.Implementation;",
            "import org.robolectric.annotation.Implements;",
            "",
            "@Implements(Object.class)",
            "public class SomeShadow {",
            "  /**",
            "   * Should be markdown!",
            "   */",
            "  @Implementation protected void aMethod() {}",
            "}")
        .doTest();
  }
}
