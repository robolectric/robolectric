package org.robolectric.shadows;

import static org.robolectric.shadow.api.Shadow.directlyOn;

import android.widget.BaseAdapter;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(BaseAdapter.class)
public class ShadowBaseAdapter {
  @RealObject private BaseAdapter realBaseAdapter;
  private boolean wasNotifyDataSetChangedCalled;

  @Implementation
  protected void notifyDataSetChanged() {
    wasNotifyDataSetChangedCalled = true;
    directlyOn(realBaseAdapter, BaseAdapter.class, "notifyDataSetChanged");
  }

  public void clearWasDataSetChangedCalledFlag() {
    wasNotifyDataSetChangedCalled = false;
  }

  public boolean wasNotifyDataSetChangedCalled() {
    return wasNotifyDataSetChangedCalled;
  }
}
