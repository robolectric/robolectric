package org.robolectric.shadows;

import android.content.res.AssetManager;
import android.graphics.Typeface;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.robolectric.AndroidManifest;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.bytecode.RobolectricInternals;
import org.robolectric.internal.HiddenApi;

import static org.robolectric.Robolectric.shadowOf;

@Implements(Typeface.class)
public class ShadowTypeface {
  private static Map<FontDesc, Integer> fonts = new HashMap<FontDesc, Integer>();
  private static int nextFontId = 1;

  @RealObject private Typeface realTypeface;
  private FontDesc fontDesc;

  @HiddenApi
  public void __constructor__(int fontId) {
    fontDesc = findById(fontId);
    RobolectricInternals.getConstructor(Typeface.class, realTypeface, int.class).invoke(fontId);
  }

  public String getAssetPath() {
    return fontDesc.familyName;
  }

  synchronized public static void reset() {
    // Don't need to reset cache, because native Typeface itself has a cache of font instance,
    // so this class should be consistent with it. 
  }

  @HiddenApi @Implementation
  synchronized public static int nativeCreate(String familyName, int style) {
    FontDesc fontDesc = new FontDesc(familyName, style);
    Integer fontId = fonts.get(fontDesc);
    if (fontId == null) {
      fontId = nextFontId++;
      fonts.put(fontDesc, fontId);
    }
    return fontId;
  }

  @HiddenApi @Implementation
  public static int nativeCreateFromTypeface(int native_instance, int style) {
    FontDesc fontDesc = findById(native_instance);
    return nativeCreate(fontDesc.familyName, style);
  }

  @HiddenApi @Implementation
  public static int nativeGetStyle(int native_instance) {
    return findById(native_instance).style;
  }

  @HiddenApi @Implementation
  public static int nativeCreateFromAsset(AssetManager mgr, String fontName) {
    List<String> paths = new ArrayList<String>();

    AndroidManifest appManifest = shadowOf(Robolectric.application).getAppManifest();
    paths.add(getAssetsPath(appManifest, fontName));

    List<AndroidManifest> libraryManifests = appManifest.getLibraryManifests();
    for (AndroidManifest libraryManifest : libraryManifests) {
      paths.add(getAssetsPath(libraryManifest, fontName));
    }

    return nativeCreateFromFile(paths);
  }

  @HiddenApi @Implementation
  public static int nativeCreateFromFile(List<String> paths) {
    for (String path : paths) {
      File file = new File(path);
      if (file.exists()) {
        return nativeCreate(file.getPath(), 0);
      }
    }

    throw new RuntimeException("Font not found at " + paths);
  }

  private static String getAssetsPath(AndroidManifest appManifest, String fontName) {
    return appManifest.getAssetsDirectory().join(fontName).toString();
  }

  synchronized private static FontDesc findById(int fontId) {
    for (Map.Entry<FontDesc, Integer> entry : fonts.entrySet()) {
      if (entry.getValue().equals(fontId)) {
        return entry.getKey();
      }
    }
    throw new RuntimeException("unknown font id " + fontId);
  }

  private static class FontDesc {
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
  }
}
