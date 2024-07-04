/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.graphics.ColorSpace.Named;
import android.graphics.Paint;
import android.graphics.Picture;
import android.hardware.HardwareBuffer;
import android.os.Parcel;
import android.os.StrictMode;
import android.util.DisplayMetrics;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.versioning.AndroidVersions.U;

@org.robolectric.annotation.Config(minSdk = O)
@RunWith(RobolectricTestRunner.class)
public class ShadowNativeBitmapTest {
  // small alpha values cause color values to be pre-multiplied down, losing accuracy
  private static final int PREMUL_COLOR = Color.argb(2, 255, 254, 253);
  private static final int PREMUL_ROUNDED_COLOR = Color.argb(2, 255, 255, 255);
  private static final int PREMUL_STORED_COLOR = Color.argb(2, 2, 2, 2);

  private static final BitmapFactory.Options HARDWARE_OPTIONS = createHardwareBitmapOptions();

  private Resources res;
  private Bitmap bitmap;
  private BitmapFactory.Options options;

  public static List<ColorSpace> getRgbColorSpaces() {
    List<ColorSpace> rgbColorSpaces = new ArrayList<ColorSpace>();
    for (ColorSpace.Named e : ColorSpace.Named.values()) {
      ColorSpace cs = ColorSpace.get(e);
      if (cs.getModel() != ColorSpace.Model.RGB) {
        continue;
      }
      if (((ColorSpace.Rgb) cs).getTransferParameters() == null) {
        continue;
      }
      rgbColorSpaces.add(cs);
    }
    return rgbColorSpaces;
  }

  @Before
  public void setup() {
    res = RuntimeEnvironment.getApplication().getResources();
    options = new BitmapFactory.Options();
    options.inScaled = false;
    bitmap = BitmapFactory.decodeResource(res, R.drawable.start, options);
  }

  @Test
  public void testCompressRecycled() {
    bitmap.recycle();
    assertThrows(IllegalStateException.class, () -> bitmap.compress(CompressFormat.JPEG, 0, null));
  }

  @Test
  public void testCompressNullStream() {
    assertThrows(NullPointerException.class, () -> bitmap.compress(CompressFormat.JPEG, 0, null));
  }

  @Test
  public void testCompressQualityTooLow() {
    assertThrows(
        IllegalArgumentException.class,
        () -> bitmap.compress(CompressFormat.JPEG, -1, new ByteArrayOutputStream()));
  }

  @Test
  public void testCompressQualityTooHigh() {
    assertThrows(
        IllegalArgumentException.class,
        () -> bitmap.compress(CompressFormat.JPEG, 101, new ByteArrayOutputStream()));
  }

  @Test
  public void testCopyRecycled() {
    bitmap.recycle();
    assertThrows(IllegalStateException.class, () -> bitmap.copy(Config.RGB_565, false));
  }

  @Test
  public void testCopyConfigs() {
    Config[] supportedConfigs =
        new Config[] {
          Config.ALPHA_8, Config.RGB_565, Config.ARGB_8888, Config.RGBA_F16,
        };
    for (Config src : supportedConfigs) {
      for (Config dst : supportedConfigs) {
        Bitmap srcBitmap = Bitmap.createBitmap(1, 1, src);
        srcBitmap.eraseColor(Color.WHITE);
        Bitmap dstBitmap = srcBitmap.copy(dst, false);
        assertNotNull("Should support copying from " + src + " to " + dst, dstBitmap);
        if (Config.ALPHA_8 == dst || Config.ALPHA_8 == src) {
          // Color will be opaque but color information will be lost.
          assertEquals(
              "Color should be black when copying from " + src + " to " + dst,
              Color.BLACK,
              dstBitmap.getPixel(0, 0));
        } else {
          assertEquals(
              "Color should be preserved when copying from " + src + " to " + dst,
              Color.WHITE,
              dstBitmap.getPixel(0, 0));
        }
      }
    }
  }

  @Test
  public void testCopyMutableHwBitmap() {
    bitmap = Bitmap.createBitmap(100, 100, Config.ARGB_8888);
    assertThrows(IllegalArgumentException.class, () -> bitmap.copy(Config.HARDWARE, true));
  }

  @Test
  public void testCopyPixelsToBufferUnsupportedBufferClass() {
    final int pixSize = bitmap.getRowBytes() * bitmap.getHeight();

    assertThrows(
        RuntimeException.class, () -> bitmap.copyPixelsToBuffer(CharBuffer.allocate(pixSize)));
  }

  @Test
  public void testCopyPixelsToBufferBufferTooSmall() {
    final int pixSize = bitmap.getRowBytes() * bitmap.getHeight();
    final int tooSmall = pixSize / 2;

    assertThrows(
        RuntimeException.class, () -> bitmap.copyPixelsToBuffer(ByteBuffer.allocate(tooSmall)));
  }

  @Test
  public void testCopyPixelsToBuffer() {
    final int pixSize = bitmap.getRowBytes() * bitmap.getHeight();

    ByteBuffer byteBuf = ByteBuffer.allocate(pixSize);
    assertEquals(0, byteBuf.position());
    bitmap.copyPixelsToBuffer(byteBuf);
    assertEquals(pixSize, byteBuf.position());

    ShortBuffer shortBuf = ShortBuffer.allocate(pixSize);
    assertEquals(0, shortBuf.position());
    bitmap.copyPixelsToBuffer(shortBuf);
    assertEquals(pixSize >> 1, shortBuf.position());

    IntBuffer intBuf1 = IntBuffer.allocate(pixSize);
    assertEquals(0, intBuf1.position());
    bitmap.copyPixelsToBuffer(intBuf1);
    assertEquals(pixSize >> 2, intBuf1.position());

    bitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
    intBuf1.position(0); // copyPixelsToBuffer adjusted the position, so rewind to start
    bitmap.copyPixelsFromBuffer(intBuf1);
    IntBuffer intBuf2 = IntBuffer.allocate(pixSize);
    bitmap.copyPixelsToBuffer(intBuf2);

    assertEquals(pixSize >> 2, intBuf2.position());
    assertEquals(intBuf1.position(), intBuf2.position());
    int size = intBuf1.position();
    intBuf1.position(0);
    intBuf2.position(0);
    for (int i = 0; i < size; i++) {
      assertEquals("mismatching pixels at position " + i, intBuf1.get(), intBuf2.get());
    }
  }

  @Test
  public void testCreateBitmap1() {
    int[] colors = createColors(100);
    Bitmap bitmap = Bitmap.createBitmap(colors, 10, 10, Config.RGB_565);
    assertFalse(bitmap.isMutable());
    Bitmap ret = Bitmap.createBitmap(bitmap);
    assertNotNull(ret);
    assertFalse(ret.isMutable());
    assertEquals(10, ret.getWidth());
    assertEquals(10, ret.getHeight());
    assertEquals(Config.RGB_565, ret.getConfig());
  }

  @Test
  public void testCreateBitmapNegativeX() {
    assertThrows(
        IllegalArgumentException.class, () -> Bitmap.createBitmap(bitmap, -100, 50, 50, 200));
  }

  @Test
  public void testCreateBitmapNegativeXY() {
    bitmap = Bitmap.createBitmap(100, 100, Config.ARGB_8888);

    // abnormal case: x and/or y less than 0
    assertThrows(
        IllegalArgumentException.class,
        () -> Bitmap.createBitmap(bitmap, -1, -1, 10, 10, null, false));
  }

  @Test
  public void testCreateBitmapNegativeWidthHeight() {
    bitmap = Bitmap.createBitmap(100, 100, Config.ARGB_8888);

    // abnormal case: width and/or height less than 0
    assertThrows(
        IllegalArgumentException.class,
        () -> Bitmap.createBitmap(bitmap, 1, 1, -10, -10, null, false));
  }

  @Test
  public void testCreateBitmapXRegionTooWide() {
    bitmap = Bitmap.createBitmap(100, 100, Config.ARGB_8888);

    // abnormal case: (x + width) bigger than source bitmap's width
    assertThrows(
        IllegalArgumentException.class,
        () -> Bitmap.createBitmap(bitmap, 10, 10, 95, 50, null, false));
  }

  @Test
  public void testCreateBitmapYRegionTooTall() {
    bitmap = Bitmap.createBitmap(100, 100, Config.ARGB_8888);

    // abnormal case: (y + height) bigger than source bitmap's height
    assertThrows(
        IllegalArgumentException.class,
        () -> Bitmap.createBitmap(bitmap, 10, 10, 50, 95, null, false));
  }

  @Test
  public void testCreateMutableBitmapWithHardwareConfig() {
    assertThrows(
        IllegalArgumentException.class, () -> Bitmap.createBitmap(100, 100, Config.HARDWARE));
  }

  @Test
  public void testCreateBitmap4() {
    Bitmap ret = Bitmap.createBitmap(100, 200, Config.RGB_565);
    assertNotNull(ret);
    assertTrue(ret.isMutable());
    assertEquals(100, ret.getWidth());
    assertEquals(200, ret.getHeight());
    assertEquals(Config.RGB_565, ret.getConfig());
  }

