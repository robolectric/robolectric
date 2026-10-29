package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.N_MR1;
import static android.os.Build.VERSION_CODES.O;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.res.Resources;
import android.content.res.ResourcesImpl;
import android.graphics.drawable.Drawable;
import android.util.LongSparseArray;
import android.util.TypedValue;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadows.ShadowResourcesImpl.Picker;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

@SuppressWarnings("NewApi")
@Implements(
    value = ResourcesImpl.class,
    isInAndroidSdk = false,
    minSdk = N,
    shadowPicker = Picker.class)
public class ShadowArscResourcesImpl extends ShadowResourcesImpl {
  private static List<LongSparseArray<?>> resettableArrays;

  @RealObject ResourcesImpl realResourcesImpl;

  private static List<LongSparseArray<?>> obtainResettableArrays() {
    List<LongSparseArray<?>> resettableArrays = new ArrayList<>();
    Field[] allFields = Resources.class.getDeclaredFields();
    for (Field field : allFields) {
      if (Modifier.isStatic(field.getModifiers())
          && field.getType().equals(LongSparseArray.class)) {
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

  @Implementation(maxSdk = N_MR1)
  public Drawable loadDrawable(
      Resources wrapper, TypedValue value, int id, Resources.Theme theme, boolean useCache)
      throws Resources.NotFoundException {
    Drawable drawable =
        reflector(ResourcesImplReflector.class, realResourcesImpl)
            .loadDrawable(wrapper, value, id, theme, useCache);

    ShadowResources.setCreatedFromResId(wrapper, id, drawable);
    return drawable;
  }

  @Implementation(minSdk = O)
  public Drawable loadDrawable(
      Resources wrapper, TypedValue value, int id, int density, Resources.Theme theme) {
    Drawable drawable =
        reflector(ResourcesImplReflector.class, realResourcesImpl)
            .loadDrawable(wrapper, value, id, density, theme);

    ShadowResources.setCreatedFromResId(wrapper, id, drawable);
    return drawable;
  }

  @ForType(ResourcesImpl.class)
  interface ResourcesImplReflector {

    @Direct
    Drawable loadDrawable(
        Resources wrapper, TypedValue value, int id, Resources.Theme theme, boolean useCache);

    @Direct
    Drawable loadDrawable(
        Resources wrapper, TypedValue value, int id, int density, Resources.Theme theme);
  }
}
