package org.robolectric.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugPattern;
import java.io.IOException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link DeprecatedMethodsCheck} */
@RunWith(JUnit4.class)
@SuppressWarnings("LineLength")
public class DeprecatedMethodsCheckTest {
  private BugCheckerRefactoringTestHelper testHelper;

  @Before
  public void setUp() {
    this.testHelper =
        BugCheckerRefactoringTestHelper.newInstance(
            DeprecatedMethodsCheckForTest.class, getClass());
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
            "import static xxx.XShadows.shadowOf;",
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

  @Test
  public void replaceShadowApplicationGetLatestStuff() throws IOException {
    testHelper
        .addInputLines(
            "in/SomeTest.java",
            "import static xxx.XShadows.shadowOf;",
            "",
            "import org.junit.Test;",
            "import org.robolectric.RuntimeEnvironment;",
            "import xxx.XShadowApplication;",
            "import xxx.XShadowAlertDialog;",
            "import xxx.XShadowDialog;",
            "import xxx.XShadowPopupMenu;",
            "",
            "public class SomeTest {",
            "  @Test void theTest() {",
            "    XShadowAlertDialog ad ="
                + " shadowOf(RuntimeEnvironment.application).getLatestAlertDialog();",
            "    XShadowDialog d = shadowOf(RuntimeEnvironment.application).getLatestDialog();",
            "    XShadowPopupMenu pm ="
                + " shadowOf(RuntimeEnvironment.application).getLatestPopupMenu();",
            "  }",
            "}")
        .addOutputLines(
            "in/SomeTest.java",
            "import static xxx.XShadows.shadowOf;",
            "",
            "import org.junit.Test;",
            "import org.robolectric.RuntimeEnvironment;", // removable
            "import xxx.XShadowApplication;",
            "import xxx.XShadowAlertDialog;",
            "import xxx.XShadowDialog;",
            "import xxx.XShadowPopupMenu;",
            "",
            "public class SomeTest {",
            "  @Test void theTest() {",
            "    XShadowAlertDialog ad = shadowOf(XShadowAlertDialog.getLatestAlertDialog());",
            "    XShadowDialog d = shadowOf(XShadowDialog.getLatestDialog());",
            "    XShadowPopupMenu pm = shadowOf(XShadowPopupMenu.getLatestPopupMenu());",
            "  }",
            "}")
        .doTest();
  }

  @Test
  @Ignore("multiple-step refactorings not currently supported")
  public void inlineShadowVars() throws IOException {
    testHelper
        .addInputLines(
            "in/SomeTest.java",
            "import org.junit.Test;",
            "import xxx.XShadowApplication;",
            "",
            "public class SomeTest {",
            "  @Test void theTest() {",
            "    XShadowApplication shadowApplication = XShadowApplication.getInstance();",
            "    shadowApplication.runBackgroundTasks();",
            "  }",
            "}")
        .addOutputLines(
            "in/SomeTest.java",
            "import static xxx.XShadows.shadowOf;",
            "",
            "import android.app.Application;",
            "import org.junit.Test;",
            "import org.robolectric.RuntimeEnvironment;",
            "import org.robolectric.Shadows;",
            "import xxx.XShadowApplication;", // removable
            "",
            "public class SomeTest {",
            "  @Test void theTest() {",
            "    Application application = RuntimeEnvironment.application;",
            "    XShadows.shadowOf(application).runBackgroundTasks();",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void useShadowsNonStaticIfAlreadyImported() throws IOException {
    testHelper
        .addInputLines(
            "in/SomeTest.java",
            "import android.content.Context;",
            "import org.junit.Test;",
            "import xxx.XShadows;",
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
            "import android.content.Context;",
            "import org.junit.Test;",
            "import org.robolectric.RuntimeEnvironment;",
            "import xxx.XShadowApplication;", // removable
            "import xxx.XShadows;",
            "",
            "public class SomeTest {",
            "  Context application;",
            "  @Test void theTest() {",
            "    XShadows.shadowOf(RuntimeEnvironment.application).runBackgroundTasks();",
            "    application = RuntimeEnvironment.application;",
            "  }",
            "}")
        .doTest();
  }

  @Test
  @Ignore("multiple-step refactorings not currently supported")
  public void useFrameworkMethodWhenAppropriateAfterApplicationSubstitution() throws IOException {
    testHelper
        .addInputLines(
            "in/SomeTest.java",
            "import android.content.Context;",
            "import org.junit.Test;",
            "import org.robolectric.Shadows;",
            "import xxx.XShadowApplication;",
            "",
            "public class SomeTest {",
            "  XShadowApplication shadowApplication;",
            "  @Test void theTest() {",
            "    shadowApplication = XShadowApplication.getInstance();",
            "    shadowApplication.getMainLooper();",
            "    shadowApplication.runBackgroundTasks();",
            "  }",
            "}")
        .addOutputLines(
            "in/SomeTest.java",
            "import android.app.Application;",
            "import android.content.Context;",
            "import org.junit.Test;",
            "import org.robolectric.RuntimeEnvironment;",
            "import xxx.XShadows;",
            "import xxx.XShadowApplication;", // removable
            "",
            "public class SomeTest {",
            "  Application application;",
            "  @Test void theTest() {",
            "    application = RuntimeEnvironment.application;",
            "    application.getMainLooper();",
            "    XShadows.shadowOf(application).runBackgroundTasks();",
            "  }",
            "}")
        .doTest();
  }

  /** Test overrides for {@link DeprecatedMethodsCheck} */
  @BugPattern(name = "DeprecatedMethods", summary = "", severity = WARNING)
  public static class DeprecatedMethodsCheckForTest extends DeprecatedMethodsCheck {
    @Override
    String shadowName(String className) {
      return className.replaceAll("org\\.robolectric\\..*Shadow", "xxx.XShadow");
    }

    @Override
    String shortShadowName(String className) {
      return className.replaceAll("Shadow", "XShadow");
    }
  }
}
