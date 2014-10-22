package org.robolectric.shadows;

import android.widget.ArrayAdapter;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import static org.fest.reflect.core.Reflection.field;

@SuppressWarnings( { "UnusedDeclaration" })
@Implements(ArrayAdapter.class)
public class ShadowArrayAdapter<T> extends ShadowBaseAdapter {
  @RealObject private ArrayAdapter realArrayAdapter;

  public int getTextViewResourceId() {
    return field("mFieldId").ofType(int.class).in(realArrayAdapter).get();
  }

  public int getResourceId() {
    return field("mResource").ofType(int.class).in(realArrayAdapter).get();
  }
}