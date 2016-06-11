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
import org.robolectric.res.Attribute;
import org.robolectric.res.Plural;
import org.robolectric.res.ResName;
import org.robolectric.res.ResType;
import org.robolectric.res.ResourceLoader;
import org.robolectric.res.TypedResource;
import org.robolectric.res.builder.ResourceParser;
import org.robolectric.res.builder.XmlBlock;
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

  private TypedArray attrsToTypedArray(AttributeSet set, int[] attrs, int defStyleAttr, int themeResourceId, int defStyleRes) {
    List<Attribute> attributes = shadowOf(realResources.getAssets()).buildAttributes(set, attrs, defStyleAttr, themeResourceId, defStyleRes);
    ShadowAssetManager shadowAssetManager = shadowOf(realResources.getAssets());
    ResourceLoader resourceLoader = shadowAssetManager.getResourceLoader();

    CharSequence[] stringData = new CharSequence[attrs.length];
    int[] data = new int[attrs.length * ShadowAssetManager.STYLE_NUM_ENTRIES];
    int[] indices = new int[attrs.length + 1];
    int nextIndex = 0;

    for (int i = 0; i < attrs.length; i++) {
      int offset = i * ShadowAssetManager.STYLE_NUM_ENTRIES;

      Attribute attribute = Attribute.find(attributes, attrs[i], resourceLoader.getResourceIndex());
      if (attribute != null && !attribute.isNull()) {
        TypedValue typedValue = new TypedValue();
        Converter.convertAndFill(attribute, typedValue, resourceLoader, RuntimeEnvironment.getQualifiers(), true);
        //noinspection PointlessArithmeticExpression
        data[offset + ShadowAssetManager.STYLE_TYPE] = typedValue.type;
        data[offset + ShadowAssetManager.STYLE_DATA] = typedValue.type == TypedValue.TYPE_STRING ? i : typedValue.data;
        data[offset + ShadowAssetManager.STYLE_ASSET_COOKIE] = typedValue.assetCookie;
        data[offset + ShadowAssetManager.STYLE_RESOURCE_ID] = typedValue.resourceId;
        data[offset + ShadowAssetManager.STYLE_CHANGING_CONFIGURATIONS] = typedValue.changingConfigurations;
        data[offset + ShadowAssetManager.STYLE_DENSITY] = typedValue.density;
        stringData[i] = typedValue.string;

        indices[nextIndex + 1] = i;
        nextIndex++;
      }
    }

    indices[0] = nextIndex;

    TypedArray typedArray = ShadowTypedArray.create(realResources, attrs, data, indices, nextIndex, stringData);
    if (set != null) {
      shadowOf(typedArray).positionDescription = set.getPositionDescription();
    }
    return typedArray;
  }

  @Implementation
  public TypedArray obtainAttributes(AttributeSet set, int[] attrs) {
    return attrsToTypedArray(set, attrs, 0, 0, 0);
  }

  @Implementation
  public String getQuantityString(int id, int quantity, Object... formatArgs) throws Resources.NotFoundException {
    String raw = getQuantityString(id, quantity);
    return String.format(Locale.ENGLISH, raw, formatArgs);
  }

  @Implementation
  public String getQuantityString(int id, int quantity) throws Resources.NotFoundException {
    ShadowAssetManager shadowAssetManager = shadowOf(realResources.getAssets());
    ResName resName = shadowAssetManager.getResName(id);
    Plural plural = shadowAssetManager.getResourceLoader().getPlural(resName, quantity, RuntimeEnvironment.getQualifiers());
    String string = plural.getString();
    TypedResource<?> typedResource = shadowAssetManager.resolve(
        new TypedResource<>(string, ResType.CHAR_SEQUENCE), RuntimeEnvironment.getQualifiers(),
        new ResName(resName.packageName, "string", resName.name));
    return typedResource == null ? null : typedResource.asString();
  }

  @Implementation
  public InputStream openRawResource(int id) throws Resources.NotFoundException {
    ShadowAssetManager shadowAssetManager = shadowOf(realResources.getAssets());
    return shadowAssetManager.getResourceLoader().getRawValue(shadowAssetManager.getResName(id));
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
  public XmlResourceParser loadXmlResourceParser(int id, String type) throws Resources.NotFoundException {
    ShadowAssetManager shadowAssetManager = shadowOf(realResources.getAssets());
    ResName resName = shadowAssetManager.resolveResName(id);
    XmlBlock block = shadowAssetManager.getResourceLoader().getXml(resName, RuntimeEnvironment.getQualifiers());
    if (block == null) {
      throw new Resources.NotFoundException();
    }
    return ResourceParser.from(block, resName.packageName, shadowAssetManager.getResourceLoader().getResourceIndex());
  }

  @HiddenApi @Implementation
  public XmlResourceParser loadXmlResourceParser(String file, int id, int assetCookie, String type) throws Resources.NotFoundException {
    return loadXmlResourceParser(id, type);
  }

  @Implements(Resources.Theme.class)
  public static class ShadowTheme {
    @RealObject Resources.Theme realTheme;
    protected Resources resources;
    private int styleResourceId;

    @Implementation
    public void applyStyle(int resid, boolean force) {
      if (styleResourceId == 0) {
        this.styleResourceId = resid;
      }

      ShadowAssetManager.applyThemeStyle(styleResourceId, resid, force);
    }

    @Implementation
    public void setTo(Resources.Theme other) {
      this.styleResourceId = shadowOf(other).styleResourceId;
    }

    public int getStyleResourceId() {
      return styleResourceId;
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
      return shadowOf(getResources()).attrsToTypedArray(set, attrs, defStyleAttr, styleResourceId, defStyleRes);
    }

    @Implementation
    public Resources getResources() {
      return ReflectionHelpers.getField(realTheme, "this$0");
    }
  }

  @Implementation
  public final Resources.Theme newTheme() {
    Resources.Theme theme = directlyOn(realResources, Resources.class).newTheme();
    int themeId = Integer.valueOf(ReflectionHelpers.getField(theme, "mTheme").toString()); // TODO: in Lollipop, these can be longs, which will overflow int
    shadowOf(realResources.getAssets()).setTheme(themeId, theme);
    return theme;
  }

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
