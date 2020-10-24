package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.R;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import java.util.Map;
import org.robolectric.RuntimeEnvironment;
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

  /** @deprecated implementation detail - use public Android APIs instead */
  @HiddenApi
  @Implementation
  @Deprecated
  public int getType() {
    if (RuntimeEnvironment.getApiLevel() >= R) {
      throw new UnsupportedOperationException("unsupported on Android R");
    }
    return getFieldReflectively("mType", Integer.class);
  }

  /** @deprecated implementation detail - use public Android APIs instead */
  @Deprecated
  public String getSelection() {
    if (RuntimeEnvironment.getApiLevel() >= R) {
      throw new UnsupportedOperationException("unsupported on Android R");
    }
    return getFieldReflectively("mSelection", String.class);
  }

  /** @deprecated implementation detail - use public Android APIs instead */
  @Deprecated
  public String[] getSelectionArgs() {
    if (RuntimeEnvironment.getApiLevel() >= R) {
      throw new UnsupportedOperationException("unsupported on Android R");
    }
    return getFieldReflectively("mSelectionArgs", String[].class);
  }

  /** @deprecated implementation detail - use public Android APIs instead */
  @Deprecated
  public ContentValues getContentValues() {
    if (RuntimeEnvironment.getApiLevel() >= R) {
      throw new UnsupportedOperationException("unsupported on Android R");
    }
    return getFieldReflectively("mValues", ContentValues.class);
  }

  /** @deprecated implementation detail - use public Android APIs instead */
  @Deprecated
  public Integer getExpectedCount() {
    if (RuntimeEnvironment.getApiLevel() >= R) {
      throw new UnsupportedOperationException("unsupported on Android R");
    }
    return getFieldReflectively("mExpectedCount", Integer.class);
  }

  /** @deprecated implementation detail - use public Android APIs instead */
  @Deprecated
  public ContentValues getValuesBackReferences() {
    if (RuntimeEnvironment.getApiLevel() >= R) {
      throw new UnsupportedOperationException("unsupported on Android R");
    }
    return getFieldReflectively("mValuesBackReferences", ContentValues.class);
  }

  /** @deprecated implementation detail - use public Android APIs instead */
  @SuppressWarnings("unchecked")
  @Deprecated
  public Map<Integer, Integer> getSelectionArgsBackReferences() {
    if (RuntimeEnvironment.getApiLevel() >= R) {
      throw new UnsupportedOperationException("unsupported on Android R");
    }
    return getFieldReflectively("mSelectionArgsBackReferences", Map.class);
  }

  private <T> T getFieldReflectively(String fieldName, Class<T> clazz) {
    return clazz.cast(ReflectionHelpers.getField(realOperation, fieldName));
  }
}
