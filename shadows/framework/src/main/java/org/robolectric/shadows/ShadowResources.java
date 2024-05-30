package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.N_MR1;
import static android.os.Build.VERSION_CODES.Q;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.LongSparseArray;
import android.util.TypedValue;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.Bootstrap;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.internal.bytecode.ShadowedObject;
import org.robolectric.shadow.api.Shadow;
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
      system =
          new Resources(assetManager, Bootstrap.getDisplayMetrics(), Bootstrap.getConfiguration());
    }
    return system;
  }

  @HiddenApi
  @Implementation
  protected XmlResourceParser loadXmlResourceParser(int resId, String type)
      throws Resources.NotFoundException {

    ResourcesReflector relectedResources = reflector(ResourcesReflector.class, realResources);
    return setSourceResourceId(relectedResources.loadXmlResourceParser(resId, type), resId);
  }

  @HiddenApi
  @Implementation
  protected XmlResourceParser loadXmlResourceParser(
      String file, int id, int assetCookie, String type) throws Resources.NotFoundException {

    ResourcesReflector relectedResources = reflector(ResourcesReflector.class, realResources);
    return setSourceResourceId(
        relectedResources.loadXmlResourceParser(file, id, assetCookie, type), id);
  }

  private static XmlResourceParser setSourceResourceId(XmlResourceParser parser, int resourceId) {
    Object shadow = parser instanceof ShadowedObject ? Shadow.extract(parser) : null;
    if (shadow instanceof ShadowXmlBlock.ShadowParser) {
      ((ShadowXmlBlock.ShadowParser) shadow).setSourceResourceId(resourceId);
    }
    return parser;
  }

  @Implementation(maxSdk = N_MR1)
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