  @Test
  public void testCreateBitmapFromColorsNegativeWidthHeight() {
    int[] colors = createColors(100);

    // abnormal case: width and/or height less than 0
    assertThrows(
        IllegalArgumentException.class,
        () -> Bitmap.createBitmap(colors, 0, 100, -1, 100, Config.RGB_565));
  }

  @Test
  public void testCreateBitmapFromColorsIllegalStride() {
    int[] colors = createColors(100);

    // abnormal case: stride less than width and bigger than -width
    assertThrows(
        IllegalArgumentException.class,
        () -> Bitmap.createBitmap(colors, 10, 10, 100, 100, Config.RGB_565));
  }

  @Test
  public void testCreateBitmapFromColorsNegativeOffset() {
    int[] colors = createColors(100);

    // abnormal case: offset less than 0
    assertThrows(
        ArrayIndexOutOfBoundsException.class,
        () -> Bitmap.createBitmap(colors, -10, 100, 100, 100, Config.RGB_565));
  }

  @Test
  public void testCreateBitmapFromColorsOffsetTooLarge() {
    int[] colors = createColors(100);

    // abnormal case: (offset + width) bigger than colors' length
    assertThrows(
        ArrayIndexOutOfBoundsException.class,
        () -> Bitmap.createBitmap(colors, 10, 100, 100, 100, Config.RGB_565));
  }

  @Test
  public void testCreateBitmapFromColorsScalnlineTooLarge() {
    int[] colors = createColors(100);

    // abnormal case: (lastScanline + width) bigger than colors' length
    assertThrows(
        ArrayIndexOutOfBoundsException.class,
        () -> Bitmap.createBitmap(colors, 10, 100, 50, 100, Config.RGB_565));
  }

  @Test
  public void testCreateBitmap6() {
    int[] colors = createColors(100);

    // normal case
    Bitmap ret = Bitmap.createBitmap(colors, 5, 10, 10, 5, Config.RGB_565);
    assertNotNull(ret);
    assertFalse(ret.isMutable());
    assertEquals(10, ret.getWidth());
    assertEquals(5, ret.getHeight());
    assertEquals(Config.RGB_565, ret.getConfig());
  }

  @Test
  public void testCreateBitmap_displayMetrics_mutable() {
    DisplayMetrics metrics = RuntimeEnvironment.getApplication().getResources().getDisplayMetrics();

    Bitmap bitmap;
    bitmap = Bitmap.createBitmap(metrics, 10, 10, Config.ARGB_8888);
    assertTrue(bitmap.isMutable());
    assertEquals(metrics.densityDpi, bitmap.getDensity());

    bitmap = Bitmap.createBitmap(metrics, 10, 10, Config.ARGB_8888);
    assertTrue(bitmap.isMutable());
    assertEquals(metrics.densityDpi, bitmap.getDensity());

    bitmap = Bitmap.createBitmap(metrics, 10, 10, Config.ARGB_8888, true);
    assertTrue(bitmap.isMutable());
    assertEquals(metrics.densityDpi, bitmap.getDensity());

    bitmap =
        Bitmap.createBitmap(
            metrics, 10, 10, Config.ARGB_8888, true, ColorSpace.get(ColorSpace.Named.SRGB));

    assertTrue(bitmap.isMutable());
    assertEquals(metrics.densityDpi, bitmap.getDensity());

    int[] colors = createColors(100);
    bitmap = Bitmap.createBitmap(metrics, colors, 0, 10, 10, 10, Config.ARGB_8888);
    assertNotNull(bitmap);
    assertFalse(bitmap.isMutable());

    bitmap = Bitmap.createBitmap(metrics, colors, 10, 10, Config.ARGB_8888);
    assertNotNull(bitmap);
    assertFalse(bitmap.isMutable());
  }

  @Test
  public void testCreateBitmap_noDisplayMetrics_mutable() {
    Bitmap bitmap;
    bitmap = Bitmap.createBitmap(10, 10, Config.ARGB_8888);
    assertTrue(bitmap.isMutable());

    bitmap = Bitmap.createBitmap(10, 10, Config.ARGB_8888, true);
    assertTrue(bitmap.isMutable());

    bitmap = Bitmap.createBitmap(10, 10, Config.ARGB_8888, true, ColorSpace.get(Named.SRGB));
    assertTrue(bitmap.isMutable());
  }

  @Test
  public void testCreateBitmap_displayMetrics_immutable() {
    DisplayMetrics metrics = RuntimeEnvironment.getApplication().getResources().getDisplayMetrics();
    int[] colors = createColors(100);

    Bitmap bitmap;
    bitmap = Bitmap.createBitmap(metrics, colors, 0, 10, 10, 10, Config.ARGB_8888);
    assertFalse(bitmap.isMutable());
    assertEquals(metrics.densityDpi, bitmap.getDensity());

    bitmap = Bitmap.createBitmap(metrics, colors, 10, 10, Config.ARGB_8888);
    assertFalse(bitmap.isMutable());
    assertEquals(metrics.densityDpi, bitmap.getDensity());
  }

  @Test
  public void testCreateBitmap_noDisplayMetrics_immutable() {
    int[] colors = createColors(100);
    Bitmap bitmap;
    bitmap = Bitmap.createBitmap(colors, 0, 10, 10, 10, Config.ARGB_8888);
    assertFalse(bitmap.isMutable());

    bitmap = Bitmap.createBitmap(colors, 10, 10, Config.ARGB_8888);
    assertFalse(bitmap.isMutable());
  }

