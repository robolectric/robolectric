package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static org.robolectric.RuntimeEnvironment.getApiLevel;
import static org.robolectric.Shadows.shadowOf;

import android.content.res.AssetManager;
import android.graphics.Typeface;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.FsFile;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

@Implements(value = Typeface.class, looseSignatures = true)
public class ShadowTypeface {
  private static Map<Long, FontDesc> FONTS = new HashMap<>();
  private static long nextFontId = 1;
  private FontDesc description;
  @RealObject private Typeface realTypeface;

  @HiddenApi
  @Implementation
  public void __constructor__(int fontId) {
    description = findById((long) fontId);
  }

  @HiddenApi
  @Implementation
  public void __constructor__(long fontId) {
    description = findById(fontId);
  }

  @Implementation
  public static Typeface create(String familyName, int style) {
    return createUnderlyingTypeface(familyName, style);
  }

  @Implementation
  public static Typeface create(Typeface family, int style) {
    if (family == null) {
      return createUnderlyingTypeface(null, style);
    } else {
      return createUnderlyingTypeface(shadowOf(family).getFontDescription().getFamilyName(), style);
    }
  }

  @Implementation
  public static Typeface createFromAsset(AssetManager mgr, String path) {
    AndroidManifest appManifest = Shadows.shadowOf(RuntimeEnvironment.application).getAppManifest();
    ArrayList<String> paths = new ArrayList<>();
    paths.add(getAssetsPath(appManifest, path));

    List<AndroidManifest> libraryManifests = appManifest.getLibraryManifests();
    for (AndroidManifest libraryManifest : libraryManifests) {
      paths.add(getAssetsPath(libraryManifest, path));
    }

    for (String assetPath : paths) {
      // check if in zip file too?
      FsFile[] files = appManifest.getAssetsDirectory().listFiles(new StartsWith(path));
      if (new File(assetPath).exists() || files.length != 0) {
        return createUnderlyingTypeface(path, Typeface.NORMAL);
      }
    }

    throw new RuntimeException("Font not found at " + paths);
  }

  @Implementation
  public static Typeface createFromFile(File path) {
    String familyName = path.toPath().getFileName().toString();
    return createUnderlyingTypeface(familyName, Typeface.NORMAL);
  }

  @Implementation
  public static Typeface createFromFile(String path) {
    return createFromFile(new File(path));
  }

  @Implementation
  public int getStyle() {
    return description.getStyle();
  }

  @HiddenApi
  @Implementation(minSdk = LOLLIPOP)
  public static Typeface createFromFamilies(Object /*FontFamily[]*/ families) {
    return null;
  }

  @HiddenApi
  @Implementation(minSdk = LOLLIPOP)
  public static Typeface createFromFamiliesWithDefault(Object /*FontFamily[]*/ families) {
    return null;
  }

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

  private static String getAssetsPath(AndroidManifest appManifest, String fontName) {
    return appManifest.getAssetsDirectory().join(fontName).toString();
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
