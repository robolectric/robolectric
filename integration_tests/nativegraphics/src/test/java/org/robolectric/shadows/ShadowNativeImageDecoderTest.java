package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static com.google.common.base.StandardSystemProperty.OS_NAME;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertSame;
import static org.junit.Assert.assertEquals;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.ColorSpace;
import android.graphics.ImageDecoder;
import android.graphics.ImageDecoder.Source;
import com.google.common.collect.ImmutableList;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.function.IntFunction;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.versioning.AndroidVersions.V;

@Config(minSdk = P)
@RunWith(RobolectricTestRunner.class)
public class ShadowNativeImageDecoderTest {
  static final class Record {
    public final int resId;
    public final int width;
    public final int height;
    public final boolean isGray;
    public final boolean hasAlpha;
    public final String mimeType;
    public final ColorSpace colorSpace;

    Record(
        int resId,
        int width,
        int height,
        String mimeType,
        boolean isGray,
        boolean hasAlpha,
        ColorSpace colorSpace) {
      this.resId = resId;
      this.width = width;
      this.height = height;
      this.mimeType = mimeType;
      this.isGray = isGray;
      this.hasAlpha = hasAlpha;
      this.colorSpace = colorSpace;
    }
  }

  private static final ColorSpace SRGB = ColorSpace.get(ColorSpace.Named.SRGB);

  static Record[] getRecords() {
    ArrayList<Record> records =
        new ArrayList<>(
            Arrays.asList(
                new Record(R.drawable.baseline_jpeg, 1280, 960, "image/jpeg", false, false, SRGB),
                new Record(R.drawable.grayscale_jpg, 128, 128, "image/jpeg", true, false, SRGB),
                new Record(R.drawable.png_test, 640, 480, "image/png", false, false, SRGB),
                new Record(R.drawable.gif_test, 320, 240, "image/gif", false, false, SRGB),
                new Record(R.drawable.bmp_test, 320, 240, "image/bmp", false, false, SRGB),
                new Record(R.drawable.webp_test, 640, 480, "image/webp", false, false, SRGB)));

    // x-adobe-dng is not supported on Windows
    if (!OS_NAME.value().toLowerCase(Locale.ROOT).contains("win")) {
      records.add(new Record(R.raw.sample_1mp, 600, 338, "image/x-adobe-dng", false, false, SRGB));
    }

    return records.toArray(new Record[] {});
  }

  // offset is how many bytes to offset the beginning of the image.
  // extra is how many bytes to append at the end.
  private static byte[] getAsByteArray(int resId, int offset, int extra) {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    writeToStream(output, resId, offset, extra);
    return output.toByteArray();
  }

  static byte[] getAsByteArray(int resId) {
    return getAsByteArray(resId, 0, 0);
  }

  static void writeToStream(OutputStream output, int resId, int offset, int extra) {
    InputStream input = getResources().openRawResource(resId);
    byte[] buffer = new byte[4096];
    int bytesRead;
    try {
      for (int i = 0; i < offset; ++i) {
        output.write(0);
      }

      while ((bytesRead = input.read(buffer)) != -1) {
        output.write(buffer, 0, bytesRead);
      }

      for (int i = 0; i < extra; ++i) {
        output.write(0);
      }

      input.close();
    } catch (IOException e) {
      throw new AssertionError(e);
    }
  }

  private ByteBuffer getAsByteBufferWrap(int resId) {
    byte[] buffer = getAsByteArray(resId);
    return ByteBuffer.wrap(buffer);
  }

  private ByteBuffer getAsDirectByteBuffer(int resId) {
    byte[] buffer = getAsByteArray(resId);
    ByteBuffer byteBuffer = ByteBuffer.allocateDirect(buffer.length);
    byteBuffer.put(buffer);
    byteBuffer.position(0);
    return byteBuffer;
  }

  private ByteBuffer getAsReadOnlyByteBuffer(int resId) {
    return getAsByteBufferWrap(resId).asReadOnlyBuffer();
  }

  private File getAsFile(int resId) {
    try {
      File file = Files.createTempFile("ShadowNativeBitmapFactoryTest", "").toFile();
      file.deleteOnExit();
      try (FileOutputStream output = new FileOutputStream(file)) {
        writeToStream(output, resId, 0, 0);
      }
      return file;
    } catch (IOException e) {
      throw new AssertionError(e);
    }
  }

  private interface SourceCreator extends IntFunction<Source> {}

  private ImmutableList<SourceCreator> getSourceCreators() {
    ImmutableList.Builder<SourceCreator> builder = ImmutableList.builder();

    builder.add(
        resId -> ImageDecoder.createSource(getAsByteArray(resId)),
        resId -> ImageDecoder.createSource(getAsByteBufferWrap(resId)),
        resId -> ImageDecoder.createSource(getAsDirectByteBuffer(resId)),
        resId -> ImageDecoder.createSource(getAsReadOnlyByteBuffer(resId)));

    // TODO(hoisie): Support file sources in Android V+.
    if (RuntimeEnvironment.getApiLevel() < V.SDK_INT) {
      builder.add(resId -> ImageDecoder.createSource(getAsFile(resId)));
    }

    return builder.build();
  }

  private static Resources getResources() {
    return RuntimeEnvironment.getApplication().getResources();
  }

