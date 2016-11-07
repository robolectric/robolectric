package org.robolectric.shadows;

import android.content.res.*;
import android.graphics.drawable.Drawable;
import android.os.ParcelFileDescriptor;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.LongSparseArray;
import android.util.TypedValue;
import android.view.Display;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.*;
import org.robolectric.res.Plural;
import org.robolectric.res.PluralResourceLoader;
import org.robolectric.res.ResType;
import org.robolectric.res.TypedResource;
import org.robolectric.util.ReflectionHelpers;

import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.os.Build.VERSION_CODES.N;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.internal.Shadow.directlyOn;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;

@Implements(value = ResourcesImpl.class, isInAndroidSdk = false, minSdk = N)
public class ShadowResourcesImpl {
  private static Resources system = null;
  private static List<LongSparseArray<?>> resettableArrays;

  private float density = 1.0f;
  private DisplayMetrics displayMetrics;
  private Display display;
  @RealObject
  ResourcesImpl realResourcesImpl;

  @Resetter
  public static void reset() {
    if (resettableArrays == null) {
      resettableArrays = obtainResettableArrays();
    }
    for (LongSparseArray<?> sparseArray : resettableArrays) {
      sparseArray.clear();
    }
    system = null;
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

  @Implementation
  public static Resources getSystem() {
    if (system == null) {
      AssetManager assetManager = AssetManager.getSystem();
      DisplayMetrics metrics = new DisplayMetrics();
      Configuration config = new Configuration();
      system = new Resources(assetManager, metrics, config);
    }
    return system;
  }

  @Implementation
  public String getQuantityString(int id, int quantity, Object... formatArgs) throws Resources.NotFoundException {
    String raw = getQuantityString(id, quantity);
    return String.format(Locale.ENGLISH, raw, formatArgs);
  }

  @Implementation
  public String getQuantityString(int resId, int quantity) throws Resources.NotFoundException {
    ShadowAssetManager shadowAssetManager = shadowOf(realResourcesImpl.getAssets());

    TypedResource typedResource = shadowAssetManager.getResourceLoader().getValue(resId, RuntimeEnvironment.getQualifiers());
    if (typedResource != null && typedResource instanceof PluralResourceLoader.PluralRules) {
      PluralResourceLoader.PluralRules pluralRules = (PluralResourceLoader.PluralRules) typedResource;
      Plural plural = pluralRules.find(quantity);

      if (plural == null) {
        return null;
      }

      TypedResource<?> resolvedTypedResource = shadowAssetManager.resolve(
          new TypedResource<>(plural.getString(), ResType.CHAR_SEQUENCE), RuntimeEnvironment.getQualifiers(), resId);
      return resolvedTypedResource == null ? null : resolvedTypedResource.asString();
    } else {
      return null;
    }
  }

  @Implementation
  public InputStream openRawResource(int id) throws Resources.NotFoundException {
    return shadowOf(realResourcesImpl.getAssets()).getResourceLoader().getRawValue(id);
  }

  @Implementation
  public AssetFileDescriptor openRawResourceFd(int id) throws Resources.NotFoundException {
    try {
      FileInputStream fis = (FileInputStream)openRawResource(id);
      return new AssetFileDescriptor(ParcelFileDescriptor.dup(fis.getFD()), 0, fis.getChannel().size());
    } catch (Exception e) {
      return null;
    }
  }

  public void setDensity(float density) {
    this.density = density;
    if (displayMetrics != null) {
      displayMetrics.density = density;
    }
  }

  public void setScaledDensity(float scaledDensity) {
    if (displayMetrics != null) {
      displayMetrics.scaledDensity = scaledDensity;
    }
  }

  public void setDisplay(Display display) {
    this.display = display;
    displayMetrics = null;
  }

  @Implementation
  public DisplayMetrics getDisplayMetrics() {
    if (displayMetrics == null) {
      if (display == null) {
        display = ReflectionHelpers.callConstructor(Display.class);
      }

      displayMetrics = new DisplayMetrics();
      display.getMetrics(displayMetrics);
    }
    displayMetrics.density = this.density;
    return displayMetrics;
  }

  @HiddenApi
  @Implementation
  public XmlResourceParser loadXmlResourceParser(int resId, String type) throws Resources.NotFoundException {
    ShadowAssetManager shadowAssetManager = shadowOf(realResourcesImpl.getAssets());
    return shadowAssetManager.loadXmlResourceParser(resId, type);
  }

  @HiddenApi @Implementation
  public XmlResourceParser loadXmlResourceParser(String file, int id, int assetCookie, String type) throws Resources.NotFoundException {
    return loadXmlResourceParser(id, type);
  }

  @Implements(value = ResourcesImpl.ThemeImpl.class, minSdk = N, isInAndroidSdk = false)
  public static class ShadowThemeImpl {
    @RealObject ResourcesImpl.ThemeImpl realThemeImpl;

    @Implementation
    public TypedArray obtainStyledAttributes(Resources.Theme wrapper, AttributeSet set, int[] attrs, int defStyleAttr, int defStyleRes) {
      Resources resources = wrapper.getResources();
      return shadowOf(resources.getAssets()).attrsToTypedArray(resources, set, attrs, defStyleAttr, getNativePtr(), defStyleRes);
    }

    public long getNativePtr() {
      return ReflectionHelpers.getField(realThemeImpl, "mTheme");
    }
  }

  @Implementation
  public Drawable loadDrawable(Resources wrapper, TypedValue value, int id, Resources.Theme theme, boolean useCache) throws Resources.NotFoundException {
    Drawable drawable = directlyOn(realResourcesImpl, ResourcesImpl.class, "loadDrawable",
        from(Resources.class, wrapper),
        from(TypedValue.class, value),
        from(int.class, id),
        from(Resources.Theme.class, theme),
        from(boolean.class, useCache)
    );

    ShadowResources.setCreatedFromResId(wrapper, id, drawable);
    return drawable;
  }
}
