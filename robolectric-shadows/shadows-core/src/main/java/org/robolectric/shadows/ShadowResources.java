package org.robolectric.shadows;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
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
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.res.Plural;
import org.robolectric.res.PluralResourceLoader;
import org.robolectric.res.ResType;
import org.robolectric.res.Style;
import org.robolectric.res.ThemeStyleSet;
import org.robolectric.res.TypedResource;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.internal.Shadow.directlyOn;
import static org.robolectric.internal.Shadow.invokeConstructor;

/**
 * Shadow for {@link android.content.res.Resources}.
 */
@Implements(Resources.class)
public class ShadowResources {
  private static Resources system = null;
  private static List<LongSparseArray<?>> resettableArrays;

  private float density = 1.0f;
  private DisplayMetrics displayMetrics;
  private Display display;
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
    return shadowOf(realResources.getAssets())
        .attrsToTypedArray(realResources, set, attrs, 0, null, 0);
  }

  @Implementation
  public String getQuantityString(int id, int quantity, Object... formatArgs) throws Resources.NotFoundException {
    String raw = getQuantityString(id, quantity);
    return String.format(Locale.ENGLISH, raw, formatArgs);
  }

  @Implementation
  public String getQuantityString(int resId, int quantity) throws Resources.NotFoundException {
    ShadowAssetManager shadowAssetManager = shadowOf(realResources.getAssets());

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
    return shadowOf(realResources.getAssets()).getResourceLoader().getRawValue(id);
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

  @HiddenApi @Implementation
  public XmlResourceParser loadXmlResourceParser(int resId, String type) throws Resources.NotFoundException {
    ShadowAssetManager shadowAssetManager = shadowOf(realResources.getAssets());
    return shadowAssetManager.loadXmlResourceParser(resId, type);
  }

  @HiddenApi @Implementation
  public XmlResourceParser loadXmlResourceParser(String file, int id, int assetCookie, String type) throws Resources.NotFoundException {
    return loadXmlResourceParser(id, type);
  }

  @Implements(Resources.Theme.class)
  public static class ShadowTheme {
    @RealObject Resources.Theme realTheme;
    private ThemeStyleSet themeStyleSet = new ThemeStyleSet();

    public void __constructor__(Resources this$0) {
      invokeConstructor(Resources.Theme.class, realTheme, ClassParameter.from(Resources.class, this$0));
      Number themePtr = ReflectionHelpers.getField(realTheme, "mTheme");
      ShadowAssetManager.saveTheme(themePtr, realTheme);
    }

    @Implementation
    public TypedArray obtainStyledAttributes(int[] attrs) {
      return obtainStyledAttributes(0, attrs);
    }

    @Implementation
    public TypedArray obtainStyledAttributes(int resid, int[] attrs) throws android.content.res.Resources.NotFoundException {
      return obtainStyledAttributes(null, attrs, 0, resid);
    }

    @Implementation
    public TypedArray obtainStyledAttributes(AttributeSet set, int[] attrs, int defStyleAttr, int defStyleRes) {
      return getShadowAssetManager().attrsToTypedArray(getResources(), set, attrs, defStyleAttr, realTheme, defStyleRes);
    }

    public ThemeStyleSet getThemeStyleSet() {
      return themeStyleSet;
    }

    public void setThemeStyleSet(ThemeStyleSet themeStyleSet) {
      this.themeStyleSet = themeStyleSet;
    }

    void doApplyStyle(int styleRes, boolean force) {
      Style style = getShadowAssetManager().resolveStyle(styleRes, null);
      themeStyleSet.apply(style, force);
    }

    private ShadowAssetManager getShadowAssetManager() {
      return shadowOf(getResources().getAssets());
    }

    private Resources getResources() {
      return ReflectionHelpers.getField(realTheme, "this$0");
    }
  }

  // todo: Android N has a ThemeImpl inside Theme
//  @Implementation
//  public final Resources.Theme newTheme() {
//    Resources.Theme theme = directlyOn(realResources, Resources.class).newTheme();
//    Object themeImpl = ReflectionHelpers.getField(theme, "mThemeImpl");
//    int themeId = Integer.valueOf(ReflectionHelpers.getField(themeImpl, "mTheme").toString()); // TODO: in Lollipop, these can be longs, which will overflow int
//    shadowOf(realResources.getAssets()).setTheme(themeId, theme);
//    return theme;
//  }

  @HiddenApi @Implementation
  public Drawable loadDrawable(TypedValue value, int id) {
    Drawable drawable = directlyOn(realResources, Resources.class, "loadDrawable",
        ClassParameter.from(TypedValue.class, value), ClassParameter.from(int.class, id));

    // todo: this kinda sucks, find some better way...
    if (drawable != null) {
      shadowOf(drawable).createdFromResId = id;
      if (drawable instanceof BitmapDrawable) {
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        if (bitmap != null) {
          ShadowBitmap shadowBitmap = shadowOf(bitmap);
          if (shadowBitmap.createdFromResId == -1) {
            shadowBitmap.setCreatedFromResId(id, shadowOf(realResources.getAssets()).getResourceName(id));
          }
        }
      }
    }
    return drawable;
  }

  @Implementation
  public Drawable loadDrawable(TypedValue value, int id, Resources.Theme theme) throws Resources.NotFoundException {
    Drawable drawable = directlyOn(realResources, Resources.class, "loadDrawable",
        ClassParameter.from(TypedValue.class, value), ClassParameter.from(int.class, id), ClassParameter.from(Resources.Theme.class, theme));

    // todo: this kinda sucks, find some better way...
    if (drawable != null) {
      shadowOf(drawable).createdFromResId = id;
      if (drawable instanceof BitmapDrawable) {
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        if (bitmap != null) {
          ShadowBitmap shadowBitmap = shadowOf(bitmap);
          if (shadowBitmap.createdFromResId == -1) {
            shadowBitmap.setCreatedFromResId(id, shadowOf(realResources.getAssets()).getResourceName(id));
          }
        }
      }
    }
    return drawable;
  }

  @Implements(Resources.NotFoundException.class)
  public static class ShadowNotFoundException {
    @RealObject Resources.NotFoundException realObject;

    private String message;

    public void __constructor__() {
    }

    public void __constructor__(String name) {
      this.message = name;
    }

    @Implementation
    public String toString() {
      return realObject.getClass().getName() + ": " + message;
    }
  }
}
