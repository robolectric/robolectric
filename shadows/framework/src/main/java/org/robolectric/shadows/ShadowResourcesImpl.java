package org.robolectric.shadows;

import static org.robolectric.shadows.ShadowAssetManager.useLegacy;

import android.content.res.Resources;
import android.util.LongSparseArray;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import org.robolectric.shadow.api.ShadowPicker;
import org.robolectric.shadows.ShadowLegacyResourcesImpl.ShadowLegacyThemeImpl;

abstract public class ShadowResourcesImpl {

  public static class Picker implements ShadowPicker<ShadowResourcesImpl> {
    @Override
    public Class<? extends ShadowResourcesImpl> pickShadowClass() {
      if (useLegacy()) {
        return ShadowLegacyResourcesImpl.class;
      } else {
        return ShadowArscResourcesImpl.class;
      }
    }
  }

  private static List<LongSparseArray<?>> resettableArrays;

  public static void reset() {
    if (resettableArrays == null) {
      resettableArrays = obtainResettableArrays();
    }
    for (LongSparseArray<?> sparseArray : resettableArrays) {
      sparseArray.clear();
    }
  }

  private static List<LongSparseArray<?>> obtainResettableArrays() {
    List<LongSparseArray<?>> resettableArrays = new ArrayList<>();
    Field[] allFields = Resources.class.getDeclaredFields();
    for (Field field : allFields) {
      if (Modifier.isStatic(field.getModifiers()) && field.getType().equals(LongSparseArray.class)) {
        field.setAccessible(true);
        try {
          LongSparseArray<?> longSparseArray = (LongSparseArray<?>) field.get(null);
          if (longSparseArray != null) {
            resettableArrays.add(longSparseArray);
          }
        } catch (IllegalAccessException e) {
          throw new RuntimeException(e);
        }
      }
    }
    return resettableArrays;
  }

  abstract public static class ShadowThemeImpl {
    public static class Picker implements ShadowPicker<ShadowThemeImpl> {
      @Override
      public Class<? extends ShadowThemeImpl> pickShadowClass() {
        if (useLegacy()) {
          return ShadowLegacyThemeImpl.class;
        } else {
          return null;
        }
      }
    }
  }
}
