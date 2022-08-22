package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT_WATCH;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.N_MR1;
import static android.os.Build.VERSION_CODES.Q;
import static org.robolectric.shadows.ShadowAssetManager.legacyShadowOf;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.content.res.ResourcesImpl;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.os.ParcelFileDescriptor;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.LongSparseArray;
import android.util.TypedValue;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.Bootstrap;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.internal.bytecode.ShadowedObject;
import org.robolectric.res.Plural;
import org.robolectric.res.PluralRules;
import org.robolectric.res.ResName;
import org.robolectric.res.ResType;
import org.robolectric.res.ResourceTable;
import org.robolectric.res.TypedResource;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowLegacyResourcesImpl.ShadowLegacyThemeImpl;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/** Shadow of {@link Resources}. */
@Implements(Resources.class)
public class ShadowResources {

  private static Resources system = null;
  private static List<LongSparseArray<?>> resettableArrays;

  @RealObject Resources realResources;
  private final Set<OnConfigurationChangeListener> configurationChangeListeners = new HashSet<>();

  @Resetter
  public static void reset() {
    if (resettableArrays == null) {
      resettableArrays = obtainResettableArrays();
    }
    for (LongSparseArray<?> sparseArray : resettableArrays) {
      sparseArray.clear();
    }
    system = null;

    ReflectionHelpers.setStaticField(Resources.class, "mSystem", null);
  }

  @Implementation
  protected static Resources getSystem() {
    if (system == null) {
      AssetManager assetManager = AssetManager.getSystem();
      DisplayMetrics metrics = new DisplayMetrics();
      Configuration config = new Configuration();
      system = new Resources(assetManager, metrics, config);
      Bootstrap.updateConfiguration(system);
    }
    return system;
  }

  @Implementation
  protected TypedArray obtainAttributes(AttributeSet set, int[] attrs) {
    if (isLegacyAssetManager()) {
      return legacyShadowOf(realResources.getAssets())
          .attrsToTypedArray(realResources, set, attrs, 0, 0, 0);
    } else {
      return reflector(ResourcesReflector.class, realResources).obtainAttributes(set, attrs);
    }
  }

  @Implementation
  protected String getQuantityString(int id, int quantity, Object... formatArgs)
      throws Resources.NotFoundException {
    if (isLegacyAssetManager()) {
      String raw = getQuantityString(id, quantity);
      return String.format(Locale.ENGLISH, raw, formatArgs);
    } else {
      return reflector(ResourcesReflector.class, realResources)
          .getQuantityString(id, quantity, formatArgs);
    }
  }

  @Implementation
  protected String getQuantityString(int resId, int quantity) throws Resources.NotFoundException {
    if (isLegacyAssetManager()) {
      ShadowLegacyAssetManager shadowAssetManager = legacyShadowOf(realResources.getAssets());

      TypedResource typedResource =
          shadowAssetManager.getResourceTable().getValue(resId, shadowAssetManager.config);
      if (typedResource != null && typedResource instanceof PluralRules) {
        PluralRules pluralRules = (PluralRules) typedResource;
        Plural plural = pluralRules.find(quantity);

        if (plural == null) {
          return null;
        }

        TypedResource<?> resolvedTypedResource =
            shadowAssetManager.resolve(
                new TypedResource<>(
                    plural.getString(), ResType.CHAR_SEQUENCE, pluralRules.getXmlContext()),
                shadowAssetManager.config,
                resId);
        return resolvedTypedResource == null ? null : resolvedTypedResource.asString();
      } else {
        return null;
      }
    } else {
      return reflector(ResourcesReflector.class, realResources).getQuantityString(resId, quantity);
    }
  }

  @Implementation
  protected InputStream openRawResource(int id) throws Resources.NotFoundException {
    if (isLegacyAssetManager()) {
      ShadowLegacyAssetManager shadowAssetManager = legacyShadowOf(realResources.getAssets());
      ResourceTable resourceTable = shadowAssetManager.getResourceTable();
      InputStream inputStream = resourceTable.getRawValue(id, shadowAssetManager.config);
      if (inputStream == null) {
        throw newNotFoundException(id);
      } else {
        return inputStream;
      }
    } else {
      return reflector(ResourcesReflector.class, realResources).openRawResource(id);
    }
  }

