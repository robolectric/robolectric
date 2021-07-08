package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.widget.BaseAdapter;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(BaseAdapter.class)
public class ShadowBaseAdapter {
  @RealObject private BaseAdapter realBaseAdapter;
  private boolean wasNotifyDataSetChangedCalled;

  @Implementation
  protected void notifyDataSetChanged() {
    wasNotifyDataSetChangedCalled = true;
    reflector(BaseAdapterReflector.class, realBaseAdapter).notifyDataSetChanged();
  }

  public void clearWasDataSetChangedCalledFlag() {
    wasNotifyDataSetChangedCalled = false;
  }

  public boolean wasNotifyDataSetChangedCalled() {
    return wasNotifyDataSetChangedCalled;
  }

  @ForType(BaseAdapter.class)
  interface BaseAdapterReflector {

    @Direct
    void notifyDataSetChanged();
  }
}
