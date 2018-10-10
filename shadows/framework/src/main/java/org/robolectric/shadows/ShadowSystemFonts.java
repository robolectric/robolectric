// BEGIN-INTERNAL
package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;

import android.graphics.fonts.Font;
import android.graphics.fonts.FontCustomizationParser;
import android.text.FontConfig;
import android.text.FontConfig.Alias;
import android.util.ArrayMap;
import java.util.ArrayList;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(minSdk = Q, isInAndroidSdk = false, className = "android.graphics.fonts.SystemFonts")
public class ShadowSystemFonts {

  @Implementation
  protected static FontConfig.Alias[] buildSystemFallback(String xmlPath,
      String fontDir,
      FontCustomizationParser.Result oemCustomization,
      ArrayMap<String, android.graphics.fonts.FontFamily[]> fallbackMap,
      ArrayList<Font> availableFonts) {
    return new Alias[] {new FontConfig.Alias("sans-serif", "sans-serif", 0)};
  }
}
// END-INTERNAL
