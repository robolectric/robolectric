/*
 * Copyright (C) 2010 The Android Open Source Project
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

import static com.google.common.truth.Truth.assertWithMessage;

import android.os.Build;
import android.os.Message;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SdkSuppress;
import java.util.Iterator;
import java.util.List;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/**
 * CTS for {@link AccessibilityEvent}.
 *
 * <p>Copied from
 * cts/tests/accessibility/src/android/view/accessibility/cts/AccessibilityEventTest.java and
 * cts/tests/accessibility/src/android/view/accessibility/cts/AccessibilityRecordTest.java.
 *
 * <p>But this test class migrates assertions from junit to Google Truth.
 */
@RunWith(AndroidJUnit4.class)
public class AccessibilityEventTest {
  @Test
  @Config(minSdk = Build.VERSION_CODES.R)
  @SdkSuppress(minSdkVersion = Build.VERSION_CODES.R)
  public void testConstructors() {
    final AccessibilityEvent populatedEvent = new AccessibilityEvent();
    fullyPopulateAccessibilityEvent(populatedEvent);
    final AccessibilityEvent event = new AccessibilityEvent(populatedEvent);

    assertEqualsAccessibilityEvent(event, populatedEvent);

    final AccessibilityEvent firstEvent = new AccessibilityEvent();
    firstEvent.setEventType(AccessibilityEvent.TYPE_VIEW_FOCUSED);
    final AccessibilityEvent secondEvent =
        new AccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);

