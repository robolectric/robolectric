package org.robolectric.shadows;

import android.os.Parcelable;
import android.view.accessibility.AccessibilityRecord;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Shadow of {@link android.view.accessibility.AccessibilityRecord}.
 */
@Implements(AccessibilityRecord.class)
public class ShadowAccessibilityRecord {
  private Parcelable parcelableData;

  @Implementation
  public void setParcelableData(Parcelable data) {
    parcelableData = data;
  }

  @Implementation
  public Parcelable getParcelableData() {
    return parcelableData;
  }
}