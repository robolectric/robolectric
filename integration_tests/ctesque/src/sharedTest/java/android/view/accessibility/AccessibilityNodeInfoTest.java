/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package android.view.accessibility;

import static com.google.common.truth.Truth.assertThat;

import android.os.Build;
import android.os.Parcel;
import android.view.View;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SdkSuppress;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import org.robolectric.testapp.ActivityWithAnotherTheme;
import org.robolectric.util.ReflectionHelpers;

/**
 * CTS for {@link AccessibilityNodeInfo}.
 *
 * <p>Copied from
 * cts/tests/accessibility/src/android/view/accessibility/cts/AccessibilityNodeInfoTest.java.
 *
 * <p>But this test class migrates assertions from junit to Google Truth.
 */
@RunWith(AndroidJUnit4.class)
public class AccessibilityNodeInfoTest {
  @Test
  @Config(minSdk = Build.VERSION_CODES.R)
  @SdkSuppress(minSdkVersion = Build.VERSION_CODES.R)
  public void testConstructor() {
    // Skip getSourceId as current implementation doesn't support it.
    final View view = new View(InstrumentationRegistry.getInstrumentation().getContext());
    AccessibilityNodeInfo firstInfo = new AccessibilityNodeInfo(view);
    AccessibilityNodeInfo secondInfo = new AccessibilityNodeInfo();
    secondInfo.setSource(view);

    assertThat(secondInfo.getWindowId()).isEqualTo(firstInfo.getWindowId());

    firstInfo = new AccessibilityNodeInfo(view, /* virtualDescendantId */ 1);
    secondInfo.setSource(view, /* virtualDescendantId */ 1);

    assertThat(secondInfo.getWindowId()).isEqualTo(firstInfo.getWindowId());
  }

  /** Pre-O, the window id is set to Integer.MAX_VALUE. Post-O, the window id is set to -1. */
  @Test
  public void obtain_noArgs_windowId() {
    assertThat(AccessibilityNodeInfo.obtain().getWindowId())
        .isEqualTo(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? -1 : Integer.MAX_VALUE);
  }

  @Test
  public void obtain_withWindow_returnsWindowId() {
    try (ActivityScenario<ActivityWithAnotherTheme> scenario =
        ActivityScenario.launch(ActivityWithAnotherTheme.class)) {
      scenario.onActivity(
          activity -> {
            View rootView = activity.findViewById(android.R.id.content);
            AccessibilityNodeInfo node = AccessibilityNodeInfo.obtain(rootView);
            long sourceNodeId = ReflectionHelpers.getField(node, "mSourceNodeId");
            assertThat(sourceNodeId).isNotEqualTo(-1);
          });
    }
  }

  @Test
  public void getText_afterCreateFromParcel() {
    AccessibilityNodeInfo node = AccessibilityNodeInfo.obtain();
    node.setText("hello world");
    node.setContentDescription("hello world");

    Parcel parcel = Parcel.obtain();
    node.writeToParcel(parcel, /* flags= */ 0);
    parcel.setDataPosition(0);
    final AccessibilityNodeInfo node2 = AccessibilityNodeInfo.CREATOR.createFromParcel(parcel);

    assertThat(node.getText().toString()).isEqualTo(node2.getText().toString());
    assertThat(node.getContentDescription().toString())
        .isEqualTo(node2.getContentDescription().toString());
  }

  @Test
  @SdkSuppress(minSdkVersion = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
  @Config(minSdk = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
  @GraphicsMode(GraphicsMode.Mode.NATIVE)
  public void directAccessibilityConnection_queryChildCount() {
    try (ActivityScenario<ActivityWithAnotherTheme> scenario =
        ActivityScenario.launch(ActivityWithAnotherTheme.class)) {
      scenario.onActivity(
          activity -> {
            View rootView = activity.findViewById(android.R.id.content);
            AccessibilityNodeInfo node = rootView.createAccessibilityNodeInfo();
            node.setQueryFromAppProcessEnabled(rootView, true);
            assertThat(node.getChildCount()).isEqualTo(1);
            assertThat(node.getChild(0)).isNotNull();
            assertThat(node.getWindowId()).isEqualTo(-1);
          });
    }
  }
}
