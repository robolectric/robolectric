package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.N_MR1;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.O_MR1;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.annotation.SuppressLint;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.graphics.fonts.FontStyle;
import android.util.ArrayMap;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.ClassName;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.res.Fs;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/** Shadow for {@link Typeface}. */
@Implements(value = Typeface.class, isInAndroidSdk = false)
@SuppressLint("NewApi")
public class ShadowLegacyTypeface extends ShadowTypeface {
  private static final AtomicLong nextFontId = new AtomicLong(1);
  private FontDesc description;

  @RealObject Typeface realTypeface;

  @Implementation
  protected static void __staticInitializer__() {
    Shadow.directInitialize(Typeface.class);
    if (RuntimeEnvironment.getApiLevel() > R) {
      Typeface.loadPreinstalledSystemFontMap();
    }
  }

  @Implementation(minSdk = P)
  protected static Typeface create(Typeface family, int weight, boolean italic) {
    if (family == null) {
      return createUnderlyingTypeface(null, weight);
    } else {
      ShadowTypeface shadowTypeface = Shadow.extract(family);
      return createUnderlyingTypeface(shadowTypeface.getFontDescription().getFamilyName(), weight);
    }
  }

  @Implementation
  protected static Typeface create(String familyName, int style) {
    return createUnderlyingTypeface(familyName, style);
  }

  @Implementation
  protected static Typeface create(Typeface family, int style) {
    if (family == null) {
      return createUnderlyingTypeface(null, style);
    } else {
      ShadowTypeface shadowTypeface = Shadow.extract(family);
      return createUnderlyingTypeface(shadowTypeface.getFontDescription().getFamilyName(), style);
    }
  }

