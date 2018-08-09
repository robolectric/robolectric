package org.robolectric.shadows;

import static org.robolectric.shadows.ShadowAssetManager.useLegacy;

import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.LongSparseArray;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadow.api.ShadowPicker;
import org.robolectric.shadows.ShadowLegacyResources.ShadowLegacyTheme;

abstract public class ShadowResources {

  public static class Picker implements ShadowPicker<ShadowResources> {
    @Override
    public Class<? extends ShadowResources> pickShadowClass() {
      if (useLegacy()) {
        return ShadowLegacyResources.class;
      } else {
        if (RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.P) {
          return null;
        } else {
          return ShadowArscResources.class;
        }
      }
    }
  }

  static void reset() {
    if (resettableArrays == null) {
      resettableArrays = obtainResettableArrays();
    }
    for (LongSparseArray<?> sparseArray : resettableArrays) {
      sparseArray.clear();
    }
    system = null;
  }

  private static Resources system = null;
  private static List<LongSparseArray<?>> resettableArrays;

  protected static Resources getSystem() {
    if (system == null) {
      AssetManager assetManager = AssetManager.getSystem();
      DisplayMetrics metrics = new DisplayMetrics();
      Configuration config = new Configuration();
      system = new Resources(assetManager, metrics, config);
    }
    return system;
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

  public static abstract class ShadowTheme {

    public static class Picker implements ShadowPicker<ShadowTheme> {
      @Override
      public Class<? extends ShadowTheme> pickShadowClass() {
        if (useLegacy()) {
          return ShadowLegacyTheme.class;
        } else {
          return null;
        }
      }
    }
  }

  static void setCreatedFromResId(Resources resources, int id, Drawable drawable) {
    // todo: this kinda sucks, find some better way...
    if (drawable != null && Shadow.extract(drawable) instanceof ShadowDrawable) {
      ShadowDrawable shadowDrawable = Shadow.extract(drawable);
      shadowDrawable.createdFromResId = id;
      if (drawable instanceof BitmapDrawable) {
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        if (bitmap != null  && Shadow.extract(bitmap) instanceof ShadowBitmap) {
          ShadowBitmap shadowBitmap = Shadow.extract(bitmap);
          if (shadowBitmap.createdFromResId == -1) {
            shadowBitmap.setCreatedFromResId(id, resources.getResourceName(id));
          }
        }
      }
    }
  }

  @Implements(Resources.NotFoundException.class)
  public static class ShadowNotFoundException {
    @RealObject Resources.NotFoundException realObject;

    private String message;

    @Implementation
    protected void __constructor__() {}

    @Implementation
    protected void __constructor__(String name) {
      this.message = name;
    }

    @Override @Implementation
    public String toString() {
      return realObject.getClass().getName() + ": " + message;
    }
  }
}
