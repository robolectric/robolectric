package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;
import static android.os.Build.VERSION_CODES.TIRAMISU;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.fonts.Font;
import android.util.TypedValue;
import com.google.common.base.Ascii;
import com.google.common.base.Preconditions;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.nativeruntime.FontBuilderNatives;
import org.robolectric.nativeruntime.FontNatives;
import org.robolectric.shadows.ShadowNativeFont.Picker;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

/** Shadow for {@link Font} that is backed by native code */
@Implements(value = Font.class, minSdk = P, shadowPicker = Picker.class, isInAndroidSdk = false)
public class ShadowNativeFont {
  @Implementation(minSdk = S)
  protected static long nGetMinikinFontPtr(long font) {
    return FontNatives.nGetMinikinFontPtr(font);
  }

  @Implementation(minSdk = S)
  protected static long nCloneFont(long font) {
    return FontNatives.nCloneFont(font);
  }

  @Implementation(minSdk = S)
  protected static ByteBuffer nNewByteBuffer(long font) {
    return FontNatives.nNewByteBuffer(font);
  }

  @Implementation(minSdk = S)
  protected static long nGetBufferAddress(long font) {
    return FontNatives.nGetBufferAddress(font);
  }

  @Implementation(minSdk = S)
  protected static int nGetSourceId(long font) {
    return FontNatives.nGetSourceId(font);
  }

  @Implementation(minSdk = S)
  protected static long nGetReleaseNativeFont() {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return FontNatives.nGetReleaseNativeFont();
  }

  @Implementation(minSdk = S)
  protected static float nGetGlyphBounds(long font, int glyphId, long paint, RectF rect) {
    return FontNatives.nGetGlyphBounds(font, glyphId, paint, rect);
  }

  @Implementation(minSdk = S)
  protected static float nGetFontMetrics(long font, long paint, Paint.FontMetrics metrics) {
    return FontNatives.nGetFontMetrics(font, paint, metrics);
  }

  @Implementation(minSdk = S)
  protected static String nGetFontPath(long fontPtr) {
    return FontNatives.nGetFontPath(fontPtr);
  }

  @Implementation(minSdk = S)
  protected static String nGetLocaleList(long familyPtr) {
    return FontNatives.nGetLocaleList(familyPtr);
  }

  @Implementation(minSdk = S)
  protected static int nGetPackedStyle(long fontPtr) {
    return FontNatives.nGetPackedStyle(fontPtr);
  }

  @Implementation(minSdk = S)
  protected static int nGetIndex(long fontPtr) {
    return FontNatives.nGetIndex(fontPtr);
  }

  @Implementation(minSdk = S)
  protected static int nGetAxisCount(long fontPtr) {
    return FontNatives.nGetAxisCount(fontPtr);
  }

  @Implementation(minSdk = S)
  protected static long nGetAxisInfo(long fontPtr, int i) {
    return FontNatives.nGetAxisInfo(fontPtr, i);
  }

  @Implementation(minSdk = S)
  protected static long[] nGetAvailableFontSet() {
    return FontNatives.nGetAvailableFontSet();
  }

  /** Shadow for {@link Font.Builder} that is backed by native code */
  @Implements(
      value = Font.Builder.class,
      minSdk = P,
      shadowPicker = ShadowNativeFontBuilder.Picker.class,
      isInAndroidSdk = false)
  public static class ShadowNativeFontBuilder {

    @RealObject Font.Builder realFontBuilder;

    @Implementation(minSdk = Q, maxSdk = Q)
    protected void __constructor__(AssetManager am, String path, boolean isAsset, int cookie) {
      // In Android Q, this method uses native methods that do not exist in later versions, so
      // they need to be re-implemented using logic from S.
      reflector(FontBuilderReflector.class, realFontBuilder).setWeight(-1);
      reflector(FontBuilderReflector.class, realFontBuilder).setItalic(-1);
      reflector(FontBuilderReflector.class, realFontBuilder).setLocaleList("");
      try {
        ByteBuffer buf = createBuffer(am, path, isAsset, cookie);
        reflector(FontBuilderReflector.class, realFontBuilder).setBuffer(buf);
      } catch (IOException e) {
        reflector(FontBuilderReflector.class, realFontBuilder).setException(e);
      }
    }