  @Implementation
  protected static Typeface createFromAsset(AssetManager mgr, String path) {
    ShadowAssetManager shadowAssetManager = Shadow.extract(mgr);
    Collection<Path> assetDirs = shadowAssetManager.getAllAssetDirs();
    for (Path assetDir : assetDirs) {
      Path assetFile = assetDir.resolve(path);
      if (Files.exists(assetFile)) {
        return createUnderlyingTypeface(path, Typeface.NORMAL);
      }

      // maybe path is e.g. "myFont", but we should match "myFont.ttf" too?
      Path[] files;
      try {
        files = Fs.listFiles(assetDir, f -> f.getFileName().toString().startsWith(path));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      if (files.length != 0) {
        return createUnderlyingTypeface(path, Typeface.NORMAL);
      }
    }

    throw new RuntimeException("Font asset not found " + path);
  }

  @Implementation(minSdk = O, maxSdk = P)
  protected static Typeface createFromResources(AssetManager mgr, String path, int cookie) {
    return createUnderlyingTypeface(path, Typeface.NORMAL);
  }

  @Implementation(minSdk = O)
  protected static Typeface createFromResources(
      @ClassName("android.content.res.FontResourcesParser$FamilyResourceEntry") Object entry,
      AssetManager mgr,
      String path) {
    return createUnderlyingTypeface(path, Typeface.NORMAL);
  }

  @Implementation
  protected static Typeface createFromFile(File path) {
    String familyName = path.toPath().getFileName().toString();
    return createUnderlyingTypeface(familyName, Typeface.NORMAL);
  }

  @Implementation
  protected static Typeface createFromFile(String path) {
    return createFromFile(new File(path));
  }

  @Implementation
  protected int getStyle() {
    return description.getStyle();
  }

  @Override
  @Implementation
  public boolean equals(Object o) {
    if (o instanceof Typeface) {
      Typeface other = ((Typeface) o);
      return Objects.equals(getFontDescription(), shadowOf(other).getFontDescription());
    }
    return false;
  }

  @Override
  @Implementation
  public int hashCode() {
    return getFontDescription().hashCode();
  }

  @HiddenApi
  @Implementation
  protected static Typeface createFromFamilies(
      @ClassName("[Landroid.graphics.FontFamily;") Object families) {
    return null;
  }

  @HiddenApi
  @Implementation(maxSdk = N_MR1)
  protected static Typeface createFromFamiliesWithDefault(
      @ClassName("[Landroid.graphics.FontFamily;") Object families) {
    return null;
  }

  @Implementation(minSdk = O, maxSdk = O_MR1)
  protected static Typeface createFromFamiliesWithDefault(
      @ClassName("[Landroid.graphics.FontFamily;") Object families, int weight, int italic) {
    return createUnderlyingTypeface("fake-font", Typeface.NORMAL);
  }

  @Implementation(minSdk = P)
  protected static Typeface createFromFamiliesWithDefault(
      @ClassName("[Landroid.graphics.FontFamily;") Object families,
      String fallbackName,
      int weight,
      int italic) {
    return createUnderlyingTypeface(fallbackName, Typeface.NORMAL);
  }

  @Implementation(minSdk = P, maxSdk = P)
  protected static void buildSystemFallback(
      String xmlPath,
      String fontDir,
      ArrayMap<String, Typeface> fontMap,
      ArrayMap<String, /*android.graphics.FontFamily[]*/ ?> fallbackMap) {
    fontMap.put("sans-serif", createUnderlyingTypeface("sans-serif", 0));
  }

  /** Avoid spurious error message about /system/etc/fonts.xml */
  @Implementation(maxSdk = O_MR1)
  protected static void init() {}

  @HiddenApi
  @Implementation(minSdk = Q, maxSdk = R)
  protected static void initSystemDefaultTypefaces(
      Map<String, Typeface> systemFontMap,
      Map<String, /*android.graphics.FontFamily[]*/ ?> fallbacks,
      @ClassName("[Landroid.text.FontConfig$Alias;") Object aliases) {}

  protected static Typeface createUnderlyingTypeface(String familyName, int style) {
    long thisFontId = nextFontId.getAndIncrement();
    Typeface result =
        ReflectionHelpers.callConstructor(
            Typeface.class, ClassParameter.from(long.class, thisFontId));
    ((ShadowLegacyTypeface) Shadow.extract(result)).description = new FontDesc(familyName, style);
    return result;
  }


  @Implementation(minSdk = O, maxSdk = R)
  protected static long nativeCreateFromArray(long[] familyArray, int weight, int italic) {
    // TODO: implement this properly
    return nextFontId.incrementAndGet();
  }

  /**
   * Returns the font description.
   *
   * @return Font description.
   */
  @Override
  public FontDesc getFontDescription() {
    return description;
  }

  @Implementation(minSdk = S)
  protected static void nativeForceSetStaticFinalField(String fieldname, Typeface typeface) {
    ReflectionHelpers.setStaticField(Typeface.class, fieldname, typeface);
  }

  @Implementation(minSdk = S)
  protected static long nativeCreateFromArray(
      long[] familyArray, long fallbackTypeface, int weight, int italic) {
    return ShadowLegacyTypeface.nativeCreateFromArray(familyArray, weight, italic);
  }

  /** Shadow for {@link Typeface.Builder} */
  @Implements(value = Typeface.Builder.class, minSdk = Q)
  public static class ShadowBuilder {
    @RealObject Typeface.Builder realBuilder;

    @Implementation
    protected Typeface build() {
      String path = ReflectionHelpers.getField(realBuilder, "mPath");
      return createUnderlyingTypeface(path, Typeface.NORMAL);
    }
  }

  /** Shadow for {@link Typeface.CustomFallbackBuilder} that populates {@link #description} */
  @Implements(
      value = Typeface.CustomFallbackBuilder.class,
      minSdk = Q,
      shadowPicker = CustomFallbackBuilderPicker.class)
  public static class ShadowCustomFallbackBuilder {
    @RealObject Typeface.CustomFallbackBuilder realBuilder;

    @Implementation
    protected Typeface build() {
      Typeface result = reflector(CustomFallbackBuilderReflector.class, realBuilder).build();
      FontStyle style = reflector(CustomFallbackBuilderReflector.class, realBuilder).getStyle();
      ((ShadowLegacyTypeface) Shadow.extract(result)).description =
          new FontDesc(null, style.getWeight());
      return result;
    }
  }

  /** Shadow picker for {@link Typeface.CustomFallbackBuilder}. */
  public static final class CustomFallbackBuilderPicker extends GraphicsShadowPicker<Object> {
    public CustomFallbackBuilderPicker() {
      super(ShadowLegacyTypeface.ShadowCustomFallbackBuilder.class, null);
    }
  }

  @ForType(Typeface.CustomFallbackBuilder.class)
  interface CustomFallbackBuilderReflector {
    @Direct
    Typeface build();

    @Accessor("mStyle")
    FontStyle getStyle();
  }
}
