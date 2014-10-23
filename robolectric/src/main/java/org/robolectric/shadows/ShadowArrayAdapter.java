package org.robolectric.shadows;

import android.widget.ArrayAdapter;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import java.lang.reflect.Field;

@SuppressWarnings( { "UnusedDeclaration" })
@Implements(ArrayAdapter.class)
public class ShadowArrayAdapter<T> extends ShadowBaseAdapter {
  @RealObject private ArrayAdapter realArrayAdapter;

  public int getTextViewResourceId() {
    try {
      Field mFieldId = ArrayAdapter.class.getDeclaredField("mFieldId");
      mFieldId.setAccessible(true);
      return mFieldId.getInt(realArrayAdapter);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (NoSuchFieldException e) {
      throw new RuntimeException(e);
    }
  }

  public int getResourceId() {
    try {
      Field mResource = ArrayAdapter.class.getDeclaredField("mResource");
      mResource.setAccessible(true);
      return mResource.getInt(realArrayAdapter);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (NoSuchFieldException e) {
      throw new RuntimeException(e);
    }
  }
}