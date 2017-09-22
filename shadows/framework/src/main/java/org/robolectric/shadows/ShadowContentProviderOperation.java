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
    return getFieldReflectively("mType");
  }

  public String getSelection() {
    return getFieldReflectively("mSelection");
  }
  public String[] getSelectionArgs() {
    return getFieldReflectively("mSelectionArgs");
  }

  public ContentValues getContentValues() {
    return getFieldReflectively("mValues");
  }

  public Integer getExpectedCount() {
    return getFieldReflectively("mExpectedCount");
  }

  public ContentValues getValuesBackReferences() {
    return getFieldReflectively("mValuesBackReferences");
  }

  @SuppressWarnings("unchecked")
  public Map<Integer, Integer> getSelectionArgsBackReferences() {
    return getFieldReflectively("mSelectionArgsBackReferences");
  }

  private <T> T getFieldReflectively(String fieldName) {
    return ReflectionHelpers.getField(realOperation, fieldName);
  }
}
