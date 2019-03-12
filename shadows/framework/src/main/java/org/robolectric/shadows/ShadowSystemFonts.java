package org.robolectric.shadows;

import android.graphics.fonts.Font;
import android.graphics.fonts.FontCustomizationParser.Result;
import android.os.Build;
import android.text.FontConfig;
import android.text.FontConfig.Alias;
import android.util.ArrayMap;
import java.util.ArrayList;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(
    className = "android.graphics.fonts.SystemFonts",
    minSdk = Build.VERSION_CODES.Q,
    isInAndroidSdk = false)
public class ShadowSystemFonts {

  @Implementation
  protected static FontConfig.Alias[] buildSystemFallback(String xmlPath, String fontDir,
      Result oemCustomization, ArrayMap<String, android.graphics.fonts.FontFamily[]> fallbackMap,
      ArrayList<Font> availableFonts) {
    return new Alias[] {new FontConfig.Alias("sans-serif", "sans-serif", 0)};
  }
}
