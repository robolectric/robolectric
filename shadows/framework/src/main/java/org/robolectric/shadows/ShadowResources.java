package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.shadow.api.Shadow.directlyOn;
import static org.robolectric.shadows.ShadowAssetManager.legacyShadowOf;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.ResourcesImpl;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.ParcelFileDescriptor;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.LongSparseArray;
import android.util.TypedValue;
import android.view.Display;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.res.Plural;
import org.robolectric.res.PluralRules;
import org.robolectric.res.ResName;
import org.robolectric.res.ResType;
import org.robolectric.res.ResourceTable;
import org.robolectric.res.TypedResource;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

@Implements(Resources.class)
public class ShadowResources {
  private static Resources system = null;
  private static List<LongSparseArray<?>> resettableArrays;

  @RealObject Resources realResources;

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
  public TypedArray obtainAttributes(AttributeSet set, int[] attrs) {
    if (isLegacyAssetManager()) {
      return legacyShadowOf(realResources.getAssets())
          .attrsToTypedArray(realResources, set, attrs, 0, 0, 0);
    } else {
      return directlyOn(realResources, Resources.class, "obtainAttributes",
          ClassParameter.from(AttributeSet.class, set),
          ClassParameter.from(int[].class, attrs)
      );
    }
  }

  private boolean isLegacyAssetManager() {
    return ShadowArscAssetManager.isLegacyAssetManager(realResources.getAssets());
  }

  @Implementation
  public String getQuantityString(int id, int quantity, Object... formatArgs) throws Resources.NotFoundException {
    String raw = getQuantityString(id, quantity);
    return String.format(Locale.ENGLISH, raw, formatArgs);
  }

  @Implementation
  public String getQuantityString(int resId, int quantity) throws Resources.NotFoundException {
    if (isLegacyAssetManager()) {
      ShadowAssetManager shadowAssetManager = legacyShadowOf(realResources.getAssets());

      TypedResource typedResource = shadowAssetManager.getResourceTable()
          .getValue(resId, shadowAssetManager.config);
      if (typedResource != null && typedResource instanceof PluralRules) {
        PluralRules pluralRules = (PluralRules) typedResource;
        Plural plural = pluralRules.find(quantity);

        if (plural == null) {
          return null;
        }

        TypedResource<?> resolvedTypedResource = shadowAssetManager.resolve(
            new TypedResource<>(plural.getString(), ResType.CHAR_SEQUENCE, pluralRules.getXmlContext()),
            shadowAssetManager.config, resId);
        return resolvedTypedResource == null ? null : resolvedTypedResource.asString();
      } else {
        return null;
      }
    }
    else {
        return directlyOn(realResources, Resources.class, "getQuantityString",
            ClassParameter.from(int.class, resId), ClassParameter.from(int.class, quantity));
    }
  }

  @Implementation
  public InputStream openRawResource(int id) throws Resources.NotFoundException {
    if (isLegacyAssetManager()) {
      ShadowAssetManager shadowAssetManager = legacyShadowOf(realResources.getAssets());
      ResourceTable resourceTable = shadowAssetManager.getResourceTable();
      InputStream inputStream = resourceTable.getRawValue(id, shadowAssetManager.config);
      if (inputStream == null) {
        throw newNotFoundException(id);
      } else {
        return inputStream;
      }
    } else {
      return directlyOn(realResources, Resources.class).openRawResource(id);
    }
  }

 /**
  * Since {@link AssetFileDescriptor}s are not yet supported by Robolectric, {@code null} will
  * be returned if the resource is found. If the resource cannot be found, {@link Resources.NotFoundException} will
  * be thrown.
  */
 @Implementation
 public AssetFileDescriptor openRawResourceFd(int id) throws Resources.NotFoundException {
   if (isLegacyAssetManager()) {
     InputStream inputStream = openRawResource(id);
     if (!(inputStream instanceof FileInputStream)) {
       // todo fixme
       return null;
     }

     FileInputStream fis = (FileInputStream) inputStream;
     try {
       return new AssetFileDescriptor(ParcelFileDescriptor.dup(fis.getFD()), 0,
           fis.getChannel().size());
     } catch (IOException e) {
       throw newNotFoundException(id);
     }
   } else {
     return directlyOn(realResources, Resources.class).openRawResourceFd(id);
   }
 }

  private Resources.NotFoundException newNotFoundException(int id) {
    ResourceTable resourceTable = legacyShadowOf(realResources.getAssets()).getResourceTable();
    ResName resName = resourceTable.getResName(id);
    if (resName == null) {
      return new Resources.NotFoundException("resource ID #0x" + Integer.toHexString(id));
    } else {
      return new Resources.NotFoundException(resName.getFullyQualifiedName());
    }
  }

  @Implementation
  public TypedArray obtainTypedArray(int id) throws Resources.NotFoundException {
    if (isLegacyAssetManager()) {
      ShadowAssetManager shadowAssetManager = legacyShadowOf(realResources.getAssets());
      TypedArray typedArray = shadowAssetManager.getTypedArrayResource(realResources, id);
      if (typedArray != null) {
        return typedArray;
      } else {
        throw newNotFoundException(id);
      }
    } else {
      return directlyOn(realResources, Resources.class, "obtainTypedArray",
          new ClassParameter(int.class, id));
    }
  }