  @SuppressWarnings("UnusedVariable")
  @org.robolectric.annotation.Config(minSdk = Q)
  @Test
  public void testWrapHardwareBufferWithInvalidUsageFails() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          try (HardwareBuffer hwBuffer =
              HardwareBuffer.create(
                  512, 512, HardwareBuffer.RGBA_8888, 1, HardwareBuffer.USAGE_CPU_WRITE_RARELY)) {
            Bitmap bitmap = Bitmap.wrapHardwareBuffer(hwBuffer, ColorSpace.get(Named.SRGB));
          }
        });
  }

  @SuppressWarnings("UnusedVariable")
  @org.robolectric.annotation.Config(minSdk = Q)
  @Test
  public void testWrapHardwareBufferWithRgbBufferButNonRgbColorSpaceFails() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          try (HardwareBuffer hwBuffer =
              HardwareBuffer.create(
                  512, 512, HardwareBuffer.RGBA_8888, 1, HardwareBuffer.USAGE_GPU_SAMPLED_IMAGE)) {
            Bitmap bitmap = Bitmap.wrapHardwareBuffer(hwBuffer, ColorSpace.get(Named.CIE_LAB));
          }
        });
  }

  @Test
  public void testGenerationId() {
    Bitmap bitmap = Bitmap.createBitmap(10, 10, Config.ARGB_8888);
    int genId = bitmap.getGenerationId();
    assertEquals("not expected to change", genId, bitmap.getGenerationId());
    bitmap.setDensity(bitmap.getDensity() + 4);
    assertEquals("not expected to change", genId, bitmap.getGenerationId());
    bitmap.getPixel(0, 0);
    assertEquals("not expected to change", genId, bitmap.getGenerationId());

    int beforeGenId = bitmap.getGenerationId();
    bitmap.eraseColor(Color.WHITE);
    int afterGenId = bitmap.getGenerationId();
    assertTrue("expected to increase", afterGenId > beforeGenId);

    beforeGenId = bitmap.getGenerationId();
    bitmap.setPixel(4, 4, Color.BLUE);
    afterGenId = bitmap.getGenerationId();
    assertTrue("expected to increase again", afterGenId > beforeGenId);
  }

  @Test
  public void testDescribeContents() {
    assertEquals(0, bitmap.describeContents());
  }

  @Test
  public void testEraseColorOnRecycled() {
    bitmap.recycle();

    assertThrows(IllegalStateException.class, () -> bitmap.eraseColor(0));
  }

  @org.robolectric.annotation.Config(minSdk = Q)
  @Test
  public void testEraseColorLongOnRecycled() {
    bitmap.recycle();

    assertThrows(IllegalStateException.class, () -> bitmap.eraseColor(Color.pack(0)));
  }

  @Test
  public void testEraseColorOnImmutable() {
    bitmap = BitmapFactory.decodeResource(res, R.drawable.start, options);

    // abnormal case: bitmap is immutable
    assertThrows(IllegalStateException.class, () -> bitmap.eraseColor(0));
  }

  @org.robolectric.annotation.Config(minSdk = Q)
  @Test
  public void testEraseColorLongOnImmutable() {
    bitmap = BitmapFactory.decodeResource(res, R.drawable.start, options);

    // abnormal case: bitmap is immutable
    assertThrows(IllegalStateException.class, () -> bitmap.eraseColor(Color.pack(0)));
  }

  @Test
  public void testEraseColor() {
    // normal case
    bitmap = Bitmap.createBitmap(100, 100, Config.ARGB_8888);
    bitmap.eraseColor(0xffff0000);
    assertEquals(0xffff0000, bitmap.getPixel(10, 10));
    assertEquals(0xffff0000, bitmap.getPixel(50, 50));
  }

  @org.robolectric.annotation.Config(minSdk = Q)
  @Test
  public void testGetColorOOB() {
    bitmap = Bitmap.createBitmap(100, 100, Config.ARGB_8888);
    assertThrows(IllegalArgumentException.class, () -> bitmap.getColor(-1, 0));
  }

  @org.robolectric.annotation.Config(minSdk = Q)
  @Test
  public void testGetColorOOB2() {
    bitmap = Bitmap.createBitmap(100, 100, Config.ARGB_8888);
    assertThrows(IllegalArgumentException.class, () -> bitmap.getColor(5, -10));
  }

  @org.robolectric.annotation.Config(minSdk = Q)
  @Test
  public void testGetColorOOB3() {
    bitmap = Bitmap.createBitmap(100, 100, Config.ARGB_8888);
    assertThrows(IllegalArgumentException.class, () -> bitmap.getColor(100, 10));
  }

  @org.robolectric.annotation.Config(minSdk = Q)
  @Test
  public void testGetColorOOB4() {
    bitmap = Bitmap.createBitmap(100, 100, Config.ARGB_8888);
    assertThrows(IllegalArgumentException.class, () -> bitmap.getColor(99, 1000));
  }

  @Test
  @org.robolectric.annotation.Config(minSdk = Q)
  public void testGetColorRecycled() {
    bitmap = Bitmap.createBitmap(100, 100, Config.ARGB_8888);
    bitmap.recycle();
    assertThrows(IllegalStateException.class, () -> bitmap.getColor(0, 0));
  }

  private static float clamp(float f) {
    return clamp(f, 0.0f, 1.0f);
  }

  private static float clamp(float f, float min, float max) {
    return Math.min(Math.max(f, min), max);
  }

  @org.robolectric.annotation.Config(minSdk = Q)
  @Test
  public void testGetColor() {
    final ColorSpace sRGB = ColorSpace.get(ColorSpace.Named.SRGB);
    List<ColorSpace> rgbColorSpaces = getRgbColorSpaces();
    for (Config config : new Config[] {Config.ARGB_8888, Config.RGBA_F16, Config.RGB_565}) {
      for (ColorSpace bitmapColorSpace : rgbColorSpaces) {
        bitmap = Bitmap.createBitmap(1, 1, config, /*hasAlpha*/ false, bitmapColorSpace);
        bitmapColorSpace = bitmap.getColorSpace();
        for (ColorSpace eraseColorSpace : rgbColorSpaces) {
          for (long wideGamutLong :
              new long[] {
                Color.pack(1.0f, 0.0f, 0.0f, 1.0f, eraseColorSpace),
                Color.pack(0.0f, 1.0f, 0.0f, 1.0f, eraseColorSpace),
                Color.pack(0.0f, 0.0f, 1.0f, 1.0f, eraseColorSpace)
              }) {
            bitmap.eraseColor(wideGamutLong);

            Color result = bitmap.getColor(0, 0);
            if (bitmap.getColorSpace().equals(sRGB)) {
              assertEquals(bitmap.getPixel(0, 0), result.toArgb());
            }
            if (eraseColorSpace.equals(bitmapColorSpace)) {
              final Color wideGamutColor = Color.valueOf(wideGamutLong);
              ColorUtils.verifyColor(
                  "Erasing to Bitmap's ColorSpace " + bitmapColorSpace,
                  wideGamutColor,
                  result,
                  .001f);

            } else {
              Color convertedColor = Color.valueOf(Color.convert(wideGamutLong, bitmapColorSpace));
              if (bitmap.getConfig() != Config.RGBA_F16) {
                // It's possible that we have to clip to fit into the Config.
                convertedColor =
                    Color.valueOf(
                        clamp(convertedColor.red()),
                        clamp(convertedColor.green()),
                        clamp(convertedColor.blue()),
                        convertedColor.alpha(),
                        bitmapColorSpace);
              }
              ColorUtils.verifyColor(
                  "Bitmap(Config: "
                      + bitmap.getConfig()
                      + ", ColorSpace: "
                      + bitmapColorSpace
                      + ") erasing to "
                      + Color.valueOf(wideGamutLong),
                  convertedColor,
                  result,
                  .03f);
            }
          }
        }
      }
    }
  }

  private static class ARGB {
    public final float alpha;
    public final float red;
    public final float green;
    public final float blue;

    ARGB(float alpha, float red, float green, float blue) {
      this.alpha = alpha;
      this.red = red;
      this.green = green;
      this.blue = blue;
    }
  }

  @org.robolectric.annotation.Config(minSdk = Q)
  @Test
  public void testEraseColorLong() {
    List<ColorSpace> rgbColorSpaces = getRgbColorSpaces();
    for (Config config : new Config[] {Config.ARGB_8888, Config.RGB_565, Config.RGBA_F16}) {
      bitmap = Bitmap.createBitmap(100, 100, config);
      // pack SRGB colors into ColorLongs.
      for (int color :
          new int[] {
            Color.RED, Color.BLUE, Color.GREEN, Color.BLACK, Color.WHITE, Color.TRANSPARENT
          }) {
        if (config.equals(Config.RGB_565) && Float.compare(Color.alpha(color), 1.0f) != 0) {
          // 565 doesn't support alpha.
          continue;
        }
        bitmap.eraseColor(Color.pack(color));
        // The Bitmap is either SRGB or SRGBLinear (F16). getPixel(), which retrieves the
        // color in SRGB, should match exactly.
        ColorUtils.verifyColor(
            "Config " + config + " mismatch at 10, 10 ", color, bitmap.getPixel(10, 10), 0);
        ColorUtils.verifyColor(
            "Config " + config + " mismatch at 50, 50 ", color, bitmap.getPixel(50, 50), 0);
      }

      // Use arbitrary colors in various ColorSpaces. getPixel() should approximately match
      // the SRGB version of the color.
      for (ARGB color :
          new ARGB[] {
            new ARGB(1.0f, .5f, .5f, .5f),
            new ARGB(1.0f, .3f, .6f, .9f),
            new ARGB(0.5f, .2f, .8f, .7f)
          }) {
        if (config.equals(Config.RGB_565) && Float.compare(color.alpha, 1.0f) != 0) {
          continue;
        }
        int srgbColor = Color.argb(color.alpha, color.red, color.green, color.blue);
        for (ColorSpace cs : rgbColorSpaces) {
          long longColor = Color.convert(srgbColor, cs);
          bitmap.eraseColor(longColor);
          // These tolerances were chosen by trial and error. It is expected that
          // some conversions do not round-trip perfectly.
          int tolerance = 1;
          if (config.equals(Config.RGB_565)) {
            tolerance = 4;
          } else if (cs.equals(ColorSpace.get(ColorSpace.Named.SMPTE_C))) {
            tolerance = 3;
          }

          ColorUtils.verifyColor(
              "Config " + config + ", ColorSpace " + cs + ", mismatch at 10, 10 ",
              srgbColor,
              bitmap.getPixel(10, 10),
              tolerance);
          ColorUtils.verifyColor(
              "Config " + config + ", ColorSpace " + cs + ", mismatch at 50, 50 ",
              srgbColor,
              bitmap.getPixel(50, 50),
              tolerance);
        }
      }
    }
  }

  @org.robolectric.annotation.Config(minSdk = Q)
  @Test
  public void testEraseColorOnP3() {
    // Use a ColorLong with a different ColorSpace than the Bitmap. getPixel() should
    // approximately match the SRGB version of the color.
    bitmap =
        Bitmap.createBitmap(
            100, 100, Config.ARGB_8888, true, ColorSpace.get(ColorSpace.Named.DISPLAY_P3));
    int srgbColor = Color.argb(.5f, .3f, .6f, .7f);
    long acesColor = Color.convert(srgbColor, ColorSpace.get(ColorSpace.Named.ACES));
    bitmap.eraseColor(acesColor);
    ColorUtils.verifyColor("Mismatch at 15, 15", srgbColor, bitmap.getPixel(15, 15), 1);
  }

  @org.robolectric.annotation.Config(minSdk = Q)
  @Test
  public void testEraseColorXYZ() {
    bitmap = Bitmap.createBitmap(100, 100, Config.ARGB_8888);
    assertThrows(
        IllegalArgumentException.class,
        () ->
            bitmap.eraseColor(Color.convert(Color.BLUE, ColorSpace.get(ColorSpace.Named.CIE_XYZ))));
  }

  @org.robolectric.annotation.Config(minSdk = Q)
  @Test
  public void testEraseColorLAB() {
    bitmap = Bitmap.createBitmap(100, 100, Config.ARGB_8888);
    assertThrows(
        IllegalArgumentException.class,
        () ->
            bitmap.eraseColor(Color.convert(Color.BLUE, ColorSpace.get(ColorSpace.Named.CIE_LAB))));
  }

  @org.robolectric.annotation.Config(minSdk = Q)
  @Test
  public void testEraseColorUnknown() {
    bitmap = Bitmap.createBitmap(100, 100, Config.ARGB_8888);
    assertThrows(IllegalArgumentException.class, () -> bitmap.eraseColor(-1L));
  }

  @Test
  public void testExtractAlphaFromRecycled() {
    bitmap.recycle();

    assertThrows(IllegalStateException.class, () -> bitmap.extractAlpha());
  }

  @Test
  public void testExtractAlpha() {
    // normal case
    bitmap = BitmapFactory.decodeResource(res, R.drawable.start, options);
    Bitmap ret = bitmap.extractAlpha();
    assertNotNull(ret);
    int source = bitmap.getPixel(10, 20);
    int result = ret.getPixel(10, 20);
    assertEquals(Color.alpha(source), Color.alpha(result));
    assertEquals(0xFF, Color.alpha(result));
  }

  @Test
  public void testExtractAlphaWithPaintAndOffsetFromRecycled() {
    bitmap.recycle();

    assertThrows(
        IllegalStateException.class, () -> bitmap.extractAlpha(new Paint(), new int[] {0, 1}));
  }

  @Test
  public void testGetAllocationByteCount() {
    bitmap = Bitmap.createBitmap(100, 200, Bitmap.Config.ALPHA_8);
    int alloc = bitmap.getAllocationByteCount();
    assertEquals(bitmap.getByteCount(), alloc);

    // reconfigure same size
    bitmap.reconfigure(50, 100, Bitmap.Config.ARGB_8888);
    assertEquals(bitmap.getByteCount(), alloc);
    assertEquals(bitmap.getAllocationByteCount(), alloc);

    // reconfigure different size
    bitmap.reconfigure(10, 10, Bitmap.Config.ALPHA_8);
    assertEquals(100, bitmap.getByteCount());
    assertEquals(bitmap.getAllocationByteCount(), alloc);
  }

  @Test
  public void testGetHeight() {
    assertEquals(31, bitmap.getHeight());
    bitmap = Bitmap.createBitmap(100, 200, Bitmap.Config.ARGB_8888);
    assertEquals(200, bitmap.getHeight());
  }

  @Test
  public void testGetNinePatchChunk() {
    assertNull(bitmap.getNinePatchChunk());
  }

  @Test
  public void testGetPixelFromRecycled() {
    bitmap.recycle();

    assertThrows(IllegalStateException.class, () -> bitmap.getPixel(10, 16));
  }

  @Test
  public void testGetPixelXTooLarge() {
    bitmap = Bitmap.createBitmap(100, 200, Bitmap.Config.RGB_565);

    // abnormal case: x bigger than the source bitmap's width
    assertThrows(IllegalArgumentException.class, () -> bitmap.getPixel(200, 16));
  }

  @Test
  public void testGetPixelYTooLarge() {
    bitmap = Bitmap.createBitmap(100, 200, Bitmap.Config.RGB_565);

    // abnormal case: y bigger than the source bitmap's height
    assertThrows(IllegalArgumentException.class, () -> bitmap.getPixel(10, 300));
  }

  @Test
  public void testGetPixel() {
    bitmap = Bitmap.createBitmap(100, 200, Bitmap.Config.RGB_565);

    // normal case 565
    bitmap.setPixel(10, 16, 0xFF << 24);
    assertEquals(0xFF << 24, bitmap.getPixel(10, 16));

    // normal case A_8
    bitmap = Bitmap.createBitmap(10, 10, Config.ALPHA_8);
    bitmap.setPixel(5, 5, 0xFFFFFFFF);
    assertEquals(0xFF000000, bitmap.getPixel(5, 5));
    bitmap.setPixel(5, 5, 0xA8A8A8A8);
    assertEquals(0xA8000000, bitmap.getPixel(5, 5));
    bitmap.setPixel(5, 5, 0x00000000);
    assertEquals(0x00000000, bitmap.getPixel(5, 5));
    bitmap.setPixel(5, 5, 0x1F000000);
    assertEquals(0x1F000000, bitmap.getPixel(5, 5));
  }

  @Test
  public void testGetRowBytes() {
    Bitmap bm0 = Bitmap.createBitmap(100, 200, Bitmap.Config.ALPHA_8);
    Bitmap bm1 = Bitmap.createBitmap(100, 200, Bitmap.Config.ARGB_8888);
    Bitmap bm2 = Bitmap.createBitmap(100, 200, Bitmap.Config.RGB_565);
    Bitmap bm3 = Bitmap.createBitmap(100, 200, Bitmap.Config.ARGB_4444);

    assertEquals(100, bm0.getRowBytes());
    assertEquals(400, bm1.getRowBytes());
    assertEquals(200, bm2.getRowBytes());
    // Attempting to create a 4444 bitmap actually creates an 8888 bitmap.
    assertEquals(400, bm3.getRowBytes());
  }

  @Test
  public void testGetWidth() {
    assertEquals(31, bitmap.getWidth());
    bitmap = Bitmap.createBitmap(100, 200, Bitmap.Config.ARGB_8888);
    assertEquals(100, bitmap.getWidth());
  }

  @Test
  public void testHasAlpha() {
    assertFalse(bitmap.hasAlpha());
    bitmap = Bitmap.createBitmap(100, 200, Bitmap.Config.ARGB_8888);
    assertTrue(bitmap.hasAlpha());
  }

  @Test
  public void testIsMutable() {
    assertFalse(bitmap.isMutable());
    bitmap = Bitmap.createBitmap(100, 100, Config.ARGB_8888);
    assertTrue(bitmap.isMutable());
  }

  @Test
  public void testIsRecycled() {
    assertFalse(bitmap.isRecycled());
    bitmap.recycle();
    assertTrue(bitmap.isRecycled());
  }

  @Test
  public void testReconfigure() {
    bitmap = Bitmap.createBitmap(100, 200, Bitmap.Config.RGB_565);
    int alloc = bitmap.getAllocationByteCount();

    // test shrinking
    bitmap.reconfigure(50, 100, Bitmap.Config.ALPHA_8);
    assertEquals(bitmap.getAllocationByteCount(), alloc);
    assertEquals(bitmap.getByteCount() * 8, alloc);
  }

  @Test
  public void testReconfigureExpanding() {
    bitmap = Bitmap.createBitmap(100, 200, Bitmap.Config.RGB_565);
    assertThrows(
        IllegalArgumentException.class,
        () -> bitmap.reconfigure(101, 201, Bitmap.Config.ARGB_8888));
  }

  @Test
  public void testReconfigureMutable() {
    bitmap = BitmapFactory.decodeResource(res, R.drawable.start, options);
    assertThrows(
        IllegalStateException.class, () -> bitmap.reconfigure(1, 1, Bitmap.Config.ALPHA_8));
  }

  // Used by testAlphaAndPremul.
  private static final Config[] CONFIGS =
      new Config[] {Config.ALPHA_8, Config.ARGB_4444, Config.ARGB_8888, Config.RGB_565};

  // test that reconfigure, setHasAlpha, and setPremultiplied behave as expected with
  // respect to alpha and premultiplied.
  @Test
  public void testAlphaAndPremul() {
    boolean[] falseTrue = new boolean[] {false, true};
    for (Config fromConfig : CONFIGS) {
      for (Config toConfig : CONFIGS) {
        for (boolean hasAlpha : falseTrue) {
          for (boolean isPremul : falseTrue) {
            Bitmap bitmap = Bitmap.createBitmap(10, 10, fromConfig);

            // 4444 is deprecated, and will convert to 8888. No need to
            // attempt a reconfigure, which will be tested when fromConfig
            // is 8888.
            if (fromConfig == Config.ARGB_4444) {
              assertEquals(Config.ARGB_8888, bitmap.getConfig());
              break;
            }

            bitmap.setHasAlpha(hasAlpha);
            bitmap.setPremultiplied(isPremul);

            verifyAlphaAndPremul(bitmap, hasAlpha, isPremul, false);

            // reconfigure to a smaller size so the function will still succeed when
            // going to a Config that requires more bits.
            bitmap.reconfigure(1, 1, toConfig);
            if (toConfig == Config.ARGB_4444) {
              assertEquals(Config.ARGB_8888, bitmap.getConfig());
            } else {
              assertEquals(toConfig, bitmap.getConfig());
            }

            // Check that the alpha and premultiplied state has not changed (unless
            // we expected it to).
            verifyAlphaAndPremul(bitmap, hasAlpha, isPremul, fromConfig == Config.RGB_565);
          }
        }
      }
    }
  }

  /**
   * Assert that bitmap returns the appropriate values for hasAlpha() and isPremultiplied().
   *
   * @param bitmap Bitmap to check.
   * @param expectedAlpha Expected return value from bitmap.hasAlpha(). Note that this is based on
   *     what was set, but may be different from the actual return value depending on the Config and
   *     convertedFrom565.
   * @param expectedPremul Expected return value from bitmap.isPremultiplied(). Similar to
   *     expectedAlpha, this is based on what was set, but may be different from the actual return
   *     value depending on the Config.
   * @param convertedFrom565 Whether bitmap was converted to its current Config by being
   *     reconfigured from RGB_565. If true, and bitmap is now a Config that supports alpha,
   *     hasAlpha() is expected to be true even if expectedAlpha is false.
   */
  @SuppressWarnings("MissingCasesInEnumSwitch")
  private void verifyAlphaAndPremul(
      Bitmap bitmap, boolean expectedAlpha, boolean expectedPremul, boolean convertedFrom565) {
    switch (bitmap.getConfig()) {
      case ARGB_4444:
        // This shouldn't happen, since we don't allow creating or converting
        // to 4444.
        assertFalse(true);
        break;
      case RGB_565:
        assertFalse(bitmap.hasAlpha());
        assertFalse(bitmap.isPremultiplied());
        break;
      case ALPHA_8:
        // ALPHA_8 behaves mostly the same as 8888, except for premultiplied. Fall through.
      case ARGB_8888:
        // Since 565 is necessarily opaque, we revert to hasAlpha when switching to a type
        // that can have alpha.
        if (convertedFrom565) {
          assertTrue(bitmap.hasAlpha());
        } else {
          assertEquals(expectedAlpha, bitmap.hasAlpha());
        }

        if (bitmap.hasAlpha()) {
          // ALPHA_8's premultiplied status is undefined.
          if (bitmap.getConfig() != Config.ALPHA_8) {
            assertEquals(expectedPremul, bitmap.isPremultiplied());
          }
        } else {
          // Opaque bitmap is never considered premultiplied.
          assertFalse(bitmap.isPremultiplied());
        }
        break;
    }
  }

  @org.robolectric.annotation.Config(minSdk = Q)
  @Test
  public void testSetColorSpace() {
    // Use arbitrary colors and assign to various ColorSpaces.
    for (ARGB color :
        new ARGB[] {
          new ARGB(1.0f, .5f, .5f, .5f),
          new ARGB(1.0f, .3f, .6f, .9f),
          new ARGB(0.5f, .2f, .8f, .7f)
        }) {

      int srgbColor = Color.argb(color.alpha, color.red, color.green, color.blue);
      for (ColorSpace cs : getRgbColorSpaces()) {
        for (Config config :
            new Config[] {
              // F16 is tested elsewhere, since it defaults to EXTENDED_SRGB, and
              // many of these calls to setColorSpace would reduce the range, resulting
              // in an Exception.
              Config.ARGB_8888, Config.RGB_565,
            }) {
          bitmap = Bitmap.createBitmap(10, 10, config);
          bitmap.eraseColor(srgbColor);
          bitmap.setColorSpace(cs);
          ColorSpace actual = bitmap.getColorSpace();
          if (Objects.equals(cs, ColorSpace.get(ColorSpace.Named.EXTENDED_SRGB))) {
            assertSame(ColorSpace.get(ColorSpace.Named.SRGB), actual);
          } else if (Objects.equals(cs, ColorSpace.get(ColorSpace.Named.LINEAR_EXTENDED_SRGB))) {
            assertSame(ColorSpace.get(ColorSpace.Named.LINEAR_SRGB), actual);
          } else {
            assertSame(cs, actual);
          }

          // This tolerance was chosen by trial and error. It is expected that
          // some conversions do not round-trip perfectly.
          int tolerance = 2;
          Color c = Color.valueOf(color.red, color.green, color.blue, color.alpha, cs);
          ColorUtils.verifyColor(
              "Mismatch after setting the colorSpace to " + cs.getName(),
              c.convert(bitmap.getColorSpace()),
              bitmap.getColor(5, 5),
              tolerance);
        }
      }
    }
  }

  @org.robolectric.annotation.Config(minSdk = Q)
  @Test
  public void testSetColorSpaceRecycled() {
    bitmap = Bitmap.createBitmap(10, 10, Config.ARGB_8888);
    bitmap.recycle();
    assertThrows(
        IllegalStateException.class, () -> bitmap.setColorSpace(ColorSpace.get(Named.DISPLAY_P3)));
  }

  @org.robolectric.annotation.Config(minSdk = Q)
  @Test
  public void testSetColorSpaceNull() {
    bitmap = Bitmap.createBitmap(10, 10, Config.ARGB_8888);
    assertThrows(IllegalArgumentException.class, () -> bitmap.setColorSpace(null));
  }

  @org.robolectric.annotation.Config(minSdk = Q)
  @Test
  public void testSetColorSpaceXYZ() {
    bitmap = Bitmap.createBitmap(10, 10, Config.ARGB_8888);
    assertThrows(
        IllegalArgumentException.class, () -> bitmap.setColorSpace(ColorSpace.get(Named.CIE_XYZ)));
  }

  @org.robolectric.annotation.Config(minSdk = Q)
  @Test
  public void testSetColorSpaceNoTransferParameters() {
    bitmap = Bitmap.createBitmap(10, 10, Config.ARGB_8888);
    ColorSpace cs =
        new ColorSpace.Rgb(
            "NoTransferParams",
            new float[] {0.640f, 0.330f, 0.300f, 0.600f, 0.150f, 0.060f},
            ColorSpace.ILLUMINANT_D50,
            x -> Math.pow(x, 1.0f / 2.2f),
            x -> Math.pow(x, 2.2f),
            0,
            1);
    assertThrows(IllegalArgumentException.class, () -> bitmap.setColorSpace(cs));
  }

  @org.robolectric.annotation.Config(minSdk = Q)
  @Test
  public void testSetColorSpaceAlpha8() {
    bitmap = Bitmap.createBitmap(10, 10, Config.ALPHA_8);
    assertNull(bitmap.getColorSpace());
    assertThrows(
        IllegalArgumentException.class,
        () -> bitmap.setColorSpace(ColorSpace.get(ColorSpace.Named.SRGB)));
  }

  @org.robolectric.annotation.Config(minSdk = Q)
  @Test
  public void testSetColorSpaceReducedRange() {
    ColorSpace aces = ColorSpace.get(Named.ACES);
    bitmap = Bitmap.createBitmap(10, 10, Config.RGBA_F16, true, aces);
    try {
      bitmap.setColorSpace(ColorSpace.get(Named.SRGB));
      fail("Expected IllegalArgumentException!");
    } catch (IllegalArgumentException e) {
      assertSame(aces, bitmap.getColorSpace());
    }
  }

  @org.robolectric.annotation.Config(minSdk = Q)
  @Test
  public void testSetColorSpaceNotReducedRange() {
    ColorSpace extended = ColorSpace.get(Named.EXTENDED_SRGB);
    bitmap = Bitmap.createBitmap(10, 10, Config.RGBA_F16, true, extended);
    bitmap.setColorSpace(ColorSpace.get(Named.SRGB));
    assertSame(bitmap.getColorSpace(), extended);
  }

  @org.robolectric.annotation.Config(minSdk = Q)
  @Test
  public void testSetColorSpaceNotReducedRangeLinear() {
    ColorSpace linearExtended = ColorSpace.get(Named.LINEAR_EXTENDED_SRGB);
    bitmap = Bitmap.createBitmap(10, 10, Config.RGBA_F16, true, linearExtended);
    bitmap.setColorSpace(ColorSpace.get(Named.LINEAR_SRGB));
    assertSame(bitmap.getColorSpace(), linearExtended);
  }

  @org.robolectric.annotation.Config(minSdk = Q)
  @Test
  public void testSetColorSpaceIncreasedRange() {
    bitmap = Bitmap.createBitmap(10, 10, Config.RGBA_F16, true, ColorSpace.get(Named.DISPLAY_P3));
    ColorSpace linearExtended = ColorSpace.get(Named.LINEAR_EXTENDED_SRGB);
    bitmap.setColorSpace(linearExtended);
    assertSame(bitmap.getColorSpace(), linearExtended);
  }

  @Test
  public void testSetConfig() {
    bitmap = Bitmap.createBitmap(100, 200, Bitmap.Config.RGB_565);
    int alloc = bitmap.getAllocationByteCount();

    // test shrinking
    bitmap.setConfig(Bitmap.Config.ALPHA_8);
    assertEquals(bitmap.getAllocationByteCount(), alloc);
    assertEquals(bitmap.getByteCount() * 2, alloc);
  }

  @Test
  public void testSetConfigExpanding() {
    bitmap = Bitmap.createBitmap(100, 200, Bitmap.Config.RGB_565);
    // test expanding
    assertThrows(IllegalArgumentException.class, () -> bitmap.setConfig(Bitmap.Config.ARGB_8888));
  }

  @Test
  public void testSetConfigMutable() {
    // test mutable
    bitmap = BitmapFactory.decodeResource(res, R.drawable.start, options);
    assertThrows(IllegalStateException.class, () -> bitmap.setConfig(Bitmap.Config.ALPHA_8));
  }

  @Test
  public void testSetHeight() {
    bitmap = Bitmap.createBitmap(100, 200, Bitmap.Config.ARGB_8888);
    int alloc = bitmap.getAllocationByteCount();

    // test shrinking
    bitmap.setHeight(100);
    assertEquals(bitmap.getAllocationByteCount(), alloc);
    assertEquals(bitmap.getByteCount() * 2, alloc);
  }

  @Test
  public void testSetHeightExpanding() {
    // test expanding
    bitmap = Bitmap.createBitmap(100, 200, Bitmap.Config.ARGB_8888);
    assertThrows(IllegalArgumentException.class, () -> bitmap.setHeight(201));
  }

  @Test
  public void testSetHeightMutable() {
    // test mutable
    bitmap = BitmapFactory.decodeResource(res, R.drawable.start, options);
    assertThrows(IllegalStateException.class, () -> bitmap.setHeight(1));
  }

  @Test
  public void testSetPixelOnRecycled() {
    int color = 0xff << 24;

    bitmap.recycle();
    assertThrows(IllegalStateException.class, () -> bitmap.setPixel(10, 16, color));
  }

  @Test
  public void testSetPixelOnImmutable() {
    int color = 0xff << 24;
    bitmap = BitmapFactory.decodeResource(res, R.drawable.start, options);

    assertThrows(IllegalStateException.class, () -> bitmap.setPixel(10, 16, color));
  }

  @Test
  public void testSetPixelXIsTooLarge() {
    int color = 0xff << 24;
    bitmap = Bitmap.createBitmap(100, 200, Bitmap.Config.RGB_565);

    // abnormal case: x bigger than the source bitmap's width
    assertThrows(IllegalArgumentException.class, () -> bitmap.setPixel(200, 16, color));
  }

  @Test
  public void testSetPixelYIsTooLarge() {
    int color = 0xff << 24;
    bitmap = Bitmap.createBitmap(100, 200, Bitmap.Config.RGB_565);

    // abnormal case: y bigger than the source bitmap's height
    assertThrows(IllegalArgumentException.class, () -> bitmap.setPixel(10, 300, color));
  }

  @Test
  public void testSetPixel() {
    int color = 0xff << 24;
    bitmap = Bitmap.createBitmap(100, 200, Bitmap.Config.RGB_565);

    // normal case
    bitmap.setPixel(10, 16, color);
    assertEquals(color, bitmap.getPixel(10, 16));
  }

  @Test
  public void testSetPixelsOnRecycled() {
    int[] colors = createColors(100);

    bitmap.recycle();
    assertThrows(IllegalStateException.class, () -> bitmap.setPixels(colors, 0, 0, 0, 0, 0, 0));
  }

  @Test
  public void testSetPixelsOnImmutable() {
    int[] colors = createColors(100);
    bitmap = BitmapFactory.decodeResource(res, R.drawable.start, options);

    assertThrows(IllegalStateException.class, () -> bitmap.setPixels(colors, 0, 0, 0, 0, 0, 0));
  }

  @Test
  public void testSetPixelsXYNegative() {
    int[] colors = createColors(100);
    bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);

    // abnormal case: x and/or y less than 0
    assertThrows(
        IllegalArgumentException.class, () -> bitmap.setPixels(colors, 0, 0, -1, -1, 200, 16));
  }

  @Test
  public void testSetPixelsWidthHeightNegative() {
    int[] colors = createColors(100);
    bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);

    // abnormal case: width and/or height less than 0
    assertThrows(
        IllegalArgumentException.class, () -> bitmap.setPixels(colors, 0, 0, 0, 0, -1, -1));
  }

  @Test
  public void testSetPixelsXTooHigh() {
    int[] colors = createColors(100);
    bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);

    // abnormal case: (x + width) bigger than the source bitmap's width
    assertThrows(
        IllegalArgumentException.class, () -> bitmap.setPixels(colors, 0, 0, 10, 10, 95, 50));
  }

  @Test
  public void testSetPixelsYTooHigh() {
    int[] colors = createColors(100);
    bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);

    // abnormal case: (y + height) bigger than the source bitmap's height
    assertThrows(
        IllegalArgumentException.class, () -> bitmap.setPixels(colors, 0, 0, 10, 10, 50, 95));
  }

  @Test
  public void testSetPixelsStrideIllegal() {
    int[] colors = createColors(100);
    bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);

    // abnormal case: stride less than width and bigger than -width
    assertThrows(
        IllegalArgumentException.class, () -> bitmap.setPixels(colors, 0, 10, 10, 10, 50, 50));
  }

  @Test
  public void testSetPixelsOffsetNegative() {
    int[] colors = createColors(100);
    bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);

    // abnormal case: offset less than 0
    assertThrows(
        ArrayIndexOutOfBoundsException.class,
        () -> bitmap.setPixels(colors, -1, 50, 10, 10, 50, 50));
  }

  @Test
  public void testSetPixelsOffsetTooBig() {
    int[] colors = createColors(100);
    bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);

    // abnormal case: (offset + width) bigger than the length of colors
    assertThrows(
        ArrayIndexOutOfBoundsException.class,
        () -> bitmap.setPixels(colors, 60, 50, 10, 10, 50, 50));
  }

  @Test
  public void testSetPixelsLastScanlineNegative() {
    int[] colors = createColors(100);
    bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);

    // abnormal case: lastScanline less than 0
    assertThrows(
        ArrayIndexOutOfBoundsException.class,
        () -> bitmap.setPixels(colors, 10, -50, 10, 10, 50, 50));
  }

  @Test
  public void testSetPixelsLastScanlineTooBig() {
    int[] colors = createColors(100);
    bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);

    // abnormal case: (lastScanline + width) bigger than the length of colors
    assertThrows(
        ArrayIndexOutOfBoundsException.class,
        () -> bitmap.setPixels(colors, 10, 50, 10, 10, 50, 50));
  }

  @Test
  public void testSetPixels() {
    int[] colors = createColors(100 * 100);
    bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    bitmap.setPixels(colors, 0, 100, 0, 0, 100, 100);
    int[] ret = new int[100 * 100];
    bitmap.getPixels(ret, 0, 100, 0, 0, 100, 100);

    for (int i = 0; i < 10000; i++) {
      assertEquals(ret[i], colors[i]);
    }
  }

  private void verifyPremultipliedBitmapConfig(Config config, boolean expectedPremul) {
    Bitmap bitmap = Bitmap.createBitmap(1, 1, config);
    bitmap.setPremultiplied(true);
    bitmap.setPixel(0, 0, Color.TRANSPARENT);
    assertTrue(bitmap.isPremultiplied() == expectedPremul);

    bitmap.setHasAlpha(false);
    assertFalse(bitmap.isPremultiplied());
  }

  @Test
  public void testSetPremultipliedSimple() {
    verifyPremultipliedBitmapConfig(Bitmap.Config.ALPHA_8, true);
    verifyPremultipliedBitmapConfig(Bitmap.Config.RGB_565, false);
    verifyPremultipliedBitmapConfig(Bitmap.Config.ARGB_4444, true);
    verifyPremultipliedBitmapConfig(Bitmap.Config.ARGB_8888, true);
  }

  @Test
  public void testSetPremultipliedData() {
    // with premul, will store 2,2,2,2, so it doesn't get value correct
    Bitmap bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
    bitmap.setPixel(0, 0, PREMUL_COLOR);
    assertEquals(bitmap.getPixel(0, 0), PREMUL_ROUNDED_COLOR);

    // read premultiplied value directly
    bitmap.setPremultiplied(false);
    assertEquals(bitmap.getPixel(0, 0), PREMUL_STORED_COLOR);

    // value can now be stored/read correctly
    bitmap.setPixel(0, 0, PREMUL_COLOR);
    assertEquals(bitmap.getPixel(0, 0), PREMUL_COLOR);

    // verify with array methods
    int[] testArray = new int[] {PREMUL_COLOR};
    bitmap.setPixels(testArray, 0, 1, 0, 0, 1, 1);
    bitmap.getPixels(testArray, 0, 1, 0, 0, 1, 1);
    assertEquals(bitmap.getPixel(0, 0), PREMUL_COLOR);
  }

  @Test
  public void testPremultipliedCanvas() {
    Bitmap bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
    bitmap.setHasAlpha(true);
    bitmap.setPremultiplied(false);
    assertFalse(bitmap.isPremultiplied());

    Canvas c = new Canvas();
    assertThrows(RuntimeException.class, () -> c.drawBitmap(bitmap, 0, 0, null));
  }

  @Test
  public void testSetWidth() {
    bitmap = Bitmap.createBitmap(100, 200, Bitmap.Config.ARGB_8888);
    int alloc = bitmap.getAllocationByteCount();

    // test shrinking
    bitmap.setWidth(50);
    assertEquals(bitmap.getAllocationByteCount(), alloc);
    assertEquals(bitmap.getByteCount() * 2, alloc);
  }

  @Test
  public void testSetWidthExpanding() {
    // test expanding
    bitmap = Bitmap.createBitmap(100, 200, Bitmap.Config.ARGB_8888);

    assertThrows(IllegalArgumentException.class, () -> bitmap.setWidth(101));
  }

  @Test
  public void testSetWidthMutable() {
    // test mutable
    bitmap = BitmapFactory.decodeResource(res, R.drawable.start, options);

    assertThrows(IllegalStateException.class, () -> bitmap.setWidth(1));
  }

  @Test
  public void testWriteToParcelRecycled() {
    bitmap.recycle();

    assertThrows(IllegalStateException.class, () -> bitmap.writeToParcel(null, 0));
  }

  @Test
  public void testGetScaledHeight1() {
    int dummyDensity = 5;
    Bitmap ret = Bitmap.createBitmap(100, 200, Config.RGB_565);
    int scaledHeight = scaleFromDensity(ret.getHeight(), ret.getDensity(), dummyDensity);
    assertNotNull(ret);
    assertEquals(scaledHeight, ret.getScaledHeight(dummyDensity));
  }

  @Test
  public void testGetScaledHeight2() {
    Bitmap ret = Bitmap.createBitmap(100, 200, Config.RGB_565);
    DisplayMetrics metrics = RuntimeEnvironment.getApplication().getResources().getDisplayMetrics();
    int scaledHeight = scaleFromDensity(ret.getHeight(), ret.getDensity(), metrics.densityDpi);
    assertEquals(scaledHeight, ret.getScaledHeight(metrics));
  }

  @Test
  public void testGetScaledHeight3() {
    Bitmap ret = Bitmap.createBitmap(100, 200, Config.RGB_565);
    Bitmap mMutableBitmap = Bitmap.createBitmap(100, 200, Config.ARGB_8888);
    Canvas mCanvas = new Canvas(mMutableBitmap);
    // set Density
    mCanvas.setDensity(DisplayMetrics.DENSITY_HIGH);
    int scaledHeight = scaleFromDensity(ret.getHeight(), ret.getDensity(), mCanvas.getDensity());
    assertEquals(scaledHeight, ret.getScaledHeight(mCanvas));
  }

  @Test
  public void testGetScaledWidth1() {
    int dummyDensity = 5;
    Bitmap ret = Bitmap.createBitmap(100, 200, Config.RGB_565);
    int scaledWidth = scaleFromDensity(ret.getWidth(), ret.getDensity(), dummyDensity);
    assertNotNull(ret);
    assertEquals(scaledWidth, ret.getScaledWidth(dummyDensity));
  }

  @Test
  public void testGetScaledWidth2() {
    Bitmap ret = Bitmap.createBitmap(100, 200, Config.RGB_565);
    DisplayMetrics metrics = RuntimeEnvironment.getApplication().getResources().getDisplayMetrics();
    int scaledWidth = scaleFromDensity(ret.getWidth(), ret.getDensity(), metrics.densityDpi);
    assertEquals(scaledWidth, ret.getScaledWidth(metrics));
  }

  @Test
  public void testGetScaledWidth3() {
    Bitmap ret = Bitmap.createBitmap(100, 200, Config.RGB_565);
    Bitmap mMutableBitmap = Bitmap.createBitmap(100, 200, Config.ARGB_8888);
    Canvas mCanvas = new Canvas(mMutableBitmap);
    // set Density
    mCanvas.setDensity(DisplayMetrics.DENSITY_HIGH);
    int scaledWidth = scaleFromDensity(ret.getWidth(), ret.getDensity(), mCanvas.getDensity());
    assertEquals(scaledWidth, ret.getScaledWidth(mCanvas));
  }

  @Test
  public void testSameAs_simpleSuccess() {
    Bitmap bitmap1 = Bitmap.createBitmap(100, 100, Config.ARGB_8888);
    Bitmap bitmap2 = Bitmap.createBitmap(100, 100, Config.ARGB_8888);
    bitmap1.eraseColor(Color.BLACK);
    bitmap2.eraseColor(Color.BLACK);
    assertTrue(bitmap1.sameAs(bitmap2));
    assertTrue(bitmap2.sameAs(bitmap1));
  }

  @Test
  public void testSameAs_simpleFail() {
    Bitmap bitmap1 = Bitmap.createBitmap(100, 100, Config.ARGB_8888);
    Bitmap bitmap2 = Bitmap.createBitmap(100, 100, Config.ARGB_8888);
    bitmap1.eraseColor(Color.BLACK);
    bitmap2.eraseColor(Color.BLACK);
    bitmap2.setPixel(20, 10, Color.WHITE);
    assertFalse(bitmap1.sameAs(bitmap2));
    assertFalse(bitmap2.sameAs(bitmap1));
  }

  @Test
  public void testSameAs_reconfigure() {
    Bitmap bitmap1 = Bitmap.createBitmap(100, 100, Config.ARGB_8888);
    Bitmap bitmap2 = Bitmap.createBitmap(150, 150, Config.ARGB_8888);
    bitmap2.reconfigure(100, 100, Config.ARGB_8888); // now same size, so should be same
    bitmap1.eraseColor(Color.BLACK);
    bitmap2.eraseColor(Color.BLACK);
    assertTrue(bitmap1.sameAs(bitmap2));
    assertTrue(bitmap2.sameAs(bitmap1));
  }

  @Test
  public void testSameAs_config() {
    Bitmap bitmap1 = Bitmap.createBitmap(100, 200, Config.RGB_565);
    Bitmap bitmap2 = Bitmap.createBitmap(100, 200, Config.ARGB_8888);

    // both bitmaps can represent black perfectly
    bitmap1.eraseColor(Color.BLACK);
    bitmap2.eraseColor(Color.BLACK);

    // but not same due to config
    assertFalse(bitmap1.sameAs(bitmap2));
    assertFalse(bitmap2.sameAs(bitmap1));
  }

  @Test
  public void testSameAs_width() {
    Bitmap bitmap1 = Bitmap.createBitmap(100, 100, Config.ARGB_8888);
    Bitmap bitmap2 = Bitmap.createBitmap(101, 100, Config.ARGB_8888);
    bitmap1.eraseColor(Color.BLACK);
    bitmap2.eraseColor(Color.BLACK);
    assertFalse(bitmap1.sameAs(bitmap2));
    assertFalse(bitmap2.sameAs(bitmap1));
  }

  @Test
  public void testSameAs_height() {
    Bitmap bitmap1 = Bitmap.createBitmap(100, 100, Config.ARGB_8888);
    Bitmap bitmap2 = Bitmap.createBitmap(102, 100, Config.ARGB_8888);
    bitmap1.eraseColor(Color.BLACK);
    bitmap2.eraseColor(Color.BLACK);
    assertFalse(bitmap1.sameAs(bitmap2));
    assertFalse(bitmap2.sameAs(bitmap1));
  }

  @Test
  public void testSameAs_opaque() {
    Bitmap bitmap1 = Bitmap.createBitmap(100, 100, Config.ARGB_8888);
    Bitmap bitmap2 = Bitmap.createBitmap(100, 100, Config.ARGB_8888);
    bitmap1.eraseColor(Color.BLACK);
    bitmap2.eraseColor(Color.BLACK);
    bitmap1.setHasAlpha(true);
    bitmap2.setHasAlpha(false);
    assertFalse(bitmap1.sameAs(bitmap2));
    assertFalse(bitmap2.sameAs(bitmap1));
  }

  @org.robolectric.annotation.Config(maxSdk = U.SDK_INT) // TODO(hoisie): fix in V and above
  @Test
  public void testSameAs_hardware() {
    Bitmap bitmap1 = BitmapFactory.decodeResource(res, R.drawable.robot, HARDWARE_OPTIONS);
    Bitmap bitmap2 = BitmapFactory.decodeResource(res, R.drawable.robot, HARDWARE_OPTIONS);
    Bitmap bitmap3 = BitmapFactory.decodeResource(res, R.drawable.robot);
    Bitmap bitmap4 = BitmapFactory.decodeResource(res, R.drawable.start, HARDWARE_OPTIONS);
    assertTrue(bitmap1.sameAs(bitmap2));
    assertTrue(bitmap2.sameAs(bitmap1));

    // Note: on an emulator or real device, the HARDWARE bitmap1 and the Software bitmap3 differ
    // because the pixels cannot be read from the underlying hardware buffer. However, after the fix
    // from r.android.com/2887086, Robolectric does actually properly fill the buffer content of a
    // HARDWARE bitmap, which means it now is the same as its non-hardware counterpart.
    assertTrue(bitmap1.sameAs(bitmap3));

    assertFalse(bitmap1.sameAs(bitmap4));
  }

  @Test
  public void testHardwareSetWidth() {
    Bitmap bitmap = BitmapFactory.decodeResource(res, R.drawable.robot, HARDWARE_OPTIONS);
    assertThrows(IllegalStateException.class, () -> bitmap.setWidth(30));
  }

  @Test
  public void testHardwareSetHeight() {
    Bitmap bitmap = BitmapFactory.decodeResource(res, R.drawable.robot, HARDWARE_OPTIONS);
    assertThrows(IllegalStateException.class, () -> bitmap.setHeight(30));
  }

  @Test
  public void testHardwareSetConfig() {
    Bitmap bitmap = BitmapFactory.decodeResource(res, R.drawable.robot, HARDWARE_OPTIONS);
    assertThrows(IllegalStateException.class, () -> bitmap.setConfig(Config.ARGB_8888));
  }

  @Test
  public void testHardwareReconfigure() {
    Bitmap bitmap = BitmapFactory.decodeResource(res, R.drawable.robot, HARDWARE_OPTIONS);
    assertThrows(IllegalStateException.class, () -> bitmap.reconfigure(30, 30, Config.ARGB_8888));
  }

  @Test
  public void testHardwareSetPixels() {
    Bitmap bitmap = BitmapFactory.decodeResource(res, R.drawable.robot, HARDWARE_OPTIONS);
    assertThrows(
        IllegalStateException.class, () -> bitmap.setPixels(new int[10], 0, 1, 0, 0, 1, 1));
  }

  @Test
  public void testHardwareSetPixel() {
    Bitmap bitmap = BitmapFactory.decodeResource(res, R.drawable.robot, HARDWARE_OPTIONS);
    assertThrows(IllegalStateException.class, () -> bitmap.setPixel(1, 1, 0));
  }

  @Test
  public void testHardwareEraseColor() {
    Bitmap bitmap = BitmapFactory.decodeResource(res, R.drawable.robot, HARDWARE_OPTIONS);
    assertThrows(IllegalStateException.class, () -> bitmap.eraseColor(0));
  }

  @org.robolectric.annotation.Config(minSdk = Q)
  @Test
  public void testHardwareEraseColorLong() {
    Bitmap bitmap = BitmapFactory.decodeResource(res, R.drawable.robot, HARDWARE_OPTIONS);
    assertThrows(IllegalStateException.class, () -> bitmap.eraseColor(Color.pack(0)));
  }

  @Test
  public void testUseMetadataAfterRecycle() {
    Bitmap bitmap = Bitmap.createBitmap(10, 20, Config.RGB_565);
    bitmap.recycle();
    assertEquals(10, bitmap.getWidth());
    assertEquals(20, bitmap.getHeight());
    assertEquals(Config.RGB_565, bitmap.getConfig());
  }

  @Test
  @Ignore("TODO(b/hoisie): re-enable when HW bitmaps are better supported")
  public void testCreateScaledFromHWInStrictMode() {
    strictModeTest(
        () -> {
          Bitmap bitmap = Bitmap.createBitmap(100, 100, Config.ARGB_8888);
          Bitmap hwBitmap = bitmap.copy(Config.HARDWARE, false);
          Bitmap.createScaledBitmap(hwBitmap, 200, 200, false);
        });
  }

  @Test
  public void testCompressInStrictMode() {
    strictModeTest(
        () -> {
          Bitmap bitmap = Bitmap.createBitmap(100, 100, Config.ARGB_8888);
          bitmap.compress(CompressFormat.JPEG, 90, new ByteArrayOutputStream());
        });
  }

  @Test
  public void legacyShadowAPIs_throwException() {
    Bitmap bitmap = Bitmap.createBitmap(100, 100, Config.ARGB_8888);
    ShadowBitmap shadowBitmap = Shadow.extract(bitmap);
    assertThrows(UnsupportedOperationException.class, () -> shadowBitmap.setDescription("hello"));
  }

  @Ignore("TODO(b/hoisie): re-enable when HW bitmaps are better supported")
  @Test
  public void testParcelHWInStrictMode() {
    strictModeTest(
        () -> {
          bitmap = Bitmap.createBitmap(100, 100, Config.ARGB_8888);
          Bitmap hwBitmap = bitmap.copy(Config.HARDWARE, false);
          hwBitmap.writeToParcel(Parcel.obtain(), 0);
        });
  }

  @Test
  public void getCreatedFromResId() {
    assertThat(((ShadowNativeBitmap) Shadow.extract(bitmap)).getCreatedFromResId())
        .isEqualTo(R.drawable.start);
  }

  @Test
  public void testWriteToParcel() {
    Parcel p = Parcel.obtain();
    bitmap = Bitmap.createBitmap(100, 100, Config.ARGB_8888);
    bitmap.eraseColor(Color.GREEN);
    bitmap.writeToParcel(p, 0);
    p.setDataPosition(0);
    Bitmap fromParcel = Bitmap.CREATOR.createFromParcel(p);
    assertTrue(bitmap.sameAs(fromParcel));
    assertThat(fromParcel.isMutable()).isTrue();
    p.recycle();
  }

  @Test
  public void testWriteImmutableToParcel() {
    Parcel p = Parcel.obtain();
    bitmap = Bitmap.createBitmap(100, 100, Config.ARGB_8888);
    bitmap.eraseColor(Color.GREEN);
    Bitmap immutable = bitmap.copy(Config.ARGB_8888, /* isMutable= */ false);
    assertThat(immutable.isMutable()).isFalse();
    immutable.writeToParcel(p, 0);
    p.setDataPosition(0);
    Bitmap fromParcel = Bitmap.CREATOR.createFromParcel(p);
    assertTrue(immutable.sameAs(fromParcel));
    assertThat(fromParcel.isMutable()).isFalse();
    p.recycle();
  }

  @Test
  public void createBitmap_colorSpace_customColorSpace() {
    Bitmap bitmap =
        Bitmap.createBitmap(
            100, 100, Bitmap.Config.ARGB_8888, true, ColorSpace.get(ColorSpace.Named.ADOBE_RGB));

    assertThat(bitmap.getColorSpace()).isEqualTo(ColorSpace.get(ColorSpace.Named.ADOBE_RGB));
  }

  @org.robolectric.annotation.Config(minSdk = U.SDK_INT)
  @Test
  public void noGainmap_returnsNull() {
    Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    assertThat(bitmap.getGainmap()).isNull();
  }

  @Test
  public void compress_thenDecodeStream_sameAs() {
    Bitmap bitmap = Bitmap.createBitmap(/* width= */ 10, /* height= */ 10, Bitmap.Config.ARGB_8888);
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    bitmap.compress(CompressFormat.PNG, /* quality= */ 100, outStream);
    byte[] outBytes = outStream.toByteArray();
    ByteArrayInputStream inStream = new ByteArrayInputStream(outBytes);
    BitmapFactory.Options options = new Options();
    Bitmap bitmap2 = BitmapFactory.decodeStream(inStream, null, options);
    assertThat(bitmap.sameAs(bitmap2)).isTrue();
  }

  @Test
  public void parcelRoundTripWithoutColorSpace_isSuccessful() {
    // Importantly, ALPHA_8 doesn't have an associated color space.
    Bitmap orig = Bitmap.createBitmap(/* width= */ 314, /* height= */ 159, Bitmap.Config.ALPHA_8);

    Parcel parcel = Parcel.obtain();
    parcel.writeParcelable(orig, /* parcelableFlags= */ 0);
    parcel.setDataPosition(0);
    Bitmap copy = parcel.readParcelable(Bitmap.class.getClassLoader());

    assertThat(copy.sameAs(orig)).isTrue();
  }

  @org.robolectric.annotation.Config(minSdk = P)
  @Test
  public void testCreateBitmap_picture_immutable() {
    Picture picture = new Picture();
    Canvas canvas = picture.beginRecording(200, 100);

    Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

    p.setColor(0x88FF0000);
    canvas.drawCircle(50, 50, 40, p);

    p.setColor(Color.GREEN);
    p.setTextSize(30);
    canvas.drawText("Pictures", 60, 60, p);
    picture.endRecording();

    Bitmap bitmap;
    bitmap = Bitmap.createBitmap(picture);
    assertFalse(bitmap.isMutable());

    bitmap = Bitmap.createBitmap(picture, 100, 100, Config.HARDWARE);
    assertFalse(bitmap.isMutable());
    assertNotNull(bitmap.getColorSpace());

    bitmap = Bitmap.createBitmap(picture, 100, 100, Config.ARGB_8888);
    if (RuntimeEnvironment.getApiLevel() >= Q) {
      // In P, the bitmap returned is mutable.
      assertFalse(bitmap.isMutable());
    }
  }

  @org.robolectric.annotation.Config(minSdk = P)
  @Test
  public void testCreateBitmap_picture_requiresHWAcceleration_checkPixels() {
    Picture picture = new Picture();
    Canvas pictureCanvas = picture.beginRecording(100, 100);
    Paint p = new Paint();
    p.setColor(Color.RED);
    pictureCanvas.drawRect(0, 0, 100, 100, p);
    picture.endRecording();
    Bitmap bitmap = Bitmap.createBitmap(picture);
    assertThat(bitmap.getWidth()).isEqualTo(100);
    assertThat(bitmap.getHeight()).isEqualTo(100);
    assertThat(bitmap.getPixel(0, 0)).isEqualTo(Color.RED);
  }

  private void strictModeTest(Runnable runnable) {
    StrictMode.ThreadPolicy originalPolicy = StrictMode.getThreadPolicy();
    StrictMode.setThreadPolicy(
        new StrictMode.ThreadPolicy.Builder().detectCustomSlowCalls().penaltyDeath().build());
    try {
      runnable.run();
      fail("Shouldn't reach it");
    } catch (RuntimeException expected) {
      // expect to receive StrictModeViolation
    } finally {
      StrictMode.setThreadPolicy(originalPolicy);
    }
  }

  static final int ANDROID_BITMAP_FORMAT_RGBA_8888 = 1;

  private static int scaleFromDensity(int size, int sdensity, int tdensity) {
    if (sdensity == Bitmap.DENSITY_NONE || sdensity == tdensity) {
      return size;
    }

    // Scale by tdensity / sdensity, rounding up.
    return ((size * tdensity) + (sdensity >> 1)) / sdensity;
  }

  private static int[] createColors(int size) {
    int[] colors = new int[size];

    for (int i = 0; i < size; i++) {
      colors[i] = (0xFF << 24) | (i << 16) | (i << 8) | i;
    }

    return colors;
  }

  private static BitmapFactory.Options createHardwareBitmapOptions() {
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inPreferredConfig = Config.HARDWARE;
    return options;
  }
}
