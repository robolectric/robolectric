package org.robolectric.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** @author christianw@google.com (Christian Williams) */
@RunWith(JUnit4.class)
@SuppressWarnings("LineLength")
public class DeprecatedMethodsCheckTest {
  private BugCheckerRefactoringTestHelper testHelper;

  @Before
  public void setUp() {
    this.testHelper =
        BugCheckerRefactoringTestHelper.newInstance(new DeprecatedMethodsCheck(), getClass());
  }

  @Test
  public void replaceShadowApplicationGetInstance() throws IOException {
    testHelper
        .addInputLines(
            "in/SomeTest.java",
            "import android.content.Context;",
            "import org.junit.Test;",
            "import xxx.XShadowApplication;",
            "",
            "public class SomeTest {",
            "  Context application;",
            "  @Test void theTest() {",
            "    XShadowApplication.getInstance().runBackgroundTasks();",
            "    application = XShadowApplication.getInstance().getApplicationContext();",
            "  }",
            "}")
        .addOutputLines(
            "in/SomeTest.java",
            "import static org.robolectric.Shadows.shadowOf;",
            "",
            "import android.content.Context;",
            "import org.junit.Test;",
            "import org.robolectric.RuntimeEnvironment;",
            "import xxx.XShadowApplication;", // removable
            "",
            "public class SomeTest {",
            "  Context application;",
            "  @Test void theTest() {",
            "    shadowOf(RuntimeEnvironment.application).runBackgroundTasks();",
            "    application = RuntimeEnvironment.application;",
            "  }",
            "}")
        .doTest();
  }
}