  /**
   * Since {@link AssetFileDescriptor}s are not yet supported by Robolectric, {@code null} will be
   * returned if the resource is found. If the resource cannot be found, {@link
   * Resources.NotFoundException} will be thrown.
   */
  @Implementation
  protected AssetFileDescriptor openRawResourceFd(int id) throws Resources.NotFoundException {
    if (isLegacyAssetManager()) {
      InputStream inputStream = openRawResource(id);
      if (!(inputStream instanceof FileInputStream)) {
        // todo fixme
        return null;
      }

      FileInputStream fis = (FileInputStream) inputStream;
      try {
        return new AssetFileDescriptor(
            ParcelFileDescriptor.dup(fis.getFD()), 0, fis.getChannel().size());
      } catch (IOException e) {
        throw newNotFoundException(id);
      }
    } else {
      return reflector(ResourcesReflector.class, realResources).openRawResourceFd(id);
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
  protected TypedArray obtainTypedArray(int id) throws Resources.NotFoundException {
    if (isLegacyAssetManager()) {
      ShadowLegacyAssetManager shadowAssetManager = legacyShadowOf(realResources.getAssets());
      TypedArray typedArray = shadowAssetManager.getTypedArrayResource(realResources, id);
      if (typedArray != null) {
        return typedArray;
      } else {
        throw newNotFoundException(id);
      }
    } else {
      return reflector(ResourcesReflector.class, realResources).obtainTypedArray(id);
    }
  }

  @HiddenApi
  @Implementation
  protected XmlResourceParser loadXmlResourceParser(int resId, String type)
      throws Resources.NotFoundException {
    if (isLegacyAssetManager()) {
      ShadowLegacyAssetManager shadowAssetManager = legacyShadowOf(realResources.getAssets());
      return setSourceResourceId(shadowAssetManager.loadXmlResourceParser(resId, type), resId);
    } else {
      ResourcesReflector relectedResources = reflector(ResourcesReflector.class, realResources);
      return setSourceResourceId(relectedResources.loadXmlResourceParser(resId, type), resId);
    }
  }

  @HiddenApi
  @Implementation
  protected XmlResourceParser loadXmlResourceParser(
      String file, int id, int assetCookie, String type) throws Resources.NotFoundException {
    if (isLegacyAssetManager()) {
      return loadXmlResourceParser(id, type);
    } else {
      ResourcesReflector relectedResources = reflector(ResourcesReflector.class, realResources);
      return setSourceResourceId(
          relectedResources.loadXmlResourceParser(file, id, assetCookie, type), id);
    }
  }

  private static XmlResourceParser setSourceResourceId(XmlResourceParser parser, int resourceId) {
    Object shadow = parser instanceof ShadowedObject ? Shadow.extract(parser) : null;
    if (shadow instanceof ShadowXmlBlock.ShadowParser) {
      ((ShadowXmlBlock.ShadowParser) shadow).setSourceResourceId(resourceId);
    }
    return parser;
  }

  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  protected Drawable loadDrawable(TypedValue value, int id) {
    Drawable drawable = reflector(ResourcesReflector.class, realResources).loadDrawable(value, id);
    setCreatedFromResId(realResources, id, drawable);
    return drawable;
  }

  @Implementation(minSdk = LOLLIPOP, maxSdk = N_MR1)
  protected Drawable loadDrawable(TypedValue value, int id, Resources.Theme theme)
      throws Resources.NotFoundException {
    Drawable drawable =
        reflector(ResourcesReflector.class, realResources).loadDrawable(value, id, theme);
    setCreatedFromResId(realResources, id, drawable);
    return drawable;
  }

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

  /**
   * Returns the layout resource id the attribute set was inflated from. Backwards compatible
   * version of {@link Resources#getAttributeSetSourceResId(AttributeSet)}, passes through to the
   * underlying implementation on API levels where it is supported.
   */
  @Implementation(minSdk = Q)
  public static int getAttributeSetSourceResId(AttributeSet attrs) {
    if (RuntimeEnvironment.getApiLevel() >= Q) {
      return reflector(ResourcesReflector.class).getAttributeSetSourceResId(attrs);
    } else {
      Object shadow = attrs instanceof ShadowedObject ? Shadow.extract(attrs) : null;
      return shadow instanceof ShadowXmlBlock.ShadowParser
          ? ((ShadowXmlBlock.ShadowParser) shadow).getSourceResourceId()
          : 0;
    }
  }

  /**
   * Listener callback that's called when the configuration is updated for a resources. The callback
   * receives the old and new configs (and can use {@link Configuration#diff(Configuration)} to
   * produce a diff). The callback is called after the configuration has been applied to the
   * underlying resources, so obtaining resources will use the new configuration in the callback.
   */
  public interface OnConfigurationChangeListener {
    void onConfigurationChange(
        Configuration oldConfig, Configuration newConfig, DisplayMetrics newMetrics);
  }

  /**
   * Add a listener to observe resource configuration changes. See {@link
   * OnConfigurationChangeListener}.
   */
  public void addConfigurationChangeListener(OnConfigurationChangeListener listener) {
    configurationChangeListeners.add(listener);
  }

  /**
   * Remove a listener to observe resource configuration changes. See {@link
   * OnConfigurationChangeListener}.
   */
  public void removeConfigurationChangeListener(OnConfigurationChangeListener listener) {
    configurationChangeListeners.remove(listener);
  }

  @Implementation
  protected void updateConfiguration(
      Configuration config, DisplayMetrics metrics, CompatibilityInfo compat) {
    Configuration oldConfig;
    try {
      oldConfig = new Configuration(realResources.getConfiguration());
    } catch (NullPointerException e) {
      // In old versions of Android the resource constructor calls updateConfiguration, in the
      // app compat ResourcesWrapper subclass the reference to the underlying resources hasn't been
      // configured yet, so it'll throw an NPE, catch this to avoid crashing.
      oldConfig = null;
    }
    reflector(ResourcesReflector.class, realResources).updateConfiguration(config, metrics, compat);
    if (oldConfig != null && config != null) {
      for (OnConfigurationChangeListener listener : configurationChangeListeners) {
        listener.onConfigurationChange(oldConfig, config, metrics);
      }
    }
  }

  /** Base class for shadows of {@link Resources.Theme}. */
  public abstract static class ShadowTheme {

    /** Shadow picker for {@link ShadowTheme}. */
    public static class Picker extends ResourceModeShadowPicker<ShadowTheme> {

      public Picker() {
        super(ShadowLegacyTheme.class, null, null);
      }
    }
  }

  /** Shadow for {@link Resources.Theme}. */
  @Implements(value = Resources.Theme.class, shadowPicker = ShadowTheme.Picker.class)
  public static class ShadowLegacyTheme extends ShadowTheme {
    @RealObject Resources.Theme realTheme;

    long getNativePtr() {
      if (RuntimeEnvironment.getApiLevel() >= N) {
        ResourcesImpl.ThemeImpl themeImpl = ReflectionHelpers.getField(realTheme, "mThemeImpl");
        return ((ShadowLegacyThemeImpl) Shadow.extract(themeImpl)).getNativePtr();
      } else {
        return ((Number) ReflectionHelpers.getField(realTheme, "mTheme")).longValue();
      }
    }

    @Implementation(maxSdk = M)
    protected TypedArray obtainStyledAttributes(int[] attrs) {
      return obtainStyledAttributes(0, attrs);
    }

    @Implementation(maxSdk = M)
    protected TypedArray obtainStyledAttributes(int resid, int[] attrs)
        throws Resources.NotFoundException {
      return obtainStyledAttributes(null, attrs, 0, resid);
    }

    @Implementation(maxSdk = M)
    protected TypedArray obtainStyledAttributes(
        AttributeSet set, int[] attrs, int defStyleAttr, int defStyleRes) {
      return getShadowAssetManager()
          .attrsToTypedArray(
              innerGetResources(), set, attrs, defStyleAttr, getNativePtr(), defStyleRes);
    }

    private ShadowLegacyAssetManager getShadowAssetManager() {
      return legacyShadowOf(innerGetResources().getAssets());
    }

    private Resources innerGetResources() {
      if (RuntimeEnvironment.getApiLevel() >= LOLLIPOP) {
        return realTheme.getResources();
      }
      return ReflectionHelpers.getField(realTheme, "this$0");
    }
  }

  static void setCreatedFromResId(Resources resources, int id, Drawable drawable) {
    // todo: this kinda sucks, find some better way...
    if (drawable != null && Shadow.extract(drawable) instanceof ShadowDrawable) {
      ShadowDrawable shadowDrawable = Shadow.extract(drawable);

      String resourceName;
      try {
        resourceName = resources.getResourceName(id);
      } catch (NotFoundException e) {
        resourceName = "Unknown resource #0x" + Integer.toHexString(id);
      }

      shadowDrawable.setCreatedFromResId(id, resourceName);
    }
  }

  private boolean isLegacyAssetManager() {
    return ShadowAssetManager.useLegacy();
  }

  /** Shadow for {@link Resources.NotFoundException}. */
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

    @Override
    @Implementation
    public String toString() {
      return realObject.getClass().getName() + ": " + message;
    }
  }

  @ForType(Resources.class)
  interface ResourcesReflector {

    @Direct
    XmlResourceParser loadXmlResourceParser(int resId, String type);

    @Direct
    XmlResourceParser loadXmlResourceParser(String file, int id, int assetCookie, String type);

    @Direct
    Drawable loadDrawable(TypedValue value, int id);

    @Direct
    Drawable loadDrawable(TypedValue value, int id, Resources.Theme theme);

    @Direct
    TypedArray obtainAttributes(AttributeSet set, int[] attrs);

    @Direct
    String getQuantityString(int id, int quantity, Object... formatArgs);

    @Direct
    String getQuantityString(int resId, int quantity);

    @Direct
    InputStream openRawResource(int id);

    @Direct
    AssetFileDescriptor openRawResourceFd(int id);

    @Direct
    TypedArray obtainTypedArray(int id);

    @Direct
    int getAttributeSetSourceResId(AttributeSet attrs);

    @Direct
    void updateConfiguration(
        Configuration config, DisplayMetrics metrics, CompatibilityInfo compat);
  }
}