  /**
   * @deprecated Set screen density using {@link Config#qualifiers()} instead.
   */
  @Deprecated
  public void setDensity(float density) {
    realResources.getDisplayMetrics().density = density;
  }

  /**
   * @deprecated Set screen density using {@link Config#qualifiers()} instead.
   */
  @Deprecated
  public void setScaledDensity(float scaledDensity) {
    realResources.getDisplayMetrics().scaledDensity = scaledDensity;
  }

  /**
   * @deprecated Set up display using {@link Config#qualifiers()} instead.
   */
  @Deprecated
  public void setDisplay(Display display) {
     DisplayMetrics displayMetrics = realResources.getDisplayMetrics() ;

      display.getMetrics(displayMetrics);
    }


 @HiddenApi @Implementation
 public XmlResourceParser loadXmlResourceParser(int resId, String type) throws Resources.NotFoundException {
   if (isLegacyAssetManager()) {
     ShadowAssetManager shadowAssetManager = legacyShadowOf(realResources.getAssets());
     return shadowAssetManager.loadXmlResourceParser(resId, type);
   } else {
     return directlyOn(realResources, Resources.class, "loadXmlResourceParser",
         ClassParameter.from(int.class, resId),
         ClassParameter.from(String.class, type));
   }
 }

 @HiddenApi @Implementation
 public XmlResourceParser loadXmlResourceParser(String file, int id, int assetCookie, String type) throws Resources.NotFoundException {
   if (isLegacyAssetManager()) {
     return loadXmlResourceParser(id, type);
   } else {
     return directlyOn(realResources, Resources.class, "loadXmlResourceParser",
         ClassParameter.from(String.class, file),
         ClassParameter.from(int.class, id),
         ClassParameter.from(int.class, assetCookie),
         ClassParameter.from(String.class, type));
   }
 }

  @Implements(value = Resources.Theme.class)
  public static class ShadowTheme {
    @RealObject Resources.Theme realTheme;

    long getNativePtr() {
      if (RuntimeEnvironment.getApiLevel() >= N) {
        ResourcesImpl.ThemeImpl themeImpl = ReflectionHelpers.getField(realTheme, "mThemeImpl");
        return ((ShadowResourcesImpl.ShadowThemeImpl) Shadow.extract(themeImpl)).getNativePtr();
      } else {
        return ((Number) ReflectionHelpers.getField(realTheme, "mTheme")).longValue();
      }
    }

    @Implementation(maxSdk = M)
    public TypedArray obtainStyledAttributes(int[] attrs) {
      return obtainStyledAttributes(0, attrs);
    }

    @Implementation(maxSdk = M)
    public TypedArray obtainStyledAttributes(int resid, int[] attrs) throws android.content.res.Resources.NotFoundException {
      return obtainStyledAttributes(null, attrs, 0, resid);
    }

    @Implementation(maxSdk = M)
    public TypedArray obtainStyledAttributes(AttributeSet set, int[] attrs, int defStyleAttr, int defStyleRes) {
      if (ShadowArscAssetManager.isLegacyAssetManager(getResources().getAssets())) {
        return getShadowAssetManager().attrsToTypedArray(getResources(), set, attrs, defStyleAttr, getNativePtr(), defStyleRes);
      } else {
        return directlyOn(realTheme, Resources.Theme.class, "obtainStyledAttributes",
            ClassParameter.from(AttributeSet.class, set), ClassParameter.from(int[].class, attrs),
            ClassParameter.from(int.class, defStyleAttr),
            ClassParameter.from(int.class, defStyleRes));
      }
    }

    private ShadowAssetManager getShadowAssetManager() {
      return legacyShadowOf(getResources().getAssets());
    }

    private Resources getResources() {
      return ReflectionHelpers.getField(realTheme, "this$0");
    }
  }

  @HiddenApi @Implementation
  public Drawable loadDrawable(TypedValue value, int id) {
    Drawable drawable = directlyOn(realResources, Resources.class, "loadDrawable",
        ClassParameter.from(TypedValue.class, value), ClassParameter.from(int.class, id));
    setCreatedFromResId(realResources, id, drawable);
    return drawable;
  }

  @Implementation
  public Drawable loadDrawable(TypedValue value, int id, Resources.Theme theme) throws Resources.NotFoundException {
    Drawable drawable = directlyOn(realResources, Resources.class, "loadDrawable",
        ClassParameter.from(TypedValue.class, value), ClassParameter.from(int.class, id), ClassParameter.from(Resources.Theme.class, theme));
    setCreatedFromResId(realResources, id, drawable);
    return drawable;
  }

  static void setCreatedFromResId(Resources resources, int id, Drawable drawable) {
    // todo: this kinda sucks, find some better way...
    if (drawable != null && Shadow.extract(drawable) instanceof ShadowDrawable) {
      shadowOf(drawable).createdFromResId = id;
      if (drawable instanceof BitmapDrawable) {
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        if (bitmap != null  && Shadow.extract(bitmap) instanceof ShadowBitmap) {
          ShadowBitmap shadowBitmap = shadowOf(bitmap);
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
    public void __constructor__() {}

    @Implementation
    public void __constructor__(String name) {
      this.message = name;
    }

    @Override @Implementation
    public String toString() {
      return realObject.getClass().getName() + ": " + message;
    }
  }
}
