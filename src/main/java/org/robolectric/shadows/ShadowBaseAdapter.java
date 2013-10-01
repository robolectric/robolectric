package org.robolectric.shadows;

import android.widget.BaseAdapter;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import static org.robolectric.Robolectric.directlyOn;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(BaseAdapter.class)
public class ShadowBaseAdapter {
  @RealObject private BaseAdapter realBaseAdapter;
  private boolean wasNotifyDataSetChangedCalled;

  @Implementation
  public void notifyDataSetChanged() {
    wasNotifyDataSetChangedCalled = true;
    directlyOn(realBaseAdapter, BaseAdapter.class, "notifyDataSetChanged").invoke();
  }

  public void clearWasDataSetChangedCalledFlag() {
    wasNotifyDataSetChangedCalled = false;
  }

  public boolean wasNotifyDataSetChangedCalled() {
    return wasNotifyDataSetChangedCalled;
  }
}
