package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.S;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.graphics.fonts.Font;
import android.graphics.fonts.FontCustomizationParser;
import android.graphics.fonts.FontFamily;
import android.graphics.fonts.SystemFonts;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.text.FontConfig;
import android.util.ArrayMap;
import android.util.Log;
import com.google.common.base.Preconditions;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Map;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowNativeSystemFonts.Picker;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

/**
 * Shadow for {@link SystemFonts} for the Robolectric native runtime. It supports getting system
 * font config using a custom fonts path.
 */
@Implements(
    value = SystemFonts.class,
    minSdk = Build.VERSION_CODES.Q,
    isInAndroidSdk = false,
    shadowPicker = Picker.class)
public class ShadowNativeSystemFonts {
  @Implementation(minSdk = S)
  protected static FontConfig getSystemFontConfigInternal(
      String fontsXml,
      String systemFontDir,
      String oemXml,
      String productFontDir,
      Map<String, File> updatableFontMap,
      long lastModifiedDate,
      int configVersion) {
    String fontDir = System.getProperty("robolectric.nativeruntime.fontdir");
    Preconditions.checkNotNull(fontDir);
    Preconditions.checkState(new File(fontDir).isDirectory(), "Missing fonts directory");
    Preconditions.checkState(fontDir.endsWith("/"), "Fonts directory must end with a slash");
    return reflector(SystemFontsReflector.class)
        .getSystemFontConfigInternal(
            fontDir + "fonts.xml",
            fontDir,
            null,
            null,
            updatableFontMap,
            lastModifiedDate,
            configVersion);
  }

  @Implementation(maxSdk = VERSION_CODES.R)
  public static FontConfig.Alias[] buildSystemFallback(
      String xmlPath,
      String systemFontDir,
      FontCustomizationParser.Result oemCustomization,
      ArrayMap<String, FontFamily[]> fallbackMap,
      ArrayList<Font> availableFonts) {
    String fontDir = System.getProperty("robolectric.nativeruntime.fontdir");
    Preconditions.checkNotNull(fontDir);
    Preconditions.checkState(new File(fontDir).isDirectory(), "Missing fonts directory");
    Preconditions.checkState(fontDir.endsWith("/"), "Fonts directory must end with a slash");
    return reflector(SystemFontsReflector.class)
        .buildSystemFallback(
            fontDir + "fonts.xml", fontDir, oemCustomization, fallbackMap, availableFonts);
  }

  @Implementation(minSdk = Q, maxSdk = Q)
  @Nullable
  protected static ByteBuffer mmap(@NonNull String fullPath) {
    try (FileInputStream file = new FileInputStream(fullPath)) {
      final FileChannel fileChannel = file.getChannel();
      final long fontSize = fileChannel.size();
      return fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fontSize);
    } catch (IOException e) {
      Log.w("SystemFonts", e.getMessage());
      return null;
    }
  }

  @ForType(SystemFonts.class)
  interface SystemFontsReflector {
    @Static
    @Direct
    FontConfig getSystemFontConfigInternal(
        String fontsXml,
        String systemFontDir,
        String oemXml,
        String productFontDir,
        Map<String, File> updatableFontMap,
        long lastModifiedDate,
        int configVersion);

    @Static
    @Direct
    FontConfig.Alias[] buildSystemFallback(
        String xmlPath,
        String fontDir,
        FontCustomizationParser.Result oemCustomization,
        ArrayMap<String, FontFamily[]> fallbackMap,
        ArrayList<Font> availableFonts);
  }

  /** Shadow picker for {@link SystemFonts}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(null, ShadowNativeSystemFonts.class);
    }
  }
}
