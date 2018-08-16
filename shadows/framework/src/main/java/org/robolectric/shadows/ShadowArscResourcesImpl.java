package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.N_MR1;
import static android.os.Build.VERSION_CODES.O;
import static org.robolectric.shadow.api.Shadow.directlyOn;
import static org.robolectric.shadows.ShadowAssetManager.legacyShadowOf;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.ResourcesImpl;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.os.ParcelFileDescriptor;
import android.util.AttributeSet;
import android.util.LongSparseArray;
import android.util.TypedValue;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.robolectric.RuntimeEnvironment;
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
import org.robolectric.shadows.ShadowResourcesImpl.Picker;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

@SuppressWarnings("NewApi")
@Implements(value = ResourcesImpl.class, isInAndroidSdk = false, minSdk = N,
    shadowPicker = Picker.class)
public class ShadowArscResourcesImpl extends ShadowResourcesImpl {
  private static List<LongSparseArray<?>> resettableArrays;

  @RealObject
  ResourcesImpl realResourcesImpl;

  @Resetter
  public static void reset() {
    if (RuntimeEnvironment.useLegacyResources()) {
      ShadowResourcesImpl.reset();
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

  @Implementation(maxSdk = M)
  public String getQuantityString(int id, int quantity, Object... formatArgs) throws Resources.NotFoundException {
    String raw = getQuantityString(id, quantity);
    return String.format(Locale.ENGLISH, raw, formatArgs);
  }

  @Implementation(maxSdk = M)
  public String getQuantityString(int resId, int quantity) throws Resources.NotFoundException {
    ShadowLegacyAssetManager shadowAssetManager = legacyShadowOf(realResourcesImpl.getAssets());

    TypedResource typedResource = shadowAssetManager.getResourceTable().getValue(resId, shadowAssetManager.config);
    if (typedResource != null && typedResource instanceof PluralRules) {
      PluralRules pluralRules = (PluralRules) typedResource;
      Plural plural = pluralRules.find(quantity);

      if (plural == null) {
        return null;
      }

      TypedResource<?> resolvedTypedResource = shadowAssetManager.resolve(
          new TypedResource<>(plural.getString(), ResType.CHAR_SEQUENCE, pluralRules.getXmlContext()), shadowAssetManager.config, resId);
      return resolvedTypedResource == null ? null : resolvedTypedResource.asString();
    } else {
      return null;
    }
  }

  @Implementation(maxSdk = M)
  public InputStream openRawResource(int id) throws Resources.NotFoundException {
    if (false) {
      ShadowLegacyAssetManager shadowAssetManager = legacyShadowOf(realResourcesImpl.getAssets());
      ResourceTable resourceTable = shadowAssetManager.getResourceTable();
      InputStream inputStream = resourceTable.getRawValue(id, shadowAssetManager.config);
      if (inputStream == null) {
        throw newNotFoundException(id);
      } else {
        return inputStream;
      }
    } else {
      return directlyOn(realResourcesImpl, ResourcesImpl.class, "openRawResource",
          from(int.class, id));
    }
  }

  /**
   * Since {@link AssetFileDescriptor}s are not yet supported by Robolectric, {@code null} will
   * be returned if the resource is found. If the resource cannot be found, {@link Resources.NotFoundException} will
   * be thrown.
   */
  @Implementation(maxSdk = M)
  public AssetFileDescriptor openRawResourceFd(int id) throws Resources.NotFoundException {
    InputStream inputStream = openRawResource(id);
    if (!(inputStream instanceof FileInputStream)) {
      // todo fixme
      return null;
    }

    FileInputStream fis = (FileInputStream) inputStream;
    try {
      return new AssetFileDescriptor(ParcelFileDescriptor.dup(fis.getFD()), 0, fis.getChannel().size());
    } catch (IOException e) {
      throw newNotFoundException(id);
    }
  }

  private Resources.NotFoundException newNotFoundException(int id) {
    ResourceTable resourceTable = legacyShadowOf(realResourcesImpl.getAssets()).getResourceTable();
    ResName resName = resourceTable.getResName(id);
    if (resName == null) {
      return new Resources.NotFoundException("resource ID #0x" + Integer.toHexString(id));
    } else {
      return new Resources.NotFoundException(resName.getFullyQualifiedName());
    }
  }

  @Implementation(maxSdk = N_MR1)
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

  @Implementation(minSdk = O)
  public Drawable loadDrawable(Resources wrapper,  TypedValue value, int id, int density, Resources.Theme theme) {
    Drawable drawable = directlyOn(realResourcesImpl, ResourcesImpl.class, "loadDrawable",
        from(Resources.class, wrapper),
        from(TypedValue.class, value),
        from(int.class, id),
        from(int.class, density),
        from(Resources.Theme.class, theme));

    ShadowResources.setCreatedFromResId(wrapper, id, drawable);
    return drawable;
  }
}
