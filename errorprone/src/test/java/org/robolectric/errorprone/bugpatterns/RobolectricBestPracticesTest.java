/*
 * Copyright 2016 The Error Prone Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
public class RobolectricBestPracticesTest {
  private BugCheckerRefactoringTestHelper testHelper;

  @Before
  public void setUp() {
    this.testHelper =
        BugCheckerRefactoringTestHelper.newInstance(new RobolectricBestPractices(), getClass());
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
            "    shadowLinearLayout.getLayoutAnimation().start();", // getLayoutAnimation() should
            // be called direct
            "    shadowLinearLayout.getGravity();", // getGravity() should be called on shadow
            "    XShadowLinearLayout shadowLinearLayout2 =",
            "        XShadows.shadowOf(new LinearLayout(RuntimeEnvironment.application));",
            "    shadowLinearLayout2.getGravity();", // getGravity() should be called on shadow
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
            "    shadowLinearLayout.getLayoutAnimation().start();", // getLayoutAnimation() should
            // be called direct
            "    shadowLinearLayout.getGravity();", // getGravity() should be called on shadow
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
            "    shadowLinearLayout.getGravity();", // getGravity() should be called on shadow
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
            "    shadowLinearLayout.getLayoutAnimation().start();", // getLayoutAnimation() should
            // be called direct
            "    this.shadowLinearLayout.getGravity();", // getGravity() should be called on shadow
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
            // todo: "    this.linearLayout = linearLayout;",
            "    this.shadowLinearLayout = shadowOf(linearLayout);",
            "    linearLayout.getLayoutAnimation().start();", // getLayoutAnimation() should be
            // called direct
            // todo: "    shadowOf(this.linearLayout).getGravity();", // getGravity() should be
            // called on shadow
            "    this.shadowLinearLayout.getGravity();", // getGravity() should be called on shadow
            "  }",
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

  // javatests/com/google/android/apps/play/music/app/playback2/MediaSessionManagerTest.java
  // https://sponge.corp.google.com/target?id=2df81aeb-476e-414f-9e5b-524f6561eb7d&target=//javatests/com/google/android/apps/play/music/app:playback2/MediaSessionManagerTest
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
            "    LinearLayout linearLayout = new LinearLayout(RuntimeEnvironment.application);",
            "    linearLayout.getLayoutAnimation().start();",
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
            "    linearLayoutShadow.getLayoutAnimation().start();", // getLayoutAnimation() should
            // be called direct
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
            "    shadowOf(linearLayout).getLayoutAnimation().start();", // getLayoutAnimation()
            // should be called direct
            "    shadowOf(linearLayout).getGravity();", // getGravity() should be called on shadow
            "    XShadows.shadowOf(linearLayout).getLayoutAnimation().start();", // getGravity()
            // should be called
            // on shadow
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
  @Ignore("todo")
  public void conflictingReplacements() throws IOException {
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
  @Ignore
  public void handleStaticMethodRefs() throws IOException {
    // shadowLooper::idle -> looper::idle
  }

  @Test
  @Ignore
  public void handleSubclasses() throws IOException {}

  //////////////////////

  // /**
  //  */
}
