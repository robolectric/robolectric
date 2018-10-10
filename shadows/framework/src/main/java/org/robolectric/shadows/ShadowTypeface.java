package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.N_MR1;
import static android.os.Build.VERSION_CODES.P;
import static org.robolectric.RuntimeEnvironment.getApiLevel;

import android.annotation.SuppressLint;
import android.content.res.AssetManager;
import android.graphics.FontFamily;
import android.graphics.Typeface;
import android.util.ArrayMap;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.res.FsFile;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

@Implements(value = Typeface.class, looseSignatures = true)
@SuppressLint("NewApi")
public class ShadowTypeface {
  private static Map<Long, FontDesc> FONTS = new HashMap<>();
  private static long nextFontId = 1;
  private FontDesc description;
  @RealObject private Typeface realTypeface;

  @HiddenApi
  @Implementation
  protected void __constructor__(int fontId) {
    description = findById((long) fontId);
  }

  @HiddenApi
  @Implementation
  protected void __constructor__(long fontId) {
    description = findById(fontId);
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
    Collection<FsFile> assetDirs = shadowAssetManager.getAllAssetDirs();
    for (FsFile assetDir : assetDirs) {
      FsFile[] files = assetDir.listFiles(new StartsWith(path));
      FsFile assetFile = assetDir.join(path);
      if (assetFile.exists() || files.length != 0) {
        return createUnderlyingTypeface(path, Typeface.NORMAL);
      }
    }

    throw new RuntimeException("Font asset not found " + path);
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

  @HiddenApi
  @Implementation(minSdk = LOLLIPOP)
  protected static Typeface createFromFamilies(Object /*FontFamily[]*/ families) {
    return null;
  }

  @HiddenApi
  @Implementation(minSdk = LOLLIPOP, maxSdk = N_MR1)
  protected static Typeface createFromFamiliesWithDefault(Object /*FontFamily[]*/ families) {
    return null;
  }

  @Implementation(minSdk = P)
  protected static void buildSystemFallback(String xmlPath, String fontDir,
      ArrayMap<String, Typeface> fontMap, ArrayMap<String, FontFamily[]> fallbackMap) {
    fontMap.put("sans-serif", createUnderlyingTypeface("sans-serif", 0));
  }

  // BEGIN-INTERNAL
  @HiddenApi
  @Implementation(minSdk = android.os.Build.VERSION_CODES.Q)
  public static void initSystemDefaultTypefaces(Object systemFontMap,
      Object fallbacks,
      Object aliases) {
  }
  // END-INTERNAL

  @Resetter
  synchronized public static void reset() {
    FONTS.clear();
  }

  private static Typeface createUnderlyingTypeface(String familyName, int style) {
    long thisFontId = nextFontId++;
    FONTS.put(thisFontId, new FontDesc(familyName, style));
    if (getApiLevel() >= LOLLIPOP) {
      return ReflectionHelpers.callConstructor(Typeface.class, ClassParameter.from(long.class, thisFontId));
    } else {
      return ReflectionHelpers.callConstructor(Typeface.class, ClassParameter.from(int.class, (int) thisFontId));
    }
  }

  private synchronized static FontDesc findById(long fontId) {
    if (FONTS.containsKey(fontId)) {
      return FONTS.get(fontId);
    }
    throw new RuntimeException("Unknown font id: " + fontId);
  }

  /**
   * Returns the font description.
   *
   * @return Font description.
   */
  public FontDesc getFontDescription() {
    return description;
  }

  public static class FontDesc {
    public final String familyName;
    public final int style;

    public FontDesc(String familyName, int style) {
      this.familyName = familyName;
      this.style = style;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      FontDesc fontDesc = (FontDesc) o;

      if (style != fontDesc.style) return false;
      if (familyName != null ? !familyName.equals(fontDesc.familyName) : fontDesc.familyName != null)
        return false;

      return true;
    }

    @Override
    public int hashCode() {
      int result = familyName != null ? familyName.hashCode() : 0;
      result = 31 * result + style;
      return result;
    }

    public String getFamilyName() {
      return familyName;
    }

    public int getStyle() {
      return style;
    }
  }

  private static class StartsWith implements FsFile.Filter {
    private final String contains;

    public StartsWith(String contains) {
      this.contains = contains;
    }

    @Override
    public boolean accept(FsFile file) {
      return file.getName().startsWith(contains);
    }
  }
}
