package org.robolectric.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import java.io.IOException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** @author christianw@google.com (Christian Williams) */
@RunWith(JUnit4.class)
@SuppressWarnings("LineLength")
public class ShadowUsageCheckTest {
  private BugCheckerRefactoringTestHelper testHelper;

  @Before
  public void setUp() {
    this.testHelper =
        BugCheckerRefactoringTestHelper.newInstance(new ShadowUsageCheck(), getClass());
  }

  @Test
  public void staticMethodCalls() throws IOException {
    testHelper
        .addInputLines(
            "in/SomeTest.java",
            "import android.os.Looper;",
            "import org.junit.Test;",
            "import org.robolectric.shadows.ShadowLooper;",
            "",
            "public class SomeTest {",
            "  @Test void theTest() {",
            "    Looper.getMainLooper();",
            "    ShadowLooper.getMainLooper();",
            "  }",
            "}")
        .addOutputLines(
            "in/SomeTest.java",
            "import android.os.Looper;",
            "import org.junit.Test;",
            "import org.robolectric.shadows.ShadowLooper;", // removable
            "",
            "public class SomeTest {",
            "  @Test void theTest() {",
            "    Looper.getMainLooper();",
            "    Looper.getMainLooper();",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void localVarFromNewInstanceTest() throws IOException {
    testHelper
        .addInputLines(
            "in/SomeTest.java",
            "import static xxx.XShadows.shadowOf;",
            "",
            "import android.widget.LinearLayout;",
            "import org.junit.Test;",
            "import org.robolectric.RuntimeEnvironment;",
            "import xxx.XShadows;",
            "import xxx.XShadowLinearLayout;",
            "",
            "public class SomeTest {",
            "  @Test void theTest() {",
            "    XShadowLinearLayout shadowLinearLayout =",
            "        shadowOf(new LinearLayout(RuntimeEnvironment.application));",
            // getLayoutAnimation() should be called direct:
            "    shadowLinearLayout.getLayoutAnimation().start();",
            // getGravity() should be called on shadow:
            "    shadowLinearLayout.getGravity();",
            "    XShadowLinearLayout shadowLinearLayout2 =",
            "        XShadows.shadowOf(new LinearLayout(RuntimeEnvironment.application));",
            // getGravity() should be called on shadow
            "    shadowLinearLayout2.getGravity();",
            "  }",
            "}")
        .addOutputLines(
            "out/SomeTest.java",
            "import static xxx.XShadows.shadowOf;",
            "",
            "import android.widget.LinearLayout;",
            "import org.junit.Test;",
            "import org.robolectric.RuntimeEnvironment;",
            "import xxx.XShadows;",
            "import xxx.XShadowLinearLayout;",
            "",
            "public class SomeTest {",
            "  @Test void theTest() {",
            "    LinearLayout linearLayout = new LinearLayout(RuntimeEnvironment.application);",
            "    linearLayout.getLayoutAnimation().start();",
            "    shadowOf(linearLayout).getGravity();",
            "    LinearLayout linearLayout2 = new LinearLayout(RuntimeEnvironment.application);",
            "    XShadows.shadowOf(linearLayout2).getGravity();", // getGravity() on shadow
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void localVarWithPointlessCastFromNewInstanceTest() throws IOException {
    testHelper
        .addInputLines(
            "in/SomeTest.java",
            "import static xxx.XShadows.shadowOf;",
            "",
            "import android.widget.LinearLayout;",
            "import org.junit.Test;",
            "import org.robolectric.RuntimeEnvironment;",
            "import xxx.XShadows;",
            "import xxx.XShadowLinearLayout;",
            "",
            "public class SomeTest {",
            "  @Test void theTest() {",
            "    XShadowLinearLayout shadowLinearLayout =",
            "        (XShadowLinearLayout) shadowOf(",
            "            new LinearLayout(RuntimeEnvironment.application));",
            // getLayoutAnimation() should be called direct:
            "    shadowLinearLayout.getLayoutAnimation().start();",
            // getGravity() should be called on shadow:
            "    shadowLinearLayout.getGravity();",
            "  }",
            "}")
        .addOutputLines(
            "out/SomeTest.java",
            "import static xxx.XShadows.shadowOf;",
            "",
            "import android.widget.LinearLayout;",
            "import org.junit.Test;",
            "import org.robolectric.RuntimeEnvironment;",
            "import xxx.XShadows;",
            "import xxx.XShadowLinearLayout;",
            "",
            "public class SomeTest {",
            "  @Test void theTest() {",
            "    LinearLayout linearLayout = new LinearLayout(RuntimeEnvironment.application);",
            "    linearLayout.getLayoutAnimation().start();",
            "    shadowOf(linearLayout).getGravity();",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void localVarFromNewInstance_WithoutStaticImport() throws IOException {
    testHelper
        .addInputLines(
            "in/SomeTest.java",
            "import android.widget.LinearLayout;",
            "import org.junit.Test;",
            "import org.robolectric.RuntimeEnvironment;",
            "import xxx.XShadows;",
            "import xxx.XShadowLinearLayout;",
            "",
            "public class SomeTest {",
            "  @Test void theTest() {",
            "    XShadowLinearLayout shadowLinearLayout =",
            "        XShadows.shadowOf(new LinearLayout(RuntimeEnvironment.application));",
            // getLayoutAnimation() should be called direct:
            "    shadowLinearLayout.getLayoutAnimation().start();",
            // getGravity() should be called on shadow:
            "    shadowLinearLayout.getGravity();",
            "  }",
            "}")
        .addOutputLines(
            "out/SomeTest.java",
            "import android.widget.LinearLayout;",
            "import org.junit.Test;",
            "import org.robolectric.RuntimeEnvironment;",
            "import xxx.XShadows;",
            "import xxx.XShadowLinearLayout;",
            "",
            "public class SomeTest {",
            "  @Test void theTest() {",
            "    LinearLayout linearLayout = new LinearLayout(RuntimeEnvironment.application);",
            "    linearLayout.getLayoutAnimation().start();",
            "    XShadows.shadowOf(linearLayout).getGravity();",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void fieldFromNewInstanceTest() throws IOException {
    testHelper
        .addInputLines(
            "in/SomeTest.java",
            "import static xxx.XShadows.shadowOf;",
            "",
            "import android.widget.LinearLayout;",
            "import org.junit.Test;",
            "import org.robolectric.RuntimeEnvironment;",
            "import xxx.XShadows;",
            "import xxx.XShadowLinearLayout;",
            "",
            "public class SomeTest {",
            "  private XShadowLinearLayout shadowLinearLayout;",
            "  XShadowLinearLayout shadowLinearLayout2;",
            "  @Test void theTest() {",
            "    shadowLinearLayout = shadowOf(new LinearLayout(RuntimeEnvironment.application));",
            // getLayoutAnimation() should be called direct:
            "    shadowLinearLayout.getLayoutAnimation().start();",
            // getGravity() should be called on shadow:
            "    shadowLinearLayout.getGravity();",
            "    shadowLinearLayout2 =",
            "        XShadows.shadowOf(new LinearLayout(RuntimeEnvironment.application));",
            "  }",
            "}")
        .addOutputLines(
            "out/SomeTest.java",
            "import static xxx.XShadows.shadowOf;",
            "",
            "import android.widget.LinearLayout;",
            "import org.junit.Test;",
            "import org.robolectric.RuntimeEnvironment;",
            "import xxx.XShadows;",
            "import xxx.XShadowLinearLayout;",
            "",
            "public class SomeTest {",
            "  private LinearLayout linearLayout;",
            "  LinearLayout linearLayout2;",
            "  @Test void theTest() {",
            "    linearLayout = new LinearLayout(RuntimeEnvironment.application);",
            "    linearLayout.getLayoutAnimation().start();",
            "    shadowOf(linearLayout).getGravity();",
            "    linearLayout2 = new LinearLayout(RuntimeEnvironment.application);",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void switchFieldFromShadowToNormalWithSameName() throws IOException {
    testHelper
        .addInputLines(
            "in/SomeTest.java",
            "import static xxx.XShadows.shadowOf;",
            "",
            "import android.widget.LinearLayout;",
            "import org.junit.Test;",
            "import org.robolectric.RuntimeEnvironment;",
            "import xxx.XShadowLinearLayout;",
            "",
            "public class SomeTest {",
            "  XShadowLinearLayout linearLayout;",
            "  @Test void theTest() {",
            "    linearLayout = shadowOf(new LinearLayout(RuntimeEnvironment.application));",
            "    linearLayout.getLayoutAnimation().start();",
            "    linearLayout.getGravity();",
            "  }",
            "}")
        .addOutputLines(
            "out/SomeTest.java",
            "import static xxx.XShadows.shadowOf;",
            "",
            "import android.widget.LinearLayout;",
            "import org.junit.Test;",
            "import org.robolectric.RuntimeEnvironment;",
            "import xxx.XShadowLinearLayout;",
            "",
            "public class SomeTest {",
            "  LinearLayout linearLayout;",
            "  @Test void theTest() {",
            "    linearLayout = new LinearLayout(RuntimeEnvironment.application);",
            "    linearLayout.getLayoutAnimation().start();",
            "    shadowOf(linearLayout).getGravity();",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void fieldFromLocalVar() throws IOException {
    testHelper
        .addInputLines(
            "in/SomeTest.java",
            "import static xxx.XShadows.shadowOf;",
            "",
            "import android.widget.LinearLayout;",
            "import org.junit.Test;",
            "import org.robolectric.RuntimeEnvironment;",
            "import xxx.XShadowLinearLayout;",
            "",
            "public class SomeTest {",
            "  private XShadowLinearLayout shadowLinearLayout;",
            "",
            "  @Test void theTest() {",
            "    XShadowLinearLayout shadowLinearLayout =",
            "        shadowOf(new LinearLayout(RuntimeEnvironment.application));",
            "    this.shadowLinearLayout = shadowLinearLayout;",
            // getLayoutAnimation() should be called direct:
            "    shadowLinearLayout.getLayoutAnimation().start();",
            // getGravity() should be called on shadow:
            "    this.shadowLinearLayout.getGravity();",
            "  }",
            "}")
        .addOutputLines(
            "out/SomeTest.java",
            "import static xxx.XShadows.shadowOf;",
            "",
            "import android.widget.LinearLayout;",
            "import org.junit.Test;",
            "import org.robolectric.RuntimeEnvironment;",
            "import xxx.XShadowLinearLayout;",
            "",
            "public class SomeTest {",
            // todo: "  private LinearLayout linearLayout;",
            "  private XShadowLinearLayout shadowLinearLayout;",
            "",
            "  @Test void theTest() {",
            "    LinearLayout linearLayout = new LinearLayout(RuntimeEnvironment.application);",
            "    this.shadowLinearLayout = shadowOf(linearLayout);",
            // getLayoutAnimation() should be called direct:
            "    linearLayout.getLayoutAnimation().start();",
            // getGravity() should be called on shadow:
            "    this.shadowLinearLayout.getGravity();",
            "  }",

            // TODO(christianw): really this would be a lot better:
            // "  private LinearLayout linearLayout;",
            // "",
            // "  @Test void theTest() {",
            // "    LinearLayout linearLayout = new LinearLayout(RuntimeEnvironment.application);",
            // "    this.linearLayout = linearLayout;",
            // // getLayoutAnimation() should be called direct:
            // "    linearLayout.getLayoutAnimation().start();",
            // // getGravity() should be called on shadow:
            // "    shadowOf(this.linearLayout).getGravity();",
            // "  }",
            "}")
        .doTest();
  }

  @Test
  public void localShadowedVarFromLocalVarTest() throws IOException {
    testHelper
        .addInputLines(
            "in/SomeTest.java",
            "import static xxx.XShadows.shadowOf;",
            "",
            "import android.widget.LinearLayout;",
            "import org.junit.Test;",
            "import org.robolectric.RuntimeEnvironment;",
            "import xxx.XShadowLinearLayout;",
            "",
            "public class SomeTest {",
            "  @Test void theTest() {",
            "    LinearLayout linearLayoutX = new LinearLayout(RuntimeEnvironment.application);",
            "    XShadowLinearLayout shadowLinearLayout = shadowOf(linearLayoutX);",
            "    shadowLinearLayout.getLayoutAnimation().start();", // getLayoutAnimation() should
            // be called direct
            "    shadowLinearLayout.getGravity();", // getGravity() should be called on shadow
            "  }",
            "}")
        .addOutputLines(
            "out/SomeTest.java",
            "import static xxx.XShadows.shadowOf;",
            "",
            "import android.widget.LinearLayout;",
            "import org.junit.Test;",
            "import org.robolectric.RuntimeEnvironment;",
            "import xxx.XShadowLinearLayout;",
            "",
            "public class SomeTest {",
            "  @Test void theTest() {",
            "    LinearLayout linearLayoutX = new LinearLayout(RuntimeEnvironment.application);",
            "    linearLayoutX.getLayoutAnimation().start();",
            "    shadowOf(linearLayoutX).getGravity();",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void localShadowedVarFromLocalVarTest2() throws IOException {
    testHelper
        .addInputLines(
            "in/SomeTest.java",
            "import static xxx.XShadows.shadowOf;",
            "",
            "import android.widget.LinearLayout;",
            "import org.junit.Test;",
            "import org.robolectric.RuntimeEnvironment;",
            "import xxx.XShadowLinearLayout;",
            "",
            "public class SomeTest {",
            "  @Test void theTest() {",
            "    LinearLayout linearLayout = new LinearLayout(RuntimeEnvironment.application);",
            "    XShadowLinearLayout shadowLinearLayout = shadowOf(linearLayout);",
            // getLayoutAnimation() should be called direct:
            "    shadowLinearLayout.getLayoutAnimation().start();",
            "    shadowLinearLayout.getGravity();", // getGravity() should be called on shadow
            "  }",
            "}")
        .addOutputLines(
            "out/SomeTest.java",
            "import static xxx.XShadows.shadowOf;",
            "",
            "import android.widget.LinearLayout;",
            "import org.junit.Test;",
            "import org.robolectric.RuntimeEnvironment;",
            "import xxx.XShadowLinearLayout;",
            "",
            "public class SomeTest {",
            "  @Test void theTest() {",
            "    LinearLayout linearLayout = new LinearLayout(RuntimeEnvironment.application);",
            "    linearLayout.getLayoutAnimation().start();",
            "    shadowOf(linearLayout).getGravity();",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void localShadowedVarFromLocalVarAssignmentToSelf() throws IOException {
    testHelper
        .addInputLines(
            "in/SomeTest.java",
            "import static xxx.XShadows.shadowOf;",
            "",
            "import android.widget.LinearLayout;",
            "import org.junit.Test;",
            "import org.robolectric.RuntimeEnvironment;",
            "import xxx.XShadowLinearLayout;",
            "",
            "public class SomeTest {",
            "  @Test void theTest() {",
            "    LinearLayout linearLayout = new LinearLayout(RuntimeEnvironment.application);",
            "    XShadowLinearLayout shadowLinearLayout = shadowOf(linearLayout);",
            "    linearLayout = new LinearLayout(RuntimeEnvironment.application);",
            "    shadowLinearLayout = shadowOf(linearLayout);",
            "    shadowLinearLayout.getGravity();", // getGravity() should be called on shadow
            "  }",
            "}")
        .addOutputLines(
            "out/SomeTest.java",
            "import static xxx.XShadows.shadowOf;",
            "",
            "import android.widget.LinearLayout;",
            "import org.junit.Test;",
            "import org.robolectric.RuntimeEnvironment;",
            "import xxx.XShadowLinearLayout;",
            "",
            "public class SomeTest {",
            "  @Test void theTest() {",
            "    LinearLayout linearLayout = new LinearLayout(RuntimeEnvironment.application);",
            "    linearLayout = new LinearLayout(RuntimeEnvironment.application);",
            "    shadowOf(linearLayout).getGravity();",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void shadowedFieldFromFieldTest() throws IOException {
    testHelper
        .addInputLines(
            "in/SomeTest.java",
            "import static xxx.XShadows.shadowOf;",
            "",
            "import android.widget.LinearLayout;",
            "import org.junit.Test;",
            "import org.robolectric.RuntimeEnvironment;",
            "import xxx.XShadows;",
            "import xxx.XShadowLinearLayout;",
            "",
            "public class SomeTest {",
            "  private LinearLayout linearLayout;",
            "  private XShadowLinearLayout linearLayoutShadow;",
            "",
            "  @Test void theTest() {",
            "    linearLayout = new LinearLayout(RuntimeEnvironment.application);",
            "    linearLayoutShadow = shadowOf(linearLayout);",
            // getLayoutAnimation() should be called direct:
            "    linearLayoutShadow.getLayoutAnimation().start();",
            "    this.linearLayoutShadow.getLayoutAnimation().start();", // same with 'this.'
            "    linearLayoutShadow.getGravity();", // getGravity() should be called on shadow
            "    this.linearLayoutShadow.getGravity();", // same with 'this.'
            "  }",
            "}")
        .addOutputLines(
            "out/SomeTest.java",
            "import static xxx.XShadows.shadowOf;",
            "",
            "import android.widget.LinearLayout;",
            "import org.junit.Test;",
            "import org.robolectric.RuntimeEnvironment;",
            "import xxx.XShadows;",
            "import xxx.XShadowLinearLayout;",
            "",
            "public class SomeTest {",
            "  private LinearLayout linearLayout;",
            "",
            "  @Test void theTest() {",
            "    linearLayout = new LinearLayout(RuntimeEnvironment.application);",
            "    linearLayout.getLayoutAnimation().start();",
            "    this.linearLayout.getLayoutAnimation().start();",
            "    shadowOf(linearLayout).getGravity();",
            "    shadowOf(this.linearLayout).getGravity();",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void avoidFieldNameCollisions() throws IOException {
    testHelper
        .addInputLines(
            "in/SomeTest.java",
            "import static xxx.XShadows.shadowOf;",
            "",
            "import android.widget.LinearLayout;",
            "import org.junit.Test;",
            "import org.robolectric.RuntimeEnvironment;",
            "import xxx.XShadows;",
            "import xxx.XShadowLinearLayout;",
            "",
            "public class SomeTest {",
            "  private LinearLayout linearLayout;",
            "  private XShadowLinearLayout linearLayoutShadow;",
            "",
            "  @Test void theTest() {",
            "    linearLayout = new LinearLayout(RuntimeEnvironment.application);",
            // not the same LinearLayout, so don't reuse the field!
            "    linearLayoutShadow = shadowOf(new LinearLayout(RuntimeEnvironment.application));",
            // getLayoutAnimation() should be called direct:
            "    linearLayoutShadow.getLayoutAnimation().start();",
            "    this.linearLayoutShadow.getLayoutAnimation().start();", // same with 'this.'
            "    linearLayoutShadow.getGravity();", // getGravity() should be called on shadow
            "    this.linearLayoutShadow.getGravity();", // same with 'this.'
            "  }",
            "}")
        .addOutputLines(
            "out/SomeTest.java",
            "import static xxx.XShadows.shadowOf;",
            "",
            "import android.widget.LinearLayout;",
            "import org.junit.Test;",
            "import org.robolectric.RuntimeEnvironment;",
            "import xxx.XShadows;",
            "import xxx.XShadowLinearLayout;",
            "",
            "public class SomeTest {",
            "  private LinearLayout linearLayout;",
            "  private LinearLayout linearLayout2;",
            "",
            "  @Test void theTest() {",
            "    linearLayout = new LinearLayout(RuntimeEnvironment.application);",
            "    linearLayout2 = new LinearLayout(RuntimeEnvironment.application);",
            "    linearLayout2.getLayoutAnimation().start();",
            "    this.linearLayout2.getLayoutAnimation().start();",
            "    shadowOf(linearLayout2).getGravity();",
            "    shadowOf(this.linearLayout2).getGravity();",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void shadowedFieldFromLocalVarTest() throws IOException {
    testHelper
        .addInputLines(
            "in/SomeTest.java",
            "import static org.robolectric.Shadows.shadowOf;",
            "",
            "import android.os.Looper;",
            "import org.junit.Before;",
            "import org.junit.Test;",
            "import org.robolectric.RuntimeEnvironment;",
            "import org.robolectric.shadows.ShadowLooper;",
            "",
            "public class SomeTest {",
            "  private ShadowLooper shadowLooper;",
            "",
            "  @Before void setUp() {",
            "    Looper looper = RuntimeEnvironment.application.getMainLooper();",
            "    shadowLooper = shadowOf(looper);",
            "  }",
            "",
            "  @Test void theTest() {",
            "    shadowLooper.runToEndOfTasks();",
            "  }",
            "}")
        .addOutputLines(
            "out/SomeTest.java",
            "import static org.robolectric.Shadows.shadowOf;",
            "",
            "import android.os.Looper;",
            "import org.junit.Before;",
            "import org.junit.Test;",
            "import org.robolectric.RuntimeEnvironment;",
            "import org.robolectric.shadows.ShadowLooper;",
            "",
            "public class SomeTest {",
            "  private Looper looper;",
            "",
            "  @Before void setUp() {",
            "    Looper looper = RuntimeEnvironment.application.getMainLooper();",
            "    this.looper = looper;",
            "  }",
            "",
            "  @Test void theTest() {",
            "    shadowOf(looper).runToEndOfTasks();",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void removeShadowOfForSimpleCases() throws IOException {
    testHelper
        .addInputLines(
            "in/SomeTest.java",
            "import static xxx.XShadows.shadowOf;",
            "",
            "import android.widget.LinearLayout;",
            "import org.junit.Test;",
            "import org.robolectric.RuntimeEnvironment;",
            "import xxx.XShadows;",
            "import xxx.XShadowLinearLayout;",
            "",
            "public class SomeTest {",
            "  @Test void theTest() {",
            "    LinearLayout linearLayout = new LinearLayout(RuntimeEnvironment.application);",
            // getLayoutAnimation() should be called direct:
            "    shadowOf(linearLayout).getLayoutAnimation().start();",
            // getGravity() should be called on shadow:
            "    shadowOf(linearLayout).getGravity();",
            // getGravity() should be called on shadow
            "    XShadows.shadowOf(linearLayout).getLayoutAnimation().start();",
            "  }",
            "}")
        .addOutputLines(
            "out/SomeTest.java",
            "import static xxx.XShadows.shadowOf;",
            "",
            "import android.widget.LinearLayout;",
            "import org.junit.Test;",
            "import org.robolectric.RuntimeEnvironment;",
            "import xxx.XShadows;",
            "import xxx.XShadowLinearLayout;",
            "",
            "public class SomeTest {",
            "  @Test void theTest() {",
            "    LinearLayout linearLayout = new LinearLayout(RuntimeEnvironment.application);",
            "    linearLayout.getLayoutAnimation().start();",
            "    shadowOf(linearLayout).getGravity();",
            "    linearLayout.getLayoutAnimation().start();",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void handlingOfGenerics() throws IOException {
    testHelper
        .addInputLines(
            "in/SomeTestBase.java",
            "import android.app.Activity;",
            "import org.junit.Test;",
            "import org.robolectric.Robolectric;",
            "import org.robolectric.Shadows;",
            "import org.robolectric.shadows.ShadowActivity;",
            "import org.robolectric.android.controller.ActivityController;",
            "",
            "abstract public class SomeTestBase<T extends Activity> {",
            "  protected ActivityController<T> activityController;",
            "",
            "  @Test void theTest() {",
            "    activityController = Robolectric.buildActivity(getActivityClass());",
            "    ShadowActivity shadowActivity = Shadows.shadowOf(activityController.get());",
            "    shadowActivity.getResultCode();",
            "  }",
            "",
            "  abstract Class<T> getActivityClass();",
            "}")
        .addOutputLines(
            "in/SomeTestBase.java",
            "import android.app.Activity;",
            "import org.junit.Test;",
            "import org.robolectric.Robolectric;",
            "import org.robolectric.Shadows;",
            "import org.robolectric.shadows.ShadowActivity;",
            "import org.robolectric.android.controller.ActivityController;",
            "",
            "abstract public class SomeTestBase<T extends Activity> {",
            "  protected ActivityController<T> activityController;",
            "",
            "  @Test void theTest() {",
            "    activityController = Robolectric.buildActivity(getActivityClass());",
            "    Activity activity = activityController.get();",
            "    Shadows.shadowOf(activity).getResultCode();",
            "  }",
            "",
            "  abstract Class<T> getActivityClass();",
            "}")
        .doTest();
  }

  @Test
  public void hiddenApisNeedToGoThroughShadow() throws IOException {
    testHelper
        .addInputLines(
            "in/SomeHelper.java",
            "import static org.robolectric.Shadows.shadowOf;",
            "import android.app.Notification;",
            "import android.graphics.Bitmap;",
            "import org.junit.Test;",
            "import org.robolectric.Robolectric;",
            "",
            "public class SomeHelper {",
            "  protected Notification notification;",
            "",
            "  public Bitmap getLargeIconBitmapPostM() {",
            "    return shadowOf(notification.getLargeIcon()).getBitmap();",
            "  }",
            "}")
        .addOutputLines(
            "in/SomeHelper.java",
            "import static org.robolectric.Shadows.shadowOf;",
            "import android.app.Notification;",
            "import android.graphics.Bitmap;",
            "import org.junit.Test;",
            "import org.robolectric.Robolectric;",
            "",
            "public class SomeHelper {",
            "  protected Notification notification;",
            "",
            "  public Bitmap getLargeIconBitmapPostM() {",
            "    return shadowOf(notification.getLargeIcon()).getBitmap();",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void shouldSaveFieldsFromMethodArgs() throws Exception {
    testHelper
        .addInputLines(
            "in/SomeTestHelper.java",
            "import static org.robolectric.Shadows.shadowOf;",
            "",
            "import android.os.Looper;",
            "import org.robolectric.shadows.ShadowLooper;",
            "",
            "public class SomeTestHelper {",
            "  private final ShadowLooper theShadowLooper;",
            "",
            "  SomeTestHelper(Looper looper) {",
            "    this.theShadowLooper = shadowOf(looper);",
            "  }",
            "",
            "  boolean moreTasks() {",
            "    return theShadowLooper.getScheduler().areAnyRunnable();",
            "  }",
            "}")
        .addOutputLines(
            "in/SomeTestHelper.java",
            "import static org.robolectric.Shadows.shadowOf;",
            "",
            "import android.os.Looper;",
            "import org.robolectric.shadows.ShadowLooper;",
            "",
            "public class SomeTestHelper {",
            "  private final Looper looper;",
            "",
            "  SomeTestHelper(Looper looper) {",
            "    this.looper = looper;",
            "  }",
            "",
            "  boolean moreTasks() {",
            "    return shadowOf(looper).getScheduler().areAnyRunnable();",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void overlappingReplacements() throws IOException {
    testHelper
        .addInputLines(
            "in/SomeTest.java",
            "import static org.robolectric.Shadows.shadowOf;",
            "",
            "import android.content.Context;",
            "import android.net.ConnectivityManager;",
            "import android.net.NetworkInfo;",
            "import org.junit.Test;",
            "import org.robolectric.RuntimeEnvironment;",
            "import org.robolectric.shadows.ShadowConnectivityManager;",
            "import org.robolectric.shadows.ShadowNetworkInfo;",
            "",
            "public class SomeTest {",
            "  @Test void theTest() {",
            "    ShadowConnectivityManager shadowConnectivityManager =",
            "        shadowOf((ConnectivityManager) RuntimeEnvironment.application.getSystemService(Context.CONNECTIVITY_SERVICE));",
            "    ShadowNetworkInfo shadowActiveNetworkInfo =",
            "        shadowOf(shadowConnectivityManager.getActiveNetworkInfo());",
            "    shadowActiveNetworkInfo.setConnectionType(ConnectivityManager.TYPE_MOBILE);",
            "  }",
            "}")
        .addOutputLines(
            "out/SomeTest.java",
            "import static org.robolectric.Shadows.shadowOf;",
            "",
            "import android.content.Context;",
            "import android.net.ConnectivityManager;",
            "import android.net.NetworkInfo;",
            "import org.junit.Test;",
            "import org.robolectric.RuntimeEnvironment;",
            "import org.robolectric.shadows.ShadowConnectivityManager;",
            "import org.robolectric.shadows.ShadowNetworkInfo;",
            "",
            "public class SomeTest {",
            "  @Test void theTest() {",
            "    ConnectivityManager connectivityManager =",
            "        (ConnectivityManager) RuntimeEnvironment.application.getSystemService(Context.CONNECTIVITY_SERVICE);",
            "    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();",
            "    shadowOf(activeNetworkInfo).setConnectionType(ConnectivityManager.TYPE_MOBILE);",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void shouldSupportVarReassignment() throws Exception {
    testHelper
        .addInputLines(
            "in/SomeTest.java",
            "import static org.junit.Assert.assertEquals;",
            "",
            "import android.view.ViewGroup;",
            "import android.widget.ImageView;",
            "import org.junit.Test;",
            "import org.robolectric.Shadows;",
            "import org.robolectric.shadows.ShadowApplication;",
            "import org.robolectric.shadows.ShadowDrawable;",
            "",
            "public class SomeTest {",
            "  private ViewGroup container;",
            "",
            "  @Test",
            "  public void testFillingDotsMaskTypeDotsCorrectRefilledAfterConfigChange() {",
            "    ImageView dotView1 = (ImageView) container.getChildAt(1);",
            "    ShadowDrawable dot1 = Shadows.shadowOf(dotView1.getDrawable());",
            "    ImageView dotView2 = (ImageView) container.getChildAt(2);",
            "    ShadowDrawable dot2 = Shadows.shadowOf(dotView2.getDrawable());",
            "",
            "    assertEquals(android.R.drawable.bottom_bar, dot1.getCreatedFromResId());",
            "    assertEquals(android.R.drawable.btn_plus, dot2.getCreatedFromResId());",
            "",
            "    ShadowApplication.runBackgroundTasks();",
            "",
            "    dotView1 = (ImageView) container.getChildAt(1);",
            "    dot1 = Shadows.shadowOf(dotView1.getDrawable());",
            "    dotView2 = (ImageView) container.getChildAt(2);",
            "    dot2 = Shadows.shadowOf(dotView2.getDrawable());",
            "    assertEquals(android.R.drawable.bottom_bar, dot1.getCreatedFromResId());",
            "    assertEquals(android.R.drawable.btn_minus, dot2.getCreatedFromResId());",
            "  }",
            "}")
        .addOutputLines(
            "out/SomeTest.java",
            "import static org.junit.Assert.assertEquals;",
            "",
            "import android.graphics.drawable.Drawable;",
            "import android.view.ViewGroup;",
            "import android.widget.ImageView;",
            "import org.junit.Test;",
            "import org.robolectric.Shadows;",
            "import org.robolectric.shadows.ShadowApplication;",
            "import org.robolectric.shadows.ShadowDrawable;",
            "",
            "public class SomeTest {",
            "  private ViewGroup container;",
            "",
            "  @Test",
            "  public void testFillingDotsMaskTypeDotsCorrectRefilledAfterConfigChange() {",
            "    ImageView dotView1 = (ImageView) container.getChildAt(1);",
            "    Drawable dot1 = dotView1.getDrawable();",
            "    ImageView dotView2 = (ImageView) container.getChildAt(2);",
            "    Drawable dot2 = dotView2.getDrawable();",
            "",
            "    assertEquals(android.R.drawable.bottom_bar,",
            "        Shadows.shadowOf(dot1).getCreatedFromResId());",
            "    assertEquals(android.R.drawable.btn_plus,",
            "        Shadows.shadowOf(dot2).getCreatedFromResId());",
            "",
            "    ShadowApplication.runBackgroundTasks();",
            "",
            "    dotView1 = (ImageView) container.getChildAt(1);",
            "    dot1 = dotView1.getDrawable();",
            "    dotView2 = (ImageView) container.getChildAt(2);",
            "    dot2 = dotView2.getDrawable();",
            "    assertEquals(android.R.drawable.bottom_bar,",
            "        Shadows.shadowOf(dot1).getCreatedFromResId());",
            "    assertEquals(android.R.drawable.btn_minus,",
            "        Shadows.shadowOf(dot2).getCreatedFromResId());",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void ignoreShadowClasses() throws IOException {
    testHelper
        .addInputLines(
            "in/SomeShadow.java",
            "import android.os.Looper;",
            "import org.robolectric.annotation.Implementation;",
            "import org.robolectric.annotation.Implements;",
            "import org.robolectric.shadows.ShadowLooper;",
            "",
            "@Implements(Looper.class)",
            "public class SomeShadow extends ShadowLooper {",
            "  @Implementation",
            "  public static Looper getMainLooper() {",
            "    return ShadowLooper.getMainLooper();",
            "  }",
            "}")
        .addOutputLines(
            "in/SomeShadow.java",
            "import android.os.Looper;",
            "import org.robolectric.annotation.Implementation;",
            "import org.robolectric.annotation.Implements;",
            "import org.robolectric.shadows.ShadowLooper;",
            "",
            "@Implements(Looper.class)",
            "public class SomeShadow extends ShadowLooper {",
            "  @Implementation",
            "  public static Looper getMainLooper() {",
            "    return ShadowLooper.getMainLooper();",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void reassignmentToFormerlyShadowVars() throws Exception {
    testHelper
        .addInputLines(
            "in/SomeTest.java",
            "import static org.junit.Assert.assertEquals;",
            "import static xxx.XShadows.shadowOf;",
            "",
            "import android.graphics.drawable.Drawable;",
            "import android.view.ViewGroup;",
            "import org.junit.Test;",
            "import org.robolectric.RuntimeEnvironment;",
            "import xxx.XShadows;",
            "import xxx.XShadowDrawable;",
            "",
            "public class SomeTest {",
            "  private ViewGroup circlesButton;",
            "  @Test public void testWhiteBackgroundForModes() {",
            "    circlesButton.setClipChildren(true);",
            "    XShadowDrawable shadowBackground =",
            "        XShadows.shadowOf(circlesButton.getBackground());",
            "    assertEquals(1234, shadowBackground.getCreatedFromResId());",
            "",
            "    circlesButton.setClipChildren(false);",
            "    shadowBackground = XShadows.shadowOf(circlesButton.getBackground());",
            "    assertEquals(4321, shadowBackground.getCreatedFromResId());",
            "",
            "    circlesButton.setClipChildren(true);",
            "    shadowBackground = XShadows.shadowOf(circlesButton.getBackground());",
            "    assertEquals(1234, shadowBackground.getCreatedFromResId());",
            "  }",
            "}")
        .addOutputLines(
            "out/SomeTest.java",
            "import static org.junit.Assert.assertEquals;",
            "import static xxx.XShadows.shadowOf;",
            "",
            "import android.graphics.drawable.Drawable;",
            "import android.view.ViewGroup;",
            "import org.junit.Test;",
            "import org.robolectric.RuntimeEnvironment;",
            "import xxx.XShadows;",
            "import xxx.XShadowDrawable;",
            "",
            "public class SomeTest {",
            "  private ViewGroup circlesButton;",
            "  @Test public void testWhiteBackgroundForModes() {",
            "    circlesButton.setClipChildren(true);",
            "    Drawable background = circlesButton.getBackground();",
            "    assertEquals(1234, XShadows.shadowOf(background).getCreatedFromResId());",
            "",
            "    circlesButton.setClipChildren(false);",
            "    background = circlesButton.getBackground();",
            "    assertEquals(4321, XShadows.shadowOf(background).getCreatedFromResId());",
            "",
            "    circlesButton.setClipChildren(true);",
            "    background = circlesButton.getBackground();",
            "    assertEquals(1234, XShadows.shadowOf(background).getCreatedFromResId());",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void assignmentOfNonVarShadowExpressions_shouldNotNPE() throws Exception {
    testHelper
        .addInputLines(
            "in/SomeTest.java",
            "import static org.junit.Assert.assertEquals;",
            "",
            "import android.app.Notification;",
            "import android.app.NotificationManager;",
            "import android.content.Context;",
            "import org.junit.Test;",
            "import org.robolectric.RuntimeEnvironment;",
            "import org.robolectric.Shadows;",
            "import org.robolectric.shadows.ShadowNotification;",
            "import org.robolectric.shadows.ShadowNotificationManager;",
            "",
            "public class SomeTest {",
            "  private ShadowNotificationManager shadowNotificationManager;",
            "",
            "  @Test public void test() {",
            "    shadowNotificationManager =",
            "        Shadows.shadowOf(",
            "          (NotificationManager) RuntimeEnvironment.application",
            "            .getSystemService(Context.NOTIFICATION_SERVICE));",
            "    ShadowNotification notification =",
            "        Shadows.shadowOf(shadowNotificationManager.getNotification(0));",
            "    assertEquals(\"title\", notification.getContentTitle());",
            "  }",
            "}")
        .addOutputLines(
            "out/SomeTest.java",
            "import static org.junit.Assert.assertEquals;",
            "",
            "import android.app.Notification;",
            "import android.app.NotificationManager;",
            "import android.content.Context;",
            "import org.junit.Test;",
            "import org.robolectric.RuntimeEnvironment;",
            "import org.robolectric.Shadows;",
            "import org.robolectric.shadows.ShadowNotification;",
            "import org.robolectric.shadows.ShadowNotificationManager;",
            "",
            "public class SomeTest {",
            "  private NotificationManager notificationManager;",
            "",
            "  @Test public void test() {",
            "    notificationManager =",
            "        (NotificationManager) RuntimeEnvironment.application",
            "          .getSystemService(Context.NOTIFICATION_SERVICE);",
            "    Notification notification =",
            "        Shadows.shadowOf(notificationManager).getNotification(0);",
            "    assertEquals(\"title\", Shadows.shadowOf(notification).getContentTitle());",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void shadowOfForFieldAsMethodSelector() throws Exception {
    testHelper
        .addInputLines(
            "in/SomeTest.java",
            "import static org.junit.Assert.assertNotNull;",
            "",
            "import android.app.Activity;",
            "import org.junit.Test;",
            "import org.robolectric.RuntimeEnvironment;",
            "import org.robolectric.Shadows;",
            "import org.robolectric.shadows.ShadowActivity;",
            "import org.robolectric.shadows.ShadowIntent;",
            "",
            "public class SomeTest {",
            "  private Activity theActivity;",
            "",
            "  @Test public void test() {",
            "    ShadowActivity shadowActivity = Shadows.shadowOf(theActivity);",
            "    ShadowIntent shadowIntent =",
            "        Shadows.shadowOf(shadowActivity.getNextStartedActivity());",
            "    assertNotNull(shadowIntent);",
            // todo: "    shadowIntent = null;",
            "  }",
            "}")
        .addOutputLines(
            "out/SomeTest.java",
            "import static org.junit.Assert.assertNotNull;",
            "",
            "import android.app.Activity;",
            "import android.content.Intent;",
            "import org.junit.Test;",
            "import org.robolectric.RuntimeEnvironment;",
            "import org.robolectric.Shadows;",
            "import org.robolectric.shadows.ShadowActivity;",
            "import org.robolectric.shadows.ShadowIntent;",
            "",
            "public class SomeTest {",
            "  private Activity theActivity;",
            "",
            "  @Test public void test() {",
            "    Intent intent = Shadows.shadowOf(theActivity).getNextStartedActivity();",
            "    assertNotNull(Shadows.shadowOf(intent));",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void noChangesToShadowClasses() throws Exception {
    testHelper
        .addInputLines(
            "in/SomeTest.java",
            "import org.junit.Test;",
            "import android.graphics.Bitmap;",
            "import org.robolectric.annotation.Implementation;",
            "import org.robolectric.annotation.Implements;",
            "import org.robolectric.shadows.ShadowBitmap;",
            "",
            "public class SomeTest {",
            "  @Implements(Bitmap.class)",
            "  public static class MyShadowBitmap extends ShadowBitmap {",
            "    @Implementation public static Bitmap createBitmap(Bitmap src) {",
            "      return ShadowBitmap.createBitmap(src);",
            "    }",
            "  }",
            "}")
        .expectUnchanged()
        .doTest();
  }

  @Test
  public void fixShadowsPassedAsMethodParams() throws IOException {
    testHelper
        .addInputLines(
            "in/SomeTest.java",
            "import org.junit.Test;",
            "import android.content.Intent;",
            "import org.robolectric.Shadows;",
            "import org.robolectric.shadows.ShadowIntent;",
            "",
            "public class SomeTest {",
            "  private Intent theIntent;",
            "  private ShadowIntent theShadowIntent;",
            "",
            "  @Test public void test() {",
            "    theShadowIntent = Shadows.shadowOf(theIntent);",
            "    method1(theShadowIntent);",
            "  }",
            "",
            "  private void method1(ShadowIntent shadowIntent) {",
            "    method2(shadowIntent);",
            "  }",
            "",
            "  private void method2(ShadowIntent shadowIntent) {",
            "    shadowIntent.getIntentClass();",
            "  }",
            "}")
        .addOutputLines(
            "out/SomeTest.java",
            "import org.junit.Test;",
            "import android.content.Intent;",
            "import org.robolectric.Shadows;",
            "import org.robolectric.shadows.ShadowIntent;",
            "",
            "public class SomeTest {",
            "  private Intent theIntent;",
            "",
            "  @Test public void test() {",
            "    method1(Shadows.shadowOf(theIntent));",
            "  }",
            "",
            "  private void method1(ShadowIntent shadowIntent) {",
            "    method2(shadowIntent);",
            "  }",
            "",
            "  private void method2(ShadowIntent shadowIntent) {",
            "    shadowIntent.getIntentClass();",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void useTestApisWhenNoPublicApiAvailableAtMinimumSdkLevel() throws IOException {
    testHelper
        .addInputLines(
            "in/SomeTest.java",
            "import org.junit.Test;",
            "import static org.junit.Assert.assertNotNull;",
            "import static org.robolectric.Shadows.shadowOf;",
            "",
            "import android.app.job.JobScheduler;",
            "import android.content.Context;",
            "import org.robolectric.Shadows;",
            "import org.robolectric.shadows.ShadowJobScheduler;",
            "",
            "public class SomeTest {",
            "  private Context mContext;",
            "  private ShadowJobScheduler mJobScheduler;",
            "",
            "  @Test public void test() {",
            "    mJobScheduler =",
            "        shadowOf((JobScheduler)",
            "          mContext.getSystemService(Context.JOB_SCHEDULER_SERVICE));",
            "    assertNotNull(mJobScheduler.getPendingJob(1234));",
            "  }",
            "}")
        .addOutputLines(
            "out/SomeTest.java",
            "import org.junit.Test;",
            "import static org.junit.Assert.assertNotNull;",
            "import static org.robolectric.Shadows.shadowOf;",
            "",
            "import android.app.job.JobScheduler;",
            "import android.content.Context;",
            "import org.robolectric.Shadows;",
            "import org.robolectric.shadows.ShadowJobScheduler;",
            "",
            "public class SomeTest {",
            "  private Context mContext;",
            "  private JobScheduler mJobScheduler;",
            "",
            "  @Test public void test() {",
            "    mJobScheduler =",
            "        (JobScheduler) mContext.getSystemService(Context.JOB_SCHEDULER_SERVICE);",
            // JobScheduler.getPendingJob() was added in Android N, don't assume we can call it...
            "    assertNotNull(shadowOf(mJobScheduler).getPendingJob(1234));",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void collidingNames() throws IOException {
    testHelper
        .addInputLines(
            "in/NotificationManager.java",
            "import org.junit.Before;",
            "import static org.junit.Assert.assertNotNull;",
            "import static org.robolectric.Shadows.shadowOf;",
            "import android.content.Context;",
            "import org.robolectric.shadows.ShadowNotificationManager;",
            "public class NotificationManager {",
            "  private Context context;",
            "  private ShadowNotificationManager notificationManager;",
            "  @Before public void setUp() {",
            "    notificationManager = shadowOf((android.app.NotificationManager)",
            "        context.getSystemService(Context.NOTIFICATION_SERVICE));",
            "  }",
            "}")
        .addOutputLines(
            "out/NotificationManager.java",
            "import org.junit.Before;",
            "import static org.junit.Assert.assertNotNull;",
            "import static org.robolectric.Shadows.shadowOf;",
            "import android.content.Context;",
            "import org.robolectric.shadows.ShadowNotificationManager;",
            "public class NotificationManager {",
            "  private Context context;",
            "  private android.app.NotificationManager notificationManager;",
            "  @Before public void setUp() {",
            "    notificationManager = (android.app.NotificationManager)",
            "        context.getSystemService(Context.NOTIFICATION_SERVICE);",
            "  }",
            "}")
        .doTest();
  }

  @Test
  @Ignore
  public void handleStaticMethodRefs() throws IOException {
    // shadowLooper::idle -> looper::idle
  }

  @Test
  @Ignore
  public void handleSubclasses() throws IOException {}

}
