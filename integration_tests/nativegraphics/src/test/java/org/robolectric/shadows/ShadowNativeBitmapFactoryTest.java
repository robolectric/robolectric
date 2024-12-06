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

import static android.graphics.Bitmap.Config.ARGB_8888;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.Q;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.ParcelFileDescriptor;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import javax.annotation.Nullable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(minSdk = O)
public class ShadowNativeBitmapFactoryTest {
  // height and width of start.jpg
  private static final int START_HEIGHT = 31;
  private static final int START_WIDTH = 31;

  static class TestImage {
    TestImage(int id, int width, int height) {
      this.id = id;
      this.width = width;
      this.height = height;
    }

    public final int id;
    public final int width;
    public final int height;
  }

  private TestImage[] testImages() {
    return new TestImage[] {
      new TestImage(R.drawable.baseline_jpeg, 1280, 960),
      new TestImage(R.drawable.png_test, 640, 480),
      new TestImage(R.drawable.gif_test, 320, 240),
      new TestImage(R.drawable.bmp_test, 320, 240),
      new TestImage(R.drawable.webp_test, 640, 480),
    };
  }

  private Resources res;
  // opt for non-null
  private BitmapFactory.Options opt1;
  // opt for null
  private BitmapFactory.Options opt2;
  private int defaultDensity;
  private int targetDensity;

  @Before
  public void setup() {
    res = RuntimeEnvironment.getApplication().getResources();
    defaultDensity = DisplayMetrics.DENSITY_DEFAULT;
    targetDensity = res.getDisplayMetrics().densityDpi;

    opt1 = new BitmapFactory.Options();
    opt1.inScaled = false;
    opt2 = new BitmapFactory.Options();
    opt2.inScaled = false;
    opt2.inJustDecodeBounds = true;
  }

  @Test
  public void testDecodeResource1() {
    Bitmap b = BitmapFactory.decodeResource(res, R.drawable.start, opt1);
    assertNotNull(b);
    // Test the bitmap size
    assertEquals(START_HEIGHT, b.getHeight());
    assertEquals(START_WIDTH, b.getWidth());
    // Test if no bitmap
    assertNull(BitmapFactory.decodeResource(res, R.drawable.start, opt2));
  }

  @Test
  public void testDecodeResource2() {
    Bitmap b = BitmapFactory.decodeResource(res, R.drawable.start);
    assertNotNull(b);
    // Test the bitmap size
    assertEquals(START_HEIGHT * targetDensity / ((double) defaultDensity), b.getHeight(), 1.1);
    assertEquals(START_WIDTH * targetDensity / ((double) defaultDensity), b.getWidth(), 1.1);
  }

  @Test
  public void testDecodeResourceStream() {
    InputStream is = obtainInputStream();
    Rect r = new Rect(1, 1, 1, 1);
    TypedValue value = new TypedValue();
    Bitmap b = BitmapFactory.decodeResourceStream(res, value, is, r, opt1);
    assertNotNull(b);
    // Test the bitmap size
    assertEquals(START_HEIGHT, b.getHeight());
    assertEquals(START_WIDTH, b.getWidth());
  }

  @Test
  public void testDecodeByteArray1() {
    byte[] array = obtainArray();
    Bitmap b = BitmapFactory.decodeByteArray(array, 0, array.length, opt1);
    assertNotNull(b);
    // Test the bitmap size
    assertEquals(START_HEIGHT, b.getHeight());
    assertEquals(START_WIDTH, b.getWidth());
    // Test if no bitmap
    assertNull(BitmapFactory.decodeByteArray(array, 0, array.length, opt2));
  }

  @Test
  public void testDecodeByteArray2() {
    byte[] array = obtainArray();
    Bitmap b = BitmapFactory.decodeByteArray(array, 0, array.length);
    assertNotNull(b);
    // Test the bitmap size
    assertEquals(START_HEIGHT, b.getHeight());
    assertEquals(START_WIDTH, b.getWidth());
  }

  @Test
  public void testDecodeStream1() {
    InputStream is = obtainInputStream();
    Rect r = new Rect(1, 1, 1, 1);
    Bitmap b = BitmapFactory.decodeStream(is, r, opt1);
    assertNotNull(b);
    // Test the bitmap size
    assertEquals(START_HEIGHT, b.getHeight());
    assertEquals(START_WIDTH, b.getWidth());
    // Test if no bitmap
    assertNull(BitmapFactory.decodeStream(is, r, opt2));
  }

