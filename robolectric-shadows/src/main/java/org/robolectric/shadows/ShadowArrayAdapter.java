package org.robolectric.shadows;

import android.widget.ArrayAdapter;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;

@SuppressWarnings( { "UnusedDeclaration" })
@Implements(ArrayAdapter.class)
public class ShadowArrayAdapter<T> extends ShadowBaseAdapter {
  @RealObject private ArrayAdapter realArrayAdapter;

  public int getTextViewResourceId() {
    return ReflectionHelpers.getFieldReflectively(realArrayAdapter, "mFieldId");
  }

  public int getResourceId() {
    return ReflectionHelpers.getFieldReflectively(realArrayAdapter, "mResource");
  }
}