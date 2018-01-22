package org.robolectric.shadows;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import java.util.Map;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;

@Implements(ContentProviderOperation.class)
public class ShadowContentProviderOperation {
  public final static int TYPE_INSERT = 1;
  public final static int TYPE_UPDATE = 2;
  public final static int TYPE_DELETE = 3;
  public final static int TYPE_ASSERT = 4;

  @RealObject
  private ContentProviderOperation realOperation;

  @HiddenApi @Implementation
  public int getType() {
    return getFieldReflectively("mType", Integer.class);
  }

  public String getSelection() {
    return getFieldReflectively("mSelection", String.class);
  }
  public String[] getSelectionArgs() {
    return getFieldReflectively("mSelectionArgs", String[].class);
  }

  public ContentValues getContentValues() {
    return getFieldReflectively("mValues", ContentValues.class);
  }

  public Integer getExpectedCount() {
    return getFieldReflectively("mExpectedCount", Integer.class);
  }

  public ContentValues getValuesBackReferences() {
    return getFieldReflectively("mValuesBackReferences", ContentValues.class);
  }

  @SuppressWarnings("unchecked")
  public Map<Integer, Integer> getSelectionArgsBackReferences() {
    return getFieldReflectively("mSelectionArgsBackReferences", Map.class);
  }

  private <T> T getFieldReflectively(String fieldName, Class<T> clazz) {
    return clazz.cast(ReflectionHelpers.getField(realOperation, fieldName));
  }
}