  @Test
  public void testDecodeStream2() {
    InputStream is = obtainInputStream();
    Bitmap b = BitmapFactory.decodeStream(is);
    assertNotNull(b);
    // Test the bitmap size
    assertEquals(START_HEIGHT, b.getHeight());
    assertEquals(START_WIDTH, b.getWidth());
  }

  @Test
  public void testDecodeStream3() {
    for (TestImage testImage : testImages()) {
      InputStream is = obtainInputStream(testImage.id);
      Bitmap b = BitmapFactory.decodeStream(is);
      assertNotNull(b);
      // Test the bitmap size
      assertEquals(testImage.width, b.getWidth());
      assertEquals(testImage.height, b.getHeight());
    }
  }

  @Test
  public void testDecodeStream5() {
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inPreferredConfig = ARGB_8888;

    // Decode the PNG & WebP (google_logo) images. The WebP image has
    // been encoded from PNG image.
    InputStream iStreamPng = obtainInputStream(R.drawable.google_logo_1);
    Bitmap bPng = BitmapFactory.decodeStream(iStreamPng, null, options);
    assertNotNull(bPng);
    assertEquals(ARGB_8888, bPng.getConfig());
    assertTrue(bPng.isPremultiplied());
    assertTrue(bPng.hasAlpha());

    // Decode the corresponding WebP (transparent) image (google_logo_2.webp).
    InputStream iStreamWebP1 = obtainInputStream(R.drawable.google_logo_2);
    Bitmap bWebP1 = BitmapFactory.decodeStream(iStreamWebP1, null, options);
    assertNotNull(bWebP1);
    assertEquals(ARGB_8888, bWebP1.getConfig());
    assertTrue(bWebP1.isPremultiplied());
    assertTrue(bWebP1.hasAlpha());

    // Compress the PNG image to WebP format (Quality=90) and decode it back.
    // This will test end-to-end WebP encoding and decoding.
    ByteArrayOutputStream oStreamWebp = new ByteArrayOutputStream();
    assertTrue(bPng.compress(CompressFormat.WEBP, 90, oStreamWebp));
    InputStream iStreamWebp2 = new ByteArrayInputStream(oStreamWebp.toByteArray());
    Bitmap bWebP2 = BitmapFactory.decodeStream(iStreamWebp2, null, options);
    assertNotNull(bWebP2);
    assertEquals(ARGB_8888, bWebP2.getConfig());
    assertTrue(bWebP2.isPremultiplied());
    assertTrue(bWebP2.hasAlpha());
  }

  @Test
  public void testDecodeReuseBasic() {
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inMutable = true;
    options.inSampleSize = 0; // treated as 1
    options.inScaled = false;
    Bitmap start = BitmapFactory.decodeResource(res, R.drawable.start, options);
    int originalSize = start.getByteCount();
    assertEquals(originalSize, start.getAllocationByteCount());

    options.inBitmap = start;
    options.inMutable = false; // will be overridden by non-null inBitmap
    options.inSampleSize = -42; // treated as 1
    Bitmap pass = BitmapFactory.decodeResource(res, R.drawable.pass, options);

    assertEquals(originalSize, pass.getByteCount());
    assertEquals(originalSize, pass.getAllocationByteCount());
    assertSame(start, pass);
    assertTrue(pass.isMutable());
  }

  @Test
  public void testDecodeReuseAttempt() {
    // BitmapFactory "silently" ignores an immutable inBitmap. (It does print a log message.)
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inMutable = false;

    Bitmap start = BitmapFactory.decodeResource(res, R.drawable.start, options);
    assertFalse(start.isMutable());

    options.inBitmap = start;
    Bitmap pass = BitmapFactory.decodeResource(res, R.drawable.pass, options);
    assertNotNull(pass);
    assertNotEquals(start, pass);
  }

  @Config(minSdk = Q)
  @Test
  public void testDecodeReuseRecycled() {
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inMutable = true;

    Bitmap start = BitmapFactory.decodeResource(res, R.drawable.start, options);
    assertNotNull(start);
    start.recycle();

    options.inBitmap = start;

    assertThrows(
        IllegalArgumentException.class,
        () -> BitmapFactory.decodeResource(res, R.drawable.pass, options));
  }

  /** Create bitmap sized to load unscaled resources: start, pass, and alpha */
  private Bitmap createBitmapForReuse(int pixelCount) {
    Bitmap bitmap = Bitmap.createBitmap(pixelCount, 1, ARGB_8888);
    bitmap.eraseColor(Color.BLACK);
    bitmap.setHasAlpha(false);
    return bitmap;
  }

