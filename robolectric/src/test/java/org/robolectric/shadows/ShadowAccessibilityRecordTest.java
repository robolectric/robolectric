package org.robolectric.shadows;

import static android.view.accessibility.AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Unit tests for ShadowAccessibilityRecord. */
@RunWith(AndroidJUnit4.class)
public class ShadowAccessibilityRecordTest {

  @Test
  public void init_shouldCopyBothRealAndShadowFields() {
    AccessibilityNodeInfo source = AccessibilityNodeInfo.obtain();
    source.setClassName("fakeClassName");

    AccessibilityEvent event = AccessibilityEvent.obtain(TYPE_WINDOW_CONTENT_CHANGED);
    shadowOf(event).setSourceNode(source);
    final int fromIndex = 5;
    event.setFromIndex(fromIndex);

    AccessibilityEvent eventCopy = AccessibilityEvent.obtain(event);
    assertThat(eventCopy.getSource()).isEqualTo(source);
    assertThat(eventCopy.getFromIndex()).isEqualTo(fromIndex);
  }
}
