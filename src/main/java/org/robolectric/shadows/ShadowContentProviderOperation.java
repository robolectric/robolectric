package org.robolectric.shadows;

import static org.fest.reflect.core.Reflection.field;

import java.util.Map;

import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import android.content.ContentProviderOperation;
import android.content.ContentValues;

/**
 * Shadow for {@link ContentProviderOperation}. Gives access to operation internal properties.
 */
@Implements(ContentProviderOperation.class)
public class ShadowContentProviderOperation {

  public final static int TYPE_INSERT = 1;
  public final static int TYPE_UPDATE = 2;
  public final static int TYPE_DELETE = 3;
  public final static int TYPE_ASSERT = 4;

  @RealObject
  private ContentProviderOperation realOperation;

  public int getType() {
    return field("mType").ofType(int.class).in(realOperation).get();
  }

  public String getSelection() {
    return field("mSelection").ofType(String.class).in(realOperation).get();
  }
  public String[] getSelectionArgs() {
    return field("mSelectionArgs").ofType(String[].class).in(realOperation).get();
  }

  public ContentValues getContentValues() {
    return field("mValues").ofType(ContentValues.class).in(realOperation).get();
  }

  public Integer getExpectedCount() {
    return field("mExpectedCount").ofType(Integer.class).in(realOperation).get();
  }

  public ContentValues getValuesBackReferences() {
    return field("mValuesBackReferences").ofType(ContentValues.class).in(realOperation).get();
  }

  @SuppressWarnings("unchecked")
  public Map<Integer, Integer> getSelectionArgsBackReferences() {
    return field("mSelectionArgsBackReferences").ofType(Map.class).in(realOperation).get();
  }

}