  /** Decode resource with ResId into reuse bitmap without scaling, verifying expected hasAlpha */
  private void decodeResourceWithReuse(Bitmap reuse, int resId, boolean hasAlpha) {
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inMutable = true;
    options.inSampleSize = 1;
    options.inScaled = false;
    options.inBitmap = reuse;
    Bitmap output = BitmapFactory.decodeResource(res, resId, options);
    assertSame(reuse, output);
    assertEquals(output.hasAlpha(), hasAlpha);
  }

  @Test
  public void testDecodeReuseHasAlpha() {
    final int bitmapSize = 31; // size in pixels of start, pass, and alpha resources
    final int pixelCount = bitmapSize * bitmapSize;

    // Test reuse, hasAlpha false and true
    Bitmap bitmap = createBitmapForReuse(pixelCount);
    decodeResourceWithReuse(bitmap, R.drawable.start, false);
    decodeResourceWithReuse(bitmap, R.drawable.alpha, true);

    // Test pre-reconfigure, hasAlpha false and true
    bitmap = createBitmapForReuse(pixelCount);
    bitmap.reconfigure(bitmapSize, bitmapSize, ARGB_8888);
    bitmap.setHasAlpha(true);
    decodeResourceWithReuse(bitmap, R.drawable.start, false);

    bitmap = createBitmapForReuse(pixelCount);
    bitmap.reconfigure(bitmapSize, bitmapSize, ARGB_8888);
    decodeResourceWithReuse(bitmap, R.drawable.alpha, true);
  }

  @Test
  public void testDecodeReuseFormats() {
    for (TestImage testImage : testImages()) {
      // reuse should support all image formats
      Bitmap reuseBuffer = Bitmap.createBitmap(1000000, 1, Bitmap.Config.ALPHA_8);

      BitmapFactory.Options options = new BitmapFactory.Options();
      options.inBitmap = reuseBuffer;
      options.inSampleSize = 4;
      options.inScaled = false;
      Bitmap decoded = BitmapFactory.decodeResource(res, testImage.id, options);
      assertSame(reuseBuffer, decoded);
    }
  }

  @Test
  public void testDecodeReuseFailure() {
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inMutable = true;
    options.inScaled = false;
    options.inSampleSize = 4;
    options.inBitmap = BitmapFactory.decodeResource(res, R.drawable.robot, options);
    options.inSampleSize = 1;
    assertThrows(
        "should throw exception due to lack of space",
        IllegalArgumentException.class,
        () -> BitmapFactory.decodeResource(res, R.drawable.robot, options));
  }

  @Test
  public void testDecodeReuseScaling() {
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inMutable = true;
    options.inScaled = false;
    Bitmap original = BitmapFactory.decodeResource(res, R.drawable.robot, options);
    int originalSize = original.getByteCount();
    assertEquals(originalSize, original.getAllocationByteCount());

    options.inBitmap = original;
    options.inSampleSize = 4;
    Bitmap reduced = BitmapFactory.decodeResource(res, R.drawable.robot, options);

    assertSame(original, reduced);
    assertEquals(originalSize, reduced.getAllocationByteCount());
    assertEquals(originalSize, reduced.getByteCount() * 16);
  }

  @Test
  public void testDecodeReuseDoubleScaling() {
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inMutable = true;
    options.inScaled = false;
    options.inSampleSize = 1;
    Bitmap original = BitmapFactory.decodeResource(res, R.drawable.robot, options);
    int originalSize = original.getByteCount();

    // Verify that inSampleSize and density scaling both work with reuse concurrently
    options.inBitmap = original;
    options.inScaled = true;
    options.inSampleSize = 2;
    options.inDensity = 2;
    options.inTargetDensity = 4;
    Bitmap doubleScaled = BitmapFactory.decodeResource(res, R.drawable.robot, options);

    assertSame(original, doubleScaled);
    assertEquals(4, doubleScaled.getDensity());
    assertEquals(originalSize, doubleScaled.getByteCount());
  }

  @Test
  public void testDecodeReuseEquivalentScaling() {
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inMutable = true;
    options.inScaled = true;
    options.inDensity = 4;
    options.inTargetDensity = 2;
    Bitmap densityReduced = BitmapFactory.decodeResource(res, R.drawable.robot, options);
    assertEquals(2, densityReduced.getDensity());
    options.inBitmap = densityReduced;
    options.inDensity = 0;
    options.inScaled = false;
    options.inSampleSize = 2;
    Bitmap scaleReduced = BitmapFactory.decodeResource(res, R.drawable.robot, options);
    // verify that density isn't incorrectly carried over during bitmap reuse
    assertNotEquals(2, densityReduced.getDensity());
    assertNotEquals(0, densityReduced.getDensity());
    assertSame(densityReduced, scaleReduced);
  }

