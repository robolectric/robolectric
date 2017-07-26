package org.robolectric.shadows;

import android.os.Parcelable;
import android.view.View;
import android.view.accessibility.AccessibilityRecord;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/**
 * Shadow of {@link android.view.accessibility.AccessibilityRecord}.
 */
@Implements(AccessibilityRecord.class)
public class ShadowAccessibilityRecord {
  private Parcelable parcelableData;
  @RealObject public AccessibilityRecord realRecord;

  public static final int NO_VIRTUAL_ID = -1;

  private View sourceRoot;
  private int virtualDescendantId;

  @Implementation
  public void setSource(View root, int virtualDescendantId) {
    this.sourceRoot = root;
    this.virtualDescendantId = virtualDescendantId;
    Shadow.directlyOn(realRecord, AccessibilityRecord.class, "setSource",
        ClassParameter.from(View.class, root),
        ClassParameter.from(Integer.TYPE, virtualDescendantId));
  }

  @Implementation
  public void setSource(View root) {
    this.sourceRoot = root;
    this.virtualDescendantId = NO_VIRTUAL_ID;
    Shadow.directlyOn(realRecord, AccessibilityRecord.class, "setSource",
        ClassParameter.from(View.class, root));
  }

  @Implementation
  public void setParcelableData(Parcelable data) {
    parcelableData = data;
  }

  @Implementation
  public Parcelable getParcelableData() {
    return parcelableData;
  }

  public View getSourceRoot() {
    return sourceRoot;
  }

  public int getVirtualDescendantId() {
    return virtualDescendantId;
  }
}