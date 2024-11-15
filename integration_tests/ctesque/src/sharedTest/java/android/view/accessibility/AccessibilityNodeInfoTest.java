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

  @Test
  @SdkSuppress(minSdkVersion = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
  @Config(minSdk = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
  @GraphicsMode(GraphicsMode.Mode.NATIVE)
  public void directAccessibilityConnection_queryChildCount() throws Exception {
    String originalAni = System.getProperty("robolectric.useRealAni", "");
    System.setProperty("robolectric.useRealAni", "true");
    try (ActivityScenario<ActivityWithAnotherTheme> scenario =
        ActivityScenario.launch(ActivityWithAnotherTheme.class)) {
      scenario.onActivity(
          activity -> {
            View rootView = activity.findViewById(android.R.id.content);
            AccessibilityNodeInfo node = rootView.createAccessibilityNodeInfo();
            node.setQueryFromAppProcessEnabled(rootView, true);
            assertThat(node.getChildCount()).isEqualTo(1);
            assertThat(node.getChild(0)).isNotNull();
          });
    } finally {
      System.setProperty("robolectric.useRealAni", originalAni);
    }
  }
}