    assertEqualsAccessibilityEvent(firstEvent, secondEvent);
  }

  /**
   * Fully populates the {@link AccessibilityEvent} to marshal.
   *
   * @param sentEvent The event to populate.
   */
  private void fullyPopulateAccessibilityEvent(AccessibilityEvent sentEvent) {
    // Skip setDisplay because current implementation doesn't support
    // AccessibilityEvent#setDispalyId.
    sentEvent.setAddedCount(1);
    sentEvent.setBeforeText("BeforeText");
    sentEvent.setChecked(true);
    sentEvent.setClassName("foo.bar.baz.Class");
    sentEvent.setContentDescription("ContentDescription");
    sentEvent.setCurrentItemIndex(1);
    sentEvent.setEnabled(true);
    sentEvent.setEventType(AccessibilityEvent.TYPE_VIEW_FOCUSED);
    sentEvent.setEventTime(1000);
    sentEvent.setFromIndex(1);
    sentEvent.setFullScreen(true);
    sentEvent.setItemCount(1);
    sentEvent.setPackageName("foo.bar.baz");
    sentEvent.setParcelableData(Message.obtain(null, 1, 2, 3));
    sentEvent.setPassword(true);
    sentEvent.setRemovedCount(1);
    sentEvent.getText().add("Foo");
    sentEvent.setMaxScrollX(1);
    sentEvent.setMaxScrollY(1);
    sentEvent.setScrollX(1);
    sentEvent.setScrollY(1);
    sentEvent.setScrollDeltaX(3);
    sentEvent.setScrollDeltaY(3);
    sentEvent.setToIndex(1);
    sentEvent.setScrollable(true);
    sentEvent.setAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
    sentEvent.setMovementGranularity(AccessibilityNodeInfo.MOVEMENT_GRANULARITY_LINE);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      sentEvent.setSpeechStateChangeTypes(AccessibilityEvent.SPEECH_STATE_SPEAKING_START);
    }

    AccessibilityRecord record = AccessibilityRecord.obtain();
    fullyPopulateAccessibilityRecord(record);
    sentEvent.appendRecord(record);
  }

  /**
   * Compares all properties of the <code>expectedEvent</code> and the <code>receivedEvent</code> to
   * verify that the received event is the one that is expected.
   */
  private static void assertEqualsAccessibilityEvent(
      AccessibilityEvent expectedEvent, AccessibilityEvent receivedEvent) {
    assertWithMessage("addedCount has incorrect value")
        .that(receivedEvent.getAddedCount())
        .isEqualTo(expectedEvent.getAddedCount());
    assertWithMessage("beforeText has incorrect value")
        .that(receivedEvent.getBeforeText())
        .isEqualTo(expectedEvent.getBeforeText());
    assertWithMessage("checked has incorrect value")
        .that(receivedEvent.isChecked())
        .isEqualTo(expectedEvent.isChecked());
    assertWithMessage("className has incorrect value")
        .that(receivedEvent.getClassName())
        .isEqualTo(expectedEvent.getClassName());
    assertWithMessage("contentDescription has incorrect value")
        .that(receivedEvent.getContentDescription())
        .isEqualTo(expectedEvent.getContentDescription());
    assertWithMessage("currentItemIndex has incorrect value")
        .that(receivedEvent.getCurrentItemIndex())
        .isEqualTo(expectedEvent.getCurrentItemIndex());
    assertWithMessage("enabled has incorrect value")
        .that(receivedEvent.isEnabled())
        .isEqualTo(expectedEvent.isEnabled());
    assertWithMessage("eventType has incorrect value")
        .that(receivedEvent.getEventType())
        .isEqualTo(expectedEvent.getEventType());
    assertWithMessage("fromIndex has incorrect value")
        .that(receivedEvent.getFromIndex())
        .isEqualTo(expectedEvent.getFromIndex());
    assertWithMessage("fullScreen has incorrect value")
        .that(receivedEvent.isFullScreen())
        .isEqualTo(expectedEvent.isFullScreen());
    assertWithMessage("itemCount has incorrect value")
        .that(receivedEvent.getItemCount())
        .isEqualTo(expectedEvent.getItemCount());
    assertWithMessage("password has incorrect value")
        .that(receivedEvent.isPassword())
        .isEqualTo(expectedEvent.isPassword());
    assertWithMessage("removedCount has incorrect value")
        .that(receivedEvent.getRemovedCount())
        .isEqualTo(expectedEvent.getRemovedCount());
    assertWithMessage("maxScrollX has incorrect value")
        .that(receivedEvent.getMaxScrollX())
        .isEqualTo(expectedEvent.getMaxScrollX());
    assertWithMessage("maxScrollY has incorrect value")
        .that(receivedEvent.getMaxScrollY())
        .isEqualTo(expectedEvent.getMaxScrollY());
    assertWithMessage("scrollX has incorrect value")
        .that(receivedEvent.getScrollX())
        .isEqualTo(expectedEvent.getScrollX());
    assertWithMessage("scrollY has incorrect value")
        .that(receivedEvent.getScrollY())
        .isEqualTo(expectedEvent.getScrollY());
    assertWithMessage("scrollDeltaX has incorrect value")
        .that(receivedEvent.getScrollDeltaX())
        .isEqualTo(expectedEvent.getScrollDeltaX());
    assertWithMessage("scrollDeltaY has incorrect value")
        .that(receivedEvent.getScrollDeltaY())
        .isEqualTo(expectedEvent.getScrollDeltaY());
    assertWithMessage("toIndex has incorrect value")
        .that(receivedEvent.getToIndex())
        .isEqualTo(expectedEvent.getToIndex());
    assertWithMessage("scrollable has incorrect value")
        .that(receivedEvent.isScrollable())
        .isEqualTo(expectedEvent.isScrollable());
    assertWithMessage("granularity has incorrect value")
        .that(receivedEvent.getMovementGranularity())
        .isEqualTo(expectedEvent.getMovementGranularity());
    assertWithMessage("action has incorrect value")
        .that(receivedEvent.getAction())
        .isEqualTo(expectedEvent.getAction());
    assertWithMessage("windowChangeTypes has incorrect value")
        .that(receivedEvent.getWindowChanges())
        .isEqualTo(expectedEvent.getWindowChanges());
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      assertWithMessage("speechStateChangeTypes has incorrect value")
          .that(receivedEvent.getSpeechStateChangeTypes())
          .isEqualTo(expectedEvent.getSpeechStateChangeTypes());
    }

    assertEqualsText(expectedEvent.getText(), receivedEvent.getText());
    assertEqualAccessibilityRecord(expectedEvent, receivedEvent);

    assertEqualAppendedRecord(expectedEvent, receivedEvent);
  }

  private static void assertEqualAppendedRecord(
      AccessibilityEvent expectedEvent, AccessibilityEvent receivedEvent) {
    assertWithMessage("recordCount has incorrect value")
        .that(receivedEvent.getRecordCount())
        .isEqualTo(expectedEvent.getRecordCount());
    if (expectedEvent.getRecordCount() != 0 && receivedEvent.getRecordCount() != 0) {
      AccessibilityRecord expectedRecord = expectedEvent.getRecord(0);
      AccessibilityRecord receivedRecord = receivedEvent.getRecord(0);
      assertEqualAccessibilityRecord(expectedRecord, receivedRecord);
    }
  }

  static void assertEqualAccessibilityRecord(
      AccessibilityRecord expectedRecord, AccessibilityRecord receivedRecord) {
    assertWithMessage("addedCount has incorrect value")
        .that(receivedRecord.getAddedCount())
        .isEqualTo(expectedRecord.getAddedCount());
    assertWithMessage("beforeText has incorrect value")
        .that(receivedRecord.getBeforeText())
        .isEqualTo(expectedRecord.getBeforeText());
    assertWithMessage("checked has incorrect value")
        .that(receivedRecord.isChecked())
        .isEqualTo(expectedRecord.isChecked());
    assertWithMessage("className has incorrect value")
        .that(receivedRecord.getClassName())
        .isEqualTo(expectedRecord.getClassName());
    assertWithMessage("contentDescription has incorrect value")
        .that(receivedRecord.getContentDescription())
        .isEqualTo(expectedRecord.getContentDescription());
    assertWithMessage("currentItemIndex has incorrect value")
        .that(receivedRecord.getCurrentItemIndex())
        .isEqualTo(expectedRecord.getCurrentItemIndex());
    assertWithMessage("enabled has incorrect value")
        .that(receivedRecord.isEnabled())
        .isEqualTo(expectedRecord.isEnabled());
    assertWithMessage("fromIndex has incorrect value")
        .that(receivedRecord.getFromIndex())
        .isEqualTo(expectedRecord.getFromIndex());
    assertWithMessage("fullScreen has incorrect value")
        .that(receivedRecord.isFullScreen())
        .isEqualTo(expectedRecord.isFullScreen());
    assertWithMessage("itemCount has incorrect value")
        .that(receivedRecord.getItemCount())
        .isEqualTo(expectedRecord.getItemCount());
    assertWithMessage("password has incorrect value")
        .that(receivedRecord.isPassword())
        .isEqualTo(expectedRecord.isPassword());
    assertWithMessage("removedCount has incorrect value")
        .that(receivedRecord.getRemovedCount())
        .isEqualTo(expectedRecord.getRemovedCount());
    assertEqualsText(expectedRecord.getText(), receivedRecord.getText());
    assertWithMessage("maxScrollX has incorrect value")
        .that(receivedRecord.getMaxScrollX())
        .isEqualTo(expectedRecord.getMaxScrollX());
    assertWithMessage("maxScrollY has incorrect value")
        .that(receivedRecord.getMaxScrollY())
        .isEqualTo(expectedRecord.getMaxScrollY());
    assertWithMessage("scrollX has incorrect value")
        .that(receivedRecord.getScrollX())
        .isEqualTo(expectedRecord.getScrollX());
    assertWithMessage("scrollY has incorrect value")
        .that(receivedRecord.getScrollY())
        .isEqualTo(expectedRecord.getScrollY());
    assertWithMessage("toIndex has incorrect value")
        .that(receivedRecord.getToIndex())
        .isEqualTo(expectedRecord.getToIndex());
    assertWithMessage("scrollable has incorrect value")
        .that(receivedRecord.isScrollable())
        .isEqualTo(expectedRecord.isScrollable());
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      assertWithMessage("displayId has incorrect value")
          .that(receivedRecord.getDisplayId())
          .isEqualTo(expectedRecord.getDisplayId());
    }

    assertWithMessage("one of the parcelableData is null")
        .that(
            expectedRecord.getParcelableData() == null ^ receivedRecord.getParcelableData() == null)
        .isFalse();
    if (expectedRecord.getParcelableData() != null && receivedRecord.getParcelableData() != null) {
      assertWithMessage("parcelableData has incorrect value")
          .that(((Message) receivedRecord.getParcelableData()).what)
          .isEqualTo(((Message) expectedRecord.getParcelableData()).what);
    }
  }

  /**
   * Fully populates the {@link AccessibilityRecord}.
   *
   * @param record The record to populate.
   */
  static void fullyPopulateAccessibilityRecord(AccessibilityRecord record) {
    record.setAddedCount(1);
    record.setBeforeText("BeforeText");
    record.setChecked(true);
    record.setClassName("foo.bar.baz.Class");
    record.setContentDescription("ContentDescription");
    record.setCurrentItemIndex(1);
    record.setEnabled(true);
    record.setFromIndex(1);
    record.setFullScreen(true);
    record.setItemCount(1);
    record.setParcelableData(Message.obtain(null, 1, 2, 3));
    record.setPassword(true);
    record.setRemovedCount(1);
    record.getText().add("Foo");
    record.setMaxScrollX(1);
    record.setMaxScrollY(1);
    record.setScrollX(1);
    record.setScrollY(1);
    record.setToIndex(1);
    record.setScrollable(true);
  }

  /**
   * Compares the text of the <code>expectedEvent</code> and <code>receivedEvent</code> by comparing
   * the string representation of the corresponding {@link CharSequence}s.
   */
  static void assertEqualsText(List<CharSequence> expectedText, List<CharSequence> receivedText) {
    String message = "text has incorrect value";

    TestCase.assertEquals(message, expectedText.size(), receivedText.size());

    Iterator<CharSequence> expectedTextIterator = expectedText.iterator();
    Iterator<CharSequence> receivedTextIterator = receivedText.iterator();

    for (int i = 0; i < expectedText.size(); i++) {
      // compare the string representation
      assertWithMessage(message)
          .that(receivedTextIterator.next().toString())
          .isEqualTo(expectedTextIterator.next().toString());
    }
  }
}
