package org.robolectric.shadows;

import android.widget.BaseAdapter;
import org.robolectric.annotation.Filter;
import org.robolectric.annotation.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(BaseAdapter.class)
public class ShadowBaseAdapter {
  private boolean wasNotifyDataSetChangedCalled;

  @Filter
  protected void notifyDataSetChanged() {
    wasNotifyDataSetChangedCalled = true;
  }

  public void clearWasDataSetChangedCalledFlag() {
    wasNotifyDataSetChangedCalled = false;
  }

  public boolean wasNotifyDataSetChangedCalled() {
    return wasNotifyDataSetChangedCalled;
  }
}
