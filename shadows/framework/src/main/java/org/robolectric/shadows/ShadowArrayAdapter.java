package org.robolectric.shadows;

import android.widget.ArrayAdapter;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;

@SuppressWarnings("UnusedDeclaration")
@Implements(ArrayAdapter.class)
public class ShadowArrayAdapter<T> extends ShadowBaseAdapter {
  @RealObject private ArrayAdapter<T> realArrayAdapter;

  public int getTextViewResourceId() {
    return ReflectionHelpers.getField(realArrayAdapter, "mFieldId");
  }

  public int getResourceId() {
    return ReflectionHelpers.getField(realArrayAdapter, "mResource");
  }
}