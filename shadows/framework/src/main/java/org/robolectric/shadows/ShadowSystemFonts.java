package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.graphics.fonts.Font;
import android.graphics.fonts.FontCustomizationParser.Result;
import android.graphics.fonts.FontFamily;
import android.graphics.fonts.SystemFonts;
import android.os.Build;
import android.text.FontConfig;
import android.text.FontConfig.Alias;
import android.util.ArrayMap;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

@Implements(
    className = "android.graphics.fonts.SystemFonts",
    minSdk = Build.VERSION_CODES.Q,
    isInAndroidSdk = false)
public class ShadowSystemFonts {

  @Implementation(maxSdk = R)
  protected static FontConfig.Alias[] buildSystemFallback(
      String xmlPath,
      String fontDir,
      Result oemCustomization,
      ArrayMap<String, android.graphics.fonts.FontFamily[]> fallbackMap,
      ArrayList<Font> availableFonts) {
    return new Alias[] {new FontConfig.Alias("sans-serif", "sans-serif", 0)};
  }

  @Implementation(maxSdk = R)
  protected static FontFamily[] getSystemFallback(String familyName) {
    FontFamily[] result = reflector(SystemFontsReflector.class).getSystemFallback(familyName);
    if (result == null) {
      result = new FontFamily[0];
    }
    return result;
  }

  /** Overrides to prevent the Log.e Failed to open/read system font configurations */
  @Implementation(minSdk = S)
  protected static FontConfig getSystemFontConfigInternal(
      String fontsXml,
      String systemFontDir,
      String oemXml,
      String productFontDir,
      Map<String, File> updatableFontMap,
      long lastModifiedDate,
      int configVersion) {
    return new FontConfig(Collections.emptyList(), Collections.emptyList(), 0, 0);
  }

  @ForType(SystemFonts.class)
  interface SystemFontsReflector {

    @Static
    @Direct
    FontFamily[] getSystemFallback(String familyName);
  }
}