  @Test
  public void testDecodePremultipliedDefault() {
    Bitmap simplePremul = BitmapFactory.decodeResource(res, R.drawable.premul_data);
    assertTrue(simplePremul.isPremultiplied());
  }

  @Test
  public void testDecodeInPurgeableAllocationCount() {
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inSampleSize = 1;
    options.inJustDecodeBounds = false;
    options.inPurgeable = true;
    options.inInputShareable = false;
    byte[] array = obtainArray();
    Bitmap purgeableBitmap = BitmapFactory.decodeByteArray(array, 0, array.length, options);
    assertNotEquals(0, purgeableBitmap.getAllocationByteCount());
  }

  private int defaultCreationDensity;

  private void verifyScaled(@Nullable Bitmap b) {
    assertNotNull(b);
    assertEquals(START_WIDTH * 2, b.getWidth());
    assertEquals(2, b.getDensity());
  }

  private void verifyUnscaled(@Nullable Bitmap b) {
    assertNotNull(b);
    assertEquals(START_WIDTH, b.getWidth());
    assertEquals(b.getDensity(), defaultCreationDensity);
  }

  @Test
  public void testDecodeScaling() {
    BitmapFactory.Options defaultOpt = new BitmapFactory.Options();

    BitmapFactory.Options unscaledOpt = new BitmapFactory.Options();
    unscaledOpt.inScaled = false;

    BitmapFactory.Options scaledOpt = new BitmapFactory.Options();
    scaledOpt.inScaled = true;
    scaledOpt.inDensity = 1;
    scaledOpt.inTargetDensity = 2;

    defaultCreationDensity = Bitmap.createBitmap(1, 1, ARGB_8888).getDensity();

    byte[] bytes = obtainArray();

    verifyUnscaled(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
    verifyUnscaled(BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null));
    verifyUnscaled(BitmapFactory.decodeByteArray(bytes, 0, bytes.length, unscaledOpt));
    verifyUnscaled(BitmapFactory.decodeByteArray(bytes, 0, bytes.length, defaultOpt));

    verifyUnscaled(BitmapFactory.decodeStream(obtainInputStream()));
    verifyUnscaled(BitmapFactory.decodeStream(obtainInputStream(), null, null));
    verifyUnscaled(BitmapFactory.decodeStream(obtainInputStream(), null, unscaledOpt));
    verifyUnscaled(BitmapFactory.decodeStream(obtainInputStream(), null, defaultOpt));

    // scaling should only occur if Options are passed with inScaled=true
    verifyScaled(BitmapFactory.decodeByteArray(bytes, 0, bytes.length, scaledOpt));
    verifyScaled(BitmapFactory.decodeStream(obtainInputStream(), null, scaledOpt));
  }

  @Test
  public void testDecodeFileDescriptor1() throws IOException {
    try (ParcelFileDescriptor pfd = obtainParcelDescriptor(obtainPath())) {
      FileDescriptor input = pfd.getFileDescriptor();
      Rect r = new Rect(1, 1, 1, 1);
      Bitmap b = BitmapFactory.decodeFileDescriptor(input, r, opt1);
      assertNotNull(b);
      // Test the bitmap size
      assertEquals(START_HEIGHT, b.getHeight());
      assertEquals(START_WIDTH, b.getWidth());
      // Test if no bitmap
      assertNull(BitmapFactory.decodeFileDescriptor(input, r, opt2));
    }
  }

  @Test
  public void testDecodeFileDescriptor2() throws IOException {
    try (ParcelFileDescriptor pfd = obtainParcelDescriptor(obtainPath())) {
      FileDescriptor input = pfd.getFileDescriptor();
      Bitmap b = BitmapFactory.decodeFileDescriptor(input);

      assertNotNull(b);
      // Test the bitmap size
      assertEquals(START_HEIGHT, b.getHeight());
      assertEquals(START_WIDTH, b.getWidth());
    }
  }

