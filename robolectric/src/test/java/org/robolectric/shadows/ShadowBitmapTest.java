package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.KITKAT;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Build;
import android.os.Parcel;
import android.util.DisplayMetrics;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

@RunWith(AndroidJUnit4.class)
public class ShadowBitmapTest {
  @Test
  public void shouldCreateScaledBitmap() throws Exception {
    Bitmap originalBitmap = create("Original bitmap");
    Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 100, 200, false);
    assertThat(shadowOf(scaledBitmap).getDescription())
        .isEqualTo("Original bitmap scaled to 100 x 200");
    assertThat(scaledBitmap.getWidth()).isEqualTo(100);
    assertThat(scaledBitmap.getHeight()).isEqualTo(200);
    scaledBitmap.getPixels(new int[20000], 0, 0, 0, 0, 100, 200);
  }

  @Test
  public void shouldCreateActiveBitmap() throws Exception {
    Bitmap bitmap = Bitmap.createBitmap(100, 200, Bitmap.Config.ARGB_8888);
    assertThat(bitmap.isRecycled()).isFalse();
    assertThat(bitmap.getPixel(0, 0)).isEqualTo(0);
    assertThat(bitmap.getWidth()).isEqualTo(100);
    assertThat(bitmap.getHeight()).isEqualTo(200);
    assertThat(bitmap.getConfig()).isEqualTo(Bitmap.Config.ARGB_8888);
  }

  @Test
  public void hasAlpha() {
    Bitmap bitmap = Bitmap.createBitmap(100, 200, Bitmap.Config.ARGB_8888);
    assertThat(bitmap.hasAlpha()).isFalse();
    bitmap.setHasAlpha(true);
    assertThat(bitmap.hasAlpha()).isTrue();
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void hasMipmap() {
    Bitmap bitmap = Bitmap.createBitmap(100, 200, Bitmap.Config.ARGB_8888);
    assertThat(bitmap.hasMipMap()).isFalse();
    bitmap.setHasMipMap(true);
    assertThat(bitmap.hasMipMap()).isTrue();
  }

  @Test
  @Config(minSdk = KITKAT)
  public void getAllocationByteCount() {
    Bitmap bitmap = Bitmap.createBitmap(100, 200, Bitmap.Config.ARGB_8888);
    assertThat(bitmap.getAllocationByteCount()).isGreaterThan(0);
  }

  @Test
  public void shouldCreateBitmapWithColors() throws Exception {
    int[] colors = new int[] {
        Color.parseColor("#ff0000"), Color.parseColor("#00ff00"), Color.parseColor("#0000ff"),
        Color.parseColor("#990000"), Color.parseColor("#009900"), Color.parseColor("#000099")
    };
    Bitmap bitmap = Bitmap.createBitmap(colors, 3, 2, Bitmap.Config.ARGB_8888);
    assertThat(bitmap.getWidth()).isEqualTo(3);
    assertThat(bitmap.getHeight()).isEqualTo(2);
    assertThat(bitmap.getConfig()).isEqualTo(Bitmap.Config.ARGB_8888);
    assertThat(bitmap.getPixel(0, 0)).isEqualTo(Color.parseColor("#ff0000"));
    assertThat(bitmap.getPixel(0, 1)).isEqualTo(Color.parseColor("#990000"));
    assertThat(bitmap.getPixel(1, 0)).isEqualTo(Color.parseColor("#00ff00"));
    assertThat(bitmap.getPixel(1, 1)).isEqualTo(Color.parseColor("#009900"));
    assertThat(bitmap.getPixel(2, 0)).isEqualTo(Color.parseColor("#0000ff"));
    assertThat(bitmap.getPixel(2, 1)).isEqualTo(Color.parseColor("#000099"));
  }

  @Test
  public void shouldCreateBitmapFromAnotherBitmap() {
    Bitmap originalBitmap = create("Original bitmap");
    Bitmap newBitmap = Bitmap.createBitmap(originalBitmap);
    assertThat(shadowOf(newBitmap).getDescription())
        .isEqualTo("Original bitmap created from Bitmap object");
  }

  @Test
  public void shouldCreateBitmapWithMatrix() {
    Bitmap originalBitmap = create("Original bitmap");
    shadowOf(originalBitmap).setWidth(200);
    shadowOf(originalBitmap).setHeight(200);
    Matrix m = new Matrix();
    m.postRotate(90);
    Bitmap newBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, 100, 50, m, true);

    ShadowBitmap shadowBitmap = shadowOf(newBitmap);
    assertThat(shadowBitmap.getDescription())
        .isEqualTo("Original bitmap at (0,0) with width 100 and height 50" +
            " using matrix Matrix[pre=[], set={}, post=[rotate 90.0]] with filter");
    assertThat(shadowBitmap.getCreatedFromBitmap()).isEqualTo(originalBitmap);
    assertThat(shadowBitmap.getCreatedFromX()).isEqualTo(0);
    assertThat(shadowBitmap.getCreatedFromY()).isEqualTo(0);
    assertThat(shadowBitmap.getCreatedFromWidth()).isEqualTo(100);
    assertThat(shadowBitmap.getCreatedFromHeight()).isEqualTo(50);
    assertThat(shadowBitmap.getCreatedFromMatrix()).isEqualTo(m);
    assertThat(shadowBitmap.getCreatedFromFilter()).isEqualTo(true);
    assertThat(shadowBitmap.getWidth()).isEqualTo(50);
    assertThat(shadowBitmap.getHeight()).isEqualTo(100);
  }

  @Test
  public void shouldCreateMutableBitmap() throws Exception {
    Bitmap mutableBitmap = Bitmap.createBitmap(100, 200, Bitmap.Config.ARGB_8888);
    assertThat(mutableBitmap.isMutable()).isTrue();
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void shouldCreateMutableBitmapWithDisplayMetrics() throws Exception {
    final DisplayMetrics metrics = new DisplayMetrics();
    metrics.densityDpi = 1000;

    final Bitmap bitmap = Bitmap.createBitmap(metrics, 100, 100, Bitmap.Config.ARGB_8888);
    assertThat(bitmap.isMutable()).isTrue();
    assertThat(bitmap.getDensity()).isEqualTo(1000);
  }

  @Test
  public void shouldRecycleBitmap() throws Exception {
    Bitmap bitmap = Bitmap.createBitmap(100, 200, Bitmap.Config.ARGB_8888);
    bitmap.recycle();
    assertThat(bitmap.isRecycled()).isTrue();
  }

  @Test
  public void shouldReceiveDescriptionWhenDrawingToCanvas() throws Exception {
    Bitmap bitmap1 = create("Bitmap One");
    Bitmap bitmap2 = create("Bitmap Two");

    Canvas canvas = new Canvas(bitmap1);
    canvas.drawBitmap(bitmap2, 0, 0, null);

    assertThat(shadowOf(bitmap1).getDescription()).isEqualTo("Bitmap One\nBitmap Two");
  }

  @Test
  public void shouldReceiveDescriptionWhenDrawingToCanvasWithBitmapAndMatrixAndPaint() throws Exception {
    Bitmap bitmap1 = create("Bitmap One");
    Bitmap bitmap2 = create("Bitmap Two");

    Canvas canvas = new Canvas(bitmap1);
    canvas.drawBitmap(bitmap2, new Matrix(), null);

    assertThat(shadowOf(bitmap1).getDescription())
        .isEqualTo("Bitmap One\nBitmap Two transformed by Matrix[pre=[], set={}, post=[]]");
  }

  @Test
  public void shouldReceiveDescriptionWhenDrawABitmapToCanvasWithAPaintEffect() throws Exception {
    Bitmap bitmap1 = create("Bitmap One");
    Bitmap bitmap2 = create("Bitmap Two");

    Canvas canvas = new Canvas(bitmap1);
    Paint paint = new Paint();
    paint.setColorFilter(new ColorMatrixColorFilter(new ColorMatrix()));
    canvas.drawBitmap(bitmap2, new Matrix(), paint);

    assertThat(shadowOf(bitmap1).getDescription())
        .isEqualTo("Bitmap One\n" +
            "Bitmap Two with ColorMatrixColorFilter<1,0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,0,0,1,0>" +
            " transformed by Matrix[pre=[], set={}, post=[]]");
  }

  @Test
  public void visualize_shouldReturnDescription() throws Exception {
    Bitmap bitmap = create("Bitmap One");
    assertThat(ShadowBitmap.visualize(bitmap))
        .isEqualTo("Bitmap One");
  }

  @Test
  public void shouldCopyBitmap() {
    Bitmap bitmap = Shadow.newInstanceOf(Bitmap.class);
    Bitmap bitmapCopy = bitmap.copy(Bitmap.Config.ARGB_8888, true);
    assertThat(shadowOf(bitmapCopy).getConfig()).isEqualTo(Bitmap.Config.ARGB_8888);
    assertThat(shadowOf(bitmapCopy).isMutable()).isTrue();
  }

  @Test(expected = NullPointerException.class)
  public void rowBytesIsAccurate() {
    Bitmap b1 = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
    assertThat(b1.getRowBytes()).isEqualTo(40);
    Bitmap b2 = Bitmap.createBitmap(10, 10, Bitmap.Config.RGB_565);
    assertThat(b2.getRowBytes()).isEqualTo(20);

    // Null Bitmap.Config is not allowed.
    Bitmap b3 = Bitmap.createBitmap(10, 10, null);
    b3.getRowBytes();
  }

  @Test(expected = NullPointerException.class)
  @Config(minSdk = JELLY_BEAN_MR1)
  public void byteCountIsAccurate() {
    Bitmap b1 = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
    assertThat(b1.getByteCount()).isEqualTo(400);
    Bitmap b2 = Bitmap.createBitmap(10, 10, Bitmap.Config.RGB_565);
    assertThat(b2.getByteCount()).isEqualTo(200);

    // Null Bitmap.Config is not allowed.
    Bitmap b3 = Bitmap.createBitmap(10, 10, null);
    b3.getByteCount();
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void shouldSetDensity() {
    final Bitmap bitmap = Bitmap.createBitmap(new DisplayMetrics(), 100, 100, Bitmap.Config.ARGB_8888);
    bitmap.setDensity(1000);
    assertThat(bitmap.getDensity()).isEqualTo(1000);
  }

  @Test
  public void shouldSetPixel() {
    Bitmap bitmap = Bitmap.createBitmap(new int[] { 1 }, 1, 1, Bitmap.Config.ARGB_8888);
    shadowOf(bitmap).setMutable(true);
    bitmap.setPixel(0, 0, 2);
    assertThat(bitmap.getPixel(0, 0)).isEqualTo(2);
    assertThat(shadowOf(bitmap).getCreatedFromColors()).isEqualTo(new int[] { 1 });
  }

  @Test
  public void shouldSetPixel_allocateOnTheFly() {
    Bitmap bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
    shadowOf(bitmap).setMutable(true);
    bitmap.setPixel(0, 0, 2);
    assertThat(bitmap.getPixel(0, 0)).isEqualTo(2);
    assertThat(shadowOf(bitmap).getCreatedFromColors()).isNull();
  }

  @Test
  public void testGetPixels() {
    // Create a dummy bitmap.
    Bitmap bmp = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
    for (int y = 0; y < bmp.getHeight(); ++y) {
      for (int x = 0; x < bmp.getWidth(); ++x) {
        bmp.setPixel(x, y, packRGB(x, y, 0));
      }
    }

    // Use getPixels to get pixels as an array (getPixels was the missing Shadowed Function).
    int[] pixels = new int[bmp.getWidth() * bmp.getHeight()];
    bmp.getPixels(pixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());

    // Every entry should match regardless of accessing it by getPixel vs getPixels.
    for (int y = 0; y < bmp.getHeight(); ++y) {
      for (int x = 0; x < bmp.getWidth(); ++x) {
        assertThat(bmp.getPixel(x, y)).isEqualTo(pixels[y * bmp.getWidth() + x]);
      }
    }
  }

  @Test(expected = IllegalStateException.class)
  public void shouldThrowExceptionForSetPixelOnImmutableBitmap() {
    Bitmap bitmap = Bitmap.createBitmap(new int[] { 1 }, 1, 1, Bitmap.Config.ARGB_8888);
    bitmap.setPixel(0, 0, 2);
  }

  @Test
  public void bitmapsAreReused() {
    Bitmap b = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
    Bitmap b1 = Bitmap.createBitmap(b, 0, 0, 10, 10);
    assertThat(b1).isSameAs(b);
    Bitmap b2 = Bitmap.createBitmap(b, 0, 0, 10, 10, null, false);
    assertThat(b2).isSameAs(b);
    Bitmap b3 = Bitmap.createScaledBitmap(b, 10, 10, false);
    assertThat(b3).isSameAs(b);
  }

  @Test
  public void equalsSizeTransformReturnsOriginal() {
    Bitmap b1 = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
    Bitmap b2 = Bitmap.createBitmap(b1, 0, 0, 10, 10, null, false);
    assertThat(b1).isSameAs(b2);
    Bitmap b3 = Bitmap.createBitmap(b1, 0, 0, 10, 10, null, true);
    assertThat(b1).isSameAs(b3);
  }

  @Test(expected = IllegalArgumentException.class)
  public void throwsExceptionForInvalidDimensions() {
    Bitmap b = Bitmap.createBitmap(10, 20, Bitmap.Config.ARGB_8888);
    Bitmap.createBitmap(b, 0, 0, 20, 10, null, false);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void throwsExceptionForNegativeWidth() {
    Bitmap.createBitmap(-100, 10, Bitmap.Config.ARGB_8888);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void throwsExceptionForZeroHeight() {
    Bitmap.createBitmap(100, 0, Bitmap.Config.ARGB_8888);
  }

  @Test
  public void shouldGetPixelsFromAnyNonNullableCreatedBitmap() {
    Bitmap bitmap;
    int width = 10;
    int height = 10;

    int[] pixels = new int[width * height];
    bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

    bitmap = Bitmap.createBitmap(bitmap);
    bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

    bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
    bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

    bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, new Matrix(), false);
    bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

    bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

    bitmap = Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888);
    bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
  }

  @Test
  public void shouldGetPixelsFromSubsetOfBitmap() {
    int width = 10;
    int height = 10;
    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    int offset = 7;
    int subWidth = 3;
    int subHeight = 4;
    int x = 2;
    int y = 5;

    // Fill a region of the bitmap with increasing redness.
    int r = 0;
    for (int y0 = y; y0 < y + subHeight; y0++) {
      for (int x0 = x; x0 < x + subWidth; x0++) {
        bitmap.setPixel(x0, y0, packRGB(r++, 0, 0));
      }
    }

    // Get the pixels from that region.
    int[] pixels = new int[offset + subWidth * subHeight];
    bitmap.getPixels(pixels, offset, subWidth, x, y, subWidth, subHeight);

    // Verify that pixels contains the expected colors.
    r = 0;
    int index = offset;
    for (int y0 = 0; y0 < subHeight; y0++) {
      for (int x0 = 0; x0 < subWidth; x0++) {
        assertThat(pixels[index++]).isEqualTo(packRGB(r++, 0, 0));
      }
    }
  }

  @Test
  public void shouldAdjustDimensionsForMatrix() {
    Bitmap transformedBitmap;
    int width = 10;
    int height = 20;

    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    Matrix matrix = new Matrix();
    transformedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);
    assertThat(transformedBitmap.getWidth())
        .isEqualTo(width);
    assertThat(transformedBitmap.getHeight())
        .isEqualTo(height);

    matrix.setRotate(90);
    transformedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);
    assertThat(transformedBitmap.getWidth())
        .isEqualTo(height);
    assertThat(transformedBitmap.getHeight())
        .isEqualTo(width);

    matrix.setScale(2, 3);
    transformedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);
    assertThat(transformedBitmap.getWidth())
        .isEqualTo(width * 2);
    assertThat(transformedBitmap.getHeight())
        .isEqualTo(height * 3);
  }

  @Test
  public void shouldWriteToParcelAndReconstruct() {
    Bitmap bitmapOriginal;
    int originalWidth = 10;
    int originalHeight = 10;

    bitmapOriginal = Bitmap.createBitmap(originalWidth, originalHeight, Bitmap.Config.ARGB_8888);

    Parcel parcel = Parcel.obtain();
    bitmapOriginal.writeToParcel(parcel, 0);

    parcel.setDataPosition(0);

    Bitmap bitmapReconstructed = Bitmap.CREATOR.createFromParcel(parcel);

    // get reconstructed properties
    int reconstructedHeight = bitmapReconstructed.getHeight();
    int reconstructedWidth = bitmapReconstructed.getWidth();

    //compare bitmap properties
    assertThat(originalHeight).isEqualTo(reconstructedHeight);
    assertThat(originalWidth).isEqualTo(reconstructedWidth);
    assertThat(bitmapOriginal.getConfig()).isEqualTo(bitmapReconstructed.getConfig());

    int[] pixelsOriginal = new int[originalWidth * originalHeight];
    bitmapOriginal.getPixels(pixelsOriginal, 0, originalWidth, 0, 0, originalWidth, originalHeight);

    int[] pixelsReconstructed = new int[reconstructedWidth * reconstructedHeight];
    bitmapReconstructed.getPixels(pixelsReconstructed, 0, reconstructedWidth, 0, 0,
        reconstructedWidth, reconstructedHeight);

    assertThat(Arrays.equals(pixelsOriginal, pixelsReconstructed)).isTrue();
  }

  @Test
  public void shouldCopyPixelsToBufferAndReconstruct() {
    int width = 10;
    int height = 10;

    Bitmap bitmapOriginal = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    bitmapOriginal.setPixel(0, 0, 123);
    bitmapOriginal.setPixel(1, 1, 456);
    bitmapOriginal.setPixel(2, 2, 789);
    int[] pixelsOriginal = new int[width * height];
    bitmapOriginal.getPixels(pixelsOriginal, 0, width, 0, 0, width, height);

    ByteBuffer buffer = ByteBuffer.allocate(bitmapOriginal.getByteCount());
    bitmapOriginal.copyPixelsToBuffer(buffer);
    assertThat(buffer.position()).isEqualTo(bitmapOriginal.getByteCount());

    buffer.rewind();
    Bitmap bitmapReconstructed = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    // Set some random pixels to ensure that they're properly overwritten.
    bitmapReconstructed.setPixel(1,1, 999);
    bitmapReconstructed.setPixel(4,4, 999);
    bitmapReconstructed.copyPixelsFromBuffer(buffer);
    assertThat(buffer.position()).isEqualTo(bitmapOriginal.getByteCount());

    assertThat(bitmapReconstructed.getPixel(0, 0)).isEqualTo(123);
    assertThat(bitmapReconstructed.getPixel(1, 1)).isEqualTo(456);
    assertThat(bitmapReconstructed.getPixel(2, 2)).isEqualTo(789);

    int[] pixelsReconstructed = new int[width * height];
    bitmapReconstructed.getPixels(pixelsReconstructed, 0, width, 0, 0, width, height);
    assertThat(Arrays.equals(pixelsOriginal, pixelsReconstructed)).isTrue();
  }

  @Config(sdk = Build.VERSION_CODES.O)
  @Test
  public void getBytesPerPixel_O() {
    assertThat(ShadowBitmap.getBytesPerPixel(Bitmap.Config.RGBA_F16)).isEqualTo(8);
  }

  @Test
  public void getBytesPerPixel_preO() {
    assertThat(ShadowBitmap.getBytesPerPixel(Bitmap.Config.ARGB_8888)).isEqualTo(4);
    assertThat(ShadowBitmap.getBytesPerPixel(Bitmap.Config.RGB_565)).isEqualTo(2);
    assertThat(ShadowBitmap.getBytesPerPixel(Bitmap.Config.ARGB_4444)).isEqualTo(2);
    assertThat(ShadowBitmap.getBytesPerPixel(Bitmap.Config.ALPHA_8)).isEqualTo(1);
  }

  @Test(expected = RuntimeException.class)
  public void throwsExceptionCopyPixelsToShortBuffer() {
    Bitmap bitmapOriginal = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
    ShortBuffer buffer = ShortBuffer.allocate(bitmapOriginal.getByteCount());
    bitmapOriginal.copyPixelsToBuffer(buffer);
  }

  @Test(expected = RuntimeException.class)
  public void throwsExceptionCopyPixelsToIntBuffer() {
    Bitmap bitmapOriginal = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
    IntBuffer buffer = IntBuffer.allocate(bitmapOriginal.getByteCount());
    bitmapOriginal.copyPixelsToBuffer(buffer);
  }

  @Test(expected = RuntimeException.class)
  public void throwsExceptionCopyPixelsToLongBuffer() {
    Bitmap bitmapOriginal = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
    LongBuffer buffer = LongBuffer.allocate(bitmapOriginal.getByteCount());
    bitmapOriginal.copyPixelsToBuffer(buffer);
  }

  @Test(expected = RuntimeException.class)
  public void throwsExceptionCopyPixelsToBufferTooSmall() {
    Bitmap bitmapOriginal = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
    ByteBuffer buffer = ByteBuffer.allocate(bitmapOriginal.getByteCount() - 1);
    bitmapOriginal.copyPixelsToBuffer(buffer);
  }

  @Test(expected = RuntimeException.class)
  public void throwsExceptionCopyPixelsToBufferNonArgb8888() {
    Bitmap bitmapOriginal = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_4444);
    ByteBuffer buffer = ByteBuffer.allocate(bitmapOriginal.getByteCount());
    bitmapOriginal.copyPixelsToBuffer(buffer);
  }

  @Test(expected = RuntimeException.class)
  public void throwsExceptionCopyPixelsFromShortBuffer() {
    Bitmap bitmapOriginal = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
    ShortBuffer buffer = ShortBuffer.allocate(bitmapOriginal.getByteCount());
    bitmapOriginal.copyPixelsFromBuffer(buffer);
  }

  @Test(expected = RuntimeException.class)
  public void throwsExceptionCopyPixelsFromIntBuffer() {
    Bitmap bitmapOriginal = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
    IntBuffer buffer = IntBuffer.allocate(bitmapOriginal.getByteCount());
    bitmapOriginal.copyPixelsFromBuffer(buffer);
  }

  @Test(expected = RuntimeException.class)
  public void throwsExceptionCopyPixelsFromLongBuffer() {
    Bitmap bitmapOriginal = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
    LongBuffer buffer = LongBuffer.allocate(bitmapOriginal.getByteCount());
    bitmapOriginal.copyPixelsFromBuffer(buffer);
  }

  @Test(expected = RuntimeException.class)
  public void throwsExceptionCopyPixelsFromBufferTooSmall() {
    Bitmap bitmapOriginal = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
    ByteBuffer buffer = ByteBuffer.allocate(bitmapOriginal.getByteCount() - 1);
    bitmapOriginal.copyPixelsFromBuffer(buffer);
  }

  @Test(expected = RuntimeException.class)
  public void throwsExceptionCopyPixelsFromBufferNonArgb8888() {
    Bitmap bitmapOriginal = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_4444);
    ByteBuffer buffer = ByteBuffer.allocate(bitmapOriginal.getByteCount());
    bitmapOriginal.copyPixelsFromBuffer(buffer);
  }

  @Test(expected = IllegalStateException.class)
  public void throwsExceptionCopyPixelsFromBufferRecycled() {
    Bitmap bitmapOriginal = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
    ByteBuffer buffer = ByteBuffer.allocate(bitmapOriginal.getByteCount());
    bitmapOriginal.recycle();
    bitmapOriginal.copyPixelsFromBuffer(buffer);
  }

  @Config(sdk = Build.VERSION_CODES.KITKAT)
  @Test
  public void reconfigure_withArgb8888Bitmap_validDimensionsAndConfig_doesNotThrow() {
    Bitmap original = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    original.reconfigure(100, 100, Bitmap.Config.ARGB_8888);
  }

  @Config(sdk = Build.VERSION_CODES.O)
  @Test(expected = IllegalStateException.class)
  public void reconfigure_withHardwareBitmap_validDimensionsAndConfig_throws() {
    Bitmap original = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    ShadowBitmap shadowBitmap = Shadow.extract(original);
    shadowBitmap.setConfig(Bitmap.Config.HARDWARE);

    original.reconfigure(100, 100, Bitmap.Config.ARGB_8888);
  }

  @Config(sdk = Build.VERSION_CODES.KITKAT)
  @Test
  public void setPremultiplied() {
    Bitmap original = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    assertThat(original.isPremultiplied()).isFalse();
    original.setPremultiplied(true);
    assertThat(original.isPremultiplied()).isTrue();
    original.setPremultiplied(false);
    assertThat(original.isPremultiplied()).isFalse();
  }

  @Test
  public void sameAs_bitmapsDifferentWidth() {
    Bitmap original1 = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    Bitmap original2 = Bitmap.createBitmap(101, 100, Bitmap.Config.ARGB_8888);
    assertThat(original1.sameAs(original2)).isFalse();
  }

  @Test
  public void sameAs_bitmapsDifferentHeight() {
    Bitmap original1 = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    Bitmap original2 = Bitmap.createBitmap(100, 101, Bitmap.Config.ARGB_8888);
    assertThat(original1.sameAs(original2)).isFalse();
  }

  @Test
  public void sameAs_bitmapsDifferentConfig() {
    Bitmap original1 = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    Bitmap original2 = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_4444);
    assertThat(original1.sameAs(original2)).isFalse();
  }

  @Test
  public void sameAs_bitmapsDifferentPixels() {
    int[] pixels1 = new int[] {0, 1, 2, 3};
    Bitmap original1 = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888);
    original1.setPixels(pixels1, 0, 1, 0, 0, 2, 2);

    int[] pixels2 = new int[] {3, 2, 1, 0};
    Bitmap original2 = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888);
    original2.setPixels(pixels2, 0, 1, 0, 0, 2, 2);
    assertThat(original1.sameAs(original2)).isFalse();
  }

  @Test
  public void sameAs_bitmapsSamePixels() {
    int[] pixels = new int[] {0, 1, 2, 3};
    Bitmap original1 = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888);
    original1.setPixels(pixels, 0, 1, 0, 0, 2, 2);

    Bitmap original2 = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888);
    original2.setPixels(pixels, 0, 1, 0, 0, 2, 2);
    assertThat(original1.sameAs(original2)).isTrue();
  }

  private static Bitmap create(String name) {
    Bitmap bitmap = Shadow.newInstanceOf(Bitmap.class);
    shadowOf(bitmap).appendDescription(name);
    return bitmap;
  }

  private static int packRGB(int r, int g, int b) {
    return 0xff000000 | r << 16 | g << 8 | b;
  }
}