  @Test
  public void testInfo() throws Exception {
    for (Record record : getRecords()) {
      for (SourceCreator f : getSourceCreators()) {
        ImageDecoder.Source src = f.apply(record.resId);
        assertNotNull(src);
        ImageDecoder.decodeDrawable(
            src,
            (decoder, info, s) -> {
              assertEquals(record.width, info.getSize().getWidth());
              assertEquals(record.height, info.getSize().getHeight());
              assertEquals(record.mimeType, info.getMimeType());
              assertSame(record.colorSpace, info.getColorSpace());
            });
      }
    }
  }

  @Test
  public void loadNinePatch() {
    getResources().getDrawable(R.drawable.ninepatchdrawable);
  }

  static class AssetRecord {
    public final String name;
    public final int width;
    public final int height;
    public final boolean isF16;
    public final boolean isGray;
    public final boolean hasAlpha;
    private final ColorSpace colorSpace;

    AssetRecord(
        String name,
        int width,
        int height,
        boolean isF16,
        boolean isGray,
        boolean hasAlpha,
        ColorSpace colorSpace) {
      this.name = name;
      this.width = width;
      this.height = height;
      this.isF16 = isF16;
      this.isGray = isGray;
      this.hasAlpha = hasAlpha;
      this.colorSpace = colorSpace;
    }

    public ColorSpace getColorSpace() {
      return colorSpace;
    }

    public void checkColorSpace(ColorSpace requested, ColorSpace actual) {
      assertNotNull("Null ColorSpace for " + this.name, actual);
      if (this.isF16 && requested != null) {
        if (requested.equals(ColorSpace.get(ColorSpace.Named.LINEAR_SRGB))) {
          assertSame(ColorSpace.get(ColorSpace.Named.LINEAR_EXTENDED_SRGB), actual);
        } else if (requested.equals(ColorSpace.get(ColorSpace.Named.SRGB))) {
          assertSame(ColorSpace.get(ColorSpace.Named.EXTENDED_SRGB), actual);
        } else {
          assertSame(requested, actual);
        }
      } else if (requested != null) {
        // If the asset is *not* 16 bit, requesting EXTENDED will promote to 16 bit.
        assertSame(requested, actual);
      } else if (colorSpace == null) {
        assertEquals(this.name, "Unknown", actual.getName());
      } else {
        assertSame(this.name, colorSpace, actual);
      }
    }
  }

  static AssetRecord[] getAssetRecords() {
    return new AssetRecord[] {
      // A null ColorSpace means that the color space is "Unknown".
      new AssetRecord("almost-red-adobe.png", 1, 1, false, false, false, null),
      new AssetRecord(
          "green-p3.png", 64, 64, false, false, false, ColorSpace.get(ColorSpace.Named.DISPLAY_P3)),
      new AssetRecord("green-srgb.png", 64, 64, false, false, false, SRGB),
      new AssetRecord(
          "blue-16bit-prophoto.png",
          100,
          100,
          true,
          false,
          true,
          ColorSpace.get(ColorSpace.Named.PRO_PHOTO_RGB)),
      new AssetRecord(
          "blue-16bit-srgb.png",
          64,
          64,
          true,
          false,
          false,
          ColorSpace.get(ColorSpace.Named.EXTENDED_SRGB)),
      new AssetRecord("purple-cmyk.png", 64, 64, false, false, false, SRGB),
      new AssetRecord("purple-displayprofile.png", 64, 64, false, false, false, null),
      new AssetRecord(
          "red-adobergb.png",
          64,
          64,
          false,
          false,
          false,
          ColorSpace.get(ColorSpace.Named.ADOBE_RGB)),
      new AssetRecord(
          "translucent-green-p3.png",
          64,
          64,
          false,
          false,
          true,
          ColorSpace.get(ColorSpace.Named.DISPLAY_P3)),
      new AssetRecord(
          "grayscale-linearSrgb.png",
          32,
          32,
          false,
          true,
          false,
          ColorSpace.get(ColorSpace.Named.LINEAR_SRGB)),
      new AssetRecord(
          "grayscale-16bit-linearSrgb.png",
          32,
          32,
          true,
          false,
          true,
          ColorSpace.get(ColorSpace.Named.LINEAR_EXTENDED_SRGB)),
    };
  }

  @Test
  public void testAssetSource() throws Exception {
    for (AssetRecord record : getAssetRecords()) {
      AssetManager assets = getResources().getAssets();
      ImageDecoder.Source src = ImageDecoder.createSource(assets, record.name);
      Bitmap bm =
          ImageDecoder.decodeBitmap(
              src,
              (decoder, info, s) -> {
                if (record.isF16) {
                  // CTS infrastructure fails to create F16 HARDWARE Bitmaps, so this
                  // switches to using software.
                  decoder.setAllocator(ImageDecoder.ALLOCATOR_SOFTWARE);
                }

                record.checkColorSpace(null, info.getColorSpace());
              });
      assertEquals(record.name, record.width, bm.getWidth());
      assertEquals(record.name, record.height, bm.getHeight());
      if (record.name.startsWith("blue-16bit") && RuntimeEnvironment.getApiLevel() >= Q) {
        // This assertion fails for the "blue-16bit" images in Android P.
        record.checkColorSpace(null, bm.getColorSpace());
      }
      assertEquals(record.hasAlpha, bm.hasAlpha());
    }
  }
}