  @Test
  public void testDecodeFileDescriptor3() throws IOException {
    for (TestImage testImage : testImages()) {
      // Arbitrary offsets to use. If the offset of the FD matches the offset of the image,
      // decoding should succeed, but if they do not match, decoding should fail.
      final long[] actualOffsets = new long[] {0, 17};
      for (int j = 0; j < actualOffsets.length; ++j) {
        long actualOffset = actualOffsets[j];
        String path = obtainPath(testImage.id, actualOffset);

        try (RandomAccessFile file = new RandomAccessFile(path, "r")) {
          FileDescriptor fd = file.getFD();
          assertTrue(fd.valid());

          // Set the offset to ACTUAL_OFFSET
          file.seek(actualOffset);
          assertEquals(file.getFilePointer(), actualOffset);

          // Now decode. This should be successful and leave the offset
          // unchanged.
          Bitmap b = BitmapFactory.decodeFileDescriptor(fd);
          assertNotNull(b);
          assertEquals(file.getFilePointer(), actualOffset);

          // Now use the other offset. It should fail to decode, and
          // the offset should remain unchanged.
          long otherOffset = actualOffsets[(j + 1) % actualOffsets.length];
          assertNotEquals(otherOffset, actualOffset);
          file.seek(otherOffset);
          assertEquals(file.getFilePointer(), otherOffset);

          b = BitmapFactory.decodeFileDescriptor(fd);
          assertNull(b);
          assertEquals(file.getFilePointer(), otherOffset);
        }
      }
    }
  }

  @Test
  public void testDecodeFileDescriptor_seekPositionUnchanged() throws IOException {
    int numEmptyBytes = 25;
    // Create a file that contains 25 empty bytes as well as the image contents of R.drawable.start
    File imageFile = obtainFile(R.drawable.start, numEmptyBytes);
    FileInputStream fis = new FileInputStream(imageFile.getAbsoluteFile());

    // Set the seek position to the start of the image data
    assertThat(fis.skip(numEmptyBytes)).isEqualTo(numEmptyBytes);
    Rect r = new Rect(1, 1, 1, 1);
    int bytesAvailable = fis.available();
    Bitmap b = BitmapFactory.decodeFileDescriptor(fis.getFD(), r, opt1);
    assertThat(b).isNotNull();

    // Check that the seek position hasn't changed
    assertThat(fis.available()).isEqualTo(bytesAvailable);

    // Test the bitmap size
    assertEquals(START_HEIGHT, b.getHeight());
    assertEquals(START_WIDTH, b.getWidth());
    fis.close();
  }

  private byte[] obtainArray() {
    ByteArrayOutputStream stm = new ByteArrayOutputStream();
    Options opt = new BitmapFactory.Options();
    opt.inScaled = false;
    Bitmap bitmap = BitmapFactory.decodeResource(res, R.drawable.start, opt);
    bitmap.compress(Bitmap.CompressFormat.JPEG, 0, stm);
    return stm.toByteArray();
  }

  private static InputStream obtainInputStream() {
    return RuntimeEnvironment.getApplication().getResources().openRawResource(R.drawable.start);
  }

  private static InputStream obtainInputStream(int resId) {
    return RuntimeEnvironment.getApplication().getResources().openRawResource(resId);
  }

  private static ParcelFileDescriptor obtainParcelDescriptor(String path) throws IOException {
    File file = new File(path);
    return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
  }

  private static String obtainPath() throws IOException {
    return obtainPath(R.drawable.start, 0);
  }

  static String obtainPath(int resId, long offset) throws IOException {
    return obtainFile(resId, offset).getPath();
  }

  /**
   * Create and return a new file.
   *
   * @param resId Original file. It will be copied into the new file.
   * @param offset Number of zeroes to write to the new file before the copied file. This allows
   *     testing decodeFileDescriptor with an offset. Must be less than or equal to 1024
   */
  static File obtainFile(int resId, long offset) throws IOException {
    assertTrue(offset >= 0);
    File dir = RuntimeEnvironment.getApplication().getFilesDir();
    dir.mkdirs();

    String name = RuntimeEnvironment.getApplication().getResources().getResourceEntryName(resId);
    if (offset > 0) {
      name = name + "_" + offset;
    }

    File file = new File(dir, name);
    if (file.exists()) {
      return file;
    }

    file.createNewFile();

    InputStream is = obtainInputStream(resId);

    FileOutputStream fOutput = new FileOutputStream(file);
    byte[] dataBuffer = new byte[1024];
    // Write a bunch of zeroes before the image.
    assertThat(offset).isAtMost(1024);
    fOutput.write(dataBuffer, 0, (int) offset);
    int readLength;
    while ((readLength = is.read(dataBuffer)) != -1) {
      fOutput.write(dataBuffer, 0, readLength);
    }
    is.close();
    fOutput.close();
    return file;
  }
}