    @Implementation(minSdk = Q, maxSdk = Q)
    protected void __constructor__(Resources res, int resId) {
      // In Android Q, this method uses native methods that do not exist in later versions, so
      // they need to be re-implemented using logic from S.
      reflector(FontBuilderReflector.class, realFontBuilder).setWeight(-1);
      reflector(FontBuilderReflector.class, realFontBuilder).setItalic(-1);
      reflector(FontBuilderReflector.class, realFontBuilder).setLocaleList("");
      final TypedValue value = new TypedValue();
      res.getValue(resId, value, true);
      if (value.string == null) {
        reflector(FontBuilderReflector.class, realFontBuilder)
            .setException(new FileNotFoundException(resId + " not found"));
        return;
      }
      final String str = value.string.toString();
      if (Ascii.toLowerCase(str).endsWith(".xml")) {
        reflector(FontBuilderReflector.class, realFontBuilder)
            .setException(new FileNotFoundException(resId + " must be font file."));
        return;
      }
      try {
        ByteBuffer buf = createBuffer(res.getAssets(), str, false, value.assetCookie);
        reflector(FontBuilderReflector.class, realFontBuilder).setBuffer(buf);
      } catch (IOException e) {
        reflector(FontBuilderReflector.class, realFontBuilder).setException(e);
      }
    }

    @Implementation(minSdk = Q)
    protected static long nInitBuilder() {
      DefaultNativeRuntimeLoader.injectAndLoad();
      return FontBuilderNatives.nInitBuilder();
    }

    @Implementation(minSdk = Q)
    protected static void nAddAxis(long builderPtr, int tag, float value) {
      FontBuilderNatives.nAddAxis(builderPtr, tag, value);
    }

    @Implementation(minSdk = S)
    protected static long nBuild(
        long builderPtr,
        ByteBuffer buffer,
        String filePath,
        String localeList,
        int weight,
        boolean italic,
        int ttcIndex) {
      return FontBuilderNatives.nBuild(
          builderPtr, buffer, filePath, localeList, weight, italic, ttcIndex);
    }

    @Implementation(minSdk = Q, maxSdk = R)
    protected static long nBuild(
        long builderPtr,
        ByteBuffer buffer,
        String filePath,
        int weight,
        boolean italic,
        int ttcIndex) {
      return nBuild(builderPtr, buffer, filePath, "", weight, italic, ttcIndex);
    }

    @Implementation(minSdk = Q, maxSdk = TIRAMISU)
    protected static long nGetReleaseNativeFont() {
      // Starting in S, nGetReleaseNativeFont was moved from Font.Builder to Font, and despite
      // existing in S, Font.Builder.nGetReleaseNativeFont does not get registered with a native
      // method.
      DefaultNativeRuntimeLoader.injectAndLoad();
      return FontNatives.nGetReleaseNativeFont();
    }

    @Implementation(minSdk = S)
    protected static long nClone(
        long fontPtr, long builderPtr, int weight, boolean italic, int ttcIndex) {
      return FontBuilderNatives.nClone(fontPtr, builderPtr, weight, italic, ttcIndex);
    }

    /**
     * The Android implementation attempts to call {@link java.nio.ByteBuffer#array()} on a direct
     * byte buffer. This is supported in Libcore but not the JVM. Use an implementation that copies
     * the data from the asset into a direct buffer.
     */
    @Implementation(minSdk = R)
    protected static ByteBuffer createBuffer(
        AssetManager am, String path, boolean isAsset, int cookie) throws IOException {
      return assetToBuffer(am, path, isAsset, cookie);
    }

    @ForType(Font.Builder.class)
    interface FontBuilderReflector {
      @Accessor("mBuffer")
      void setBuffer(ByteBuffer buffer);

      @Accessor("mException")
      void setException(IOException e);

      @Accessor("mWeight")
      void setWeight(int weight);

      @Accessor("mItalic")
      void setItalic(int italic);

      @Accessor("mLocaleList")
      void setLocaleList(String localeList);
    }

    /** Shadow picker for {@link Font.Builder}. */
    public static final class Picker extends GraphicsShadowPicker<Object> {
      public Picker() {
        super(ShadowFontBuilder.class, ShadowNativeFontBuilder.class);
      }
    }
  }

  static ByteBuffer assetToBuffer(AssetManager am, String path, boolean isAsset, int cookie)
      throws IOException {
    Preconditions.checkNotNull(am, "assetManager can not be null");
    Preconditions.checkNotNull(path, "path can not be null");
    try (InputStream assetStream =
        isAsset
            ? am.open(path, AssetManager.ACCESS_BUFFER)
            : am.openNonAsset(cookie, path, AssetManager.ACCESS_BUFFER)) {
      int capacity = assetStream.available();
      ByteBuffer buffer = ByteBuffer.allocateDirect(capacity);
      buffer.order(ByteOrder.nativeOrder());
      byte[] buf = new byte[8 * 1024]; // 8k
      int bytesRead;
      while ((bytesRead = assetStream.read(buf)) != -1) {
        buffer.put(buf, 0, bytesRead);
      }
      if (assetStream.read() != -1) {
        throw new IOException("Unable to access full contents of " + path);
      }
      return buffer;
    }
  }

  /** Shadow picker for {@link Font}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(ShadowFont.class, ShadowNativeFont.class);
    }
  }
}
