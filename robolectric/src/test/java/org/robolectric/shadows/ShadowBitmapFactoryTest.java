package org.robolectric.shadows;

import static com.google.common.io.Resources.toByteArray;
import static com.google.common.truth.Truth.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.robolectric.Shadows.shadowOf;

import android.app.Application;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.io.ByteStreams;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
public class ShadowBitmapFactoryTest {
  private static final int TEST_JPEG_WIDTH = 50;
  private static final int TEST_JPEG_HEIGHT = 50;

  private Application context;

  @Before
  public void setUp() throws Exception {
    context = ApplicationProvider.getApplicationContext();
  }

  @Test
  public void decodeResource_shouldSetDescriptionAndCreatedFrom() {
    Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.an_image);
    ShadowBitmap shadowBitmap = shadowOf(bitmap);
    assertEquals(
        "Bitmap for resource:org.robolectric:drawable/an_image", shadowBitmap.getDescription());
    assertEquals(R.drawable.an_image, shadowBitmap.getCreatedFromResId());
    assertEquals(64, bitmap.getWidth());
    assertEquals(53, bitmap.getHeight());
  }

  @Test
  public void decodeResource_shouldSetDefaultBitmapConfig() {
    Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.an_image);
    assertThat(bitmap.getConfig()).isEqualTo(Bitmap.Config.ARGB_8888);
    assertThat(bitmap.getRowBytes()).isNotEqualTo(0);
  }

  @Test
  public void withResId0_decodeResource_shouldReturnNull() {
    assertThat(BitmapFactory.decodeResource(context.getResources(), 0)).isNull();
  }

  @Test
  public void decodeResource_shouldPassABitmapConfig() {
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inPreferredConfig = Bitmap.Config.ALPHA_8;
    Bitmap bitmap =
        BitmapFactory.decodeResource(context.getResources(), R.drawable.an_image, options);
    assertThat(bitmap.getConfig()).isEqualTo(Bitmap.Config.ALPHA_8);
  }

  @Test
  public void decodeResource_sameAs() throws IOException {
    Resources resources = context.getResources();
    Bitmap bitmap = BitmapFactory.decodeResource(resources, R.drawable.an_image);
    File tmp = Files.createTempFile("BitmapTest", null).toFile();
    try (FileOutputStream stream = new FileOutputStream(tmp)) {
      bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
    }
    FileInputStream fileInputStream = new FileInputStream(tmp);
    Bitmap bitmap2 = BitmapFactory.decodeStream(fileInputStream);
    assertThat(bitmap.sameAs(bitmap2)).isTrue();
  }

  @Test
  public void decodeResource_shouldGetCorrectColorFromPngImage() {
    Resources resources = context.getResources();
    BitmapFactory.Options opts = new BitmapFactory.Options();
    Bitmap bitmap = BitmapFactory.decodeResource(resources, R.drawable.an_image, opts);
    assertThat(bitmap.getPixel(0, 0) != 0).isTrue();
  }

  @Test
  public void decodeFile_shouldSetDescriptionAndCreatedFrom() {
    Bitmap bitmap = BitmapFactory.decodeFile("/some/file.jpg");
    ShadowBitmap shadowBitmap = shadowOf(bitmap);
    assertEquals("Bitmap for file:/some/file.jpg", shadowBitmap.getDescription());
    assertEquals("/some/file.jpg", shadowBitmap.getCreatedFromPath());
    assertEquals(100, bitmap.getWidth());
    assertEquals(100, bitmap.getHeight());
  }

  @Test
  public void decodeStream_shouldSetDescriptionAndCreatedFrom() throws Exception {
    InputStream inputStream =
        context.getContentResolver().openInputStream(Uri.parse("content:/path"));
    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
    ShadowBitmap shadowBitmap = shadowOf(bitmap);
    assertEquals("Bitmap for content:/path", shadowBitmap.getDescription());
    assertEquals(inputStream, shadowBitmap.getCreatedFromStream());
    assertEquals(100, bitmap.getWidth());
    assertEquals(100, bitmap.getHeight());
    bitmap.getPixels(
        new int[bitmap.getHeight() * bitmap.getWidth()],
        0,
        0,
        0,
        0,
        bitmap.getWidth(),
        bitmap.getHeight());
  }

  @Test
  public void decodeBytes_shouldSetDescriptionAndCreatedFrom() throws Exception {
    byte[] yummyBites = "Hi!".getBytes("UTF-8");
    Bitmap bitmap = BitmapFactory.decodeByteArray(yummyBites, 100, 100);
    ShadowBitmap shadowBitmap = shadowOf(bitmap);
    assertEquals("Bitmap for 3 bytes 100..100", shadowBitmap.getDescription());
    assertEquals(yummyBites, shadowBitmap.getCreatedFromBytes());
    assertEquals(100, bitmap.getWidth());
    assertEquals(100, bitmap.getHeight());
  }

  @Test
  public void decodeBytes_shouldSetDescriptionAndCreatedFromWithOptions() throws Exception {
    byte[] yummyBites = "Hi!".getBytes("UTF-8");
    BitmapFactory.Options options = new BitmapFactory.Options();
    Bitmap bitmap = BitmapFactory.decodeByteArray(yummyBites, 100, 100, options);
    ShadowBitmap shadowBitmap = shadowOf(bitmap);
    assertEquals("Bitmap for 3 bytes 100..100", shadowBitmap.getDescription());
    assertEquals(yummyBites, shadowBitmap.getCreatedFromBytes());
    assertEquals(100, bitmap.getWidth());
    assertEquals(100, bitmap.getHeight());
  }

  @Test
  public void decodeStream_shouldSetDescriptionWithNullOptions() throws Exception {
    InputStream inputStream =
        context.getContentResolver().openInputStream(Uri.parse("content:/path"));
    Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, null);
    assertEquals("Bitmap for content:/path", shadowOf(bitmap).getDescription());
    assertEquals(100, bitmap.getWidth());
    assertEquals(100, bitmap.getHeight());
  }

  @Test
  public void decodeResource_shouldGetWidthAndHeightFromHints() {
    ShadowBitmapFactory.provideWidthAndHeightHints(R.drawable.an_image, 123, 456);

    Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.an_image);
    assertEquals(
        "Bitmap for resource:org.robolectric:drawable/an_image", shadowOf(bitmap).getDescription());
    assertEquals(123, bitmap.getWidth());
    assertEquals(456, bitmap.getHeight());
  }

  @Test
  public void decodeResource_canTakeOptions() {
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inSampleSize = 100;
    Bitmap bitmap =
        BitmapFactory.decodeResource(context.getResources(), R.drawable.an_image, options);
    assertEquals(true, shadowOf(bitmap).getDescription().contains("inSampleSize=100"));
  }

  @Test
  public void decodeResourceStream_canTakeOptions() throws Exception {
    BitmapFactory.Options options = new BitmapFactory.Options();
    InputStream inputStream =
        context.getContentResolver().openInputStream(Uri.parse("content:/path"));
    options.inSampleSize = 100;
    Bitmap bitmap =
        BitmapFactory.decodeResourceStream(
            context.getResources(), null, inputStream, null, options);
    assertEquals(true, shadowOf(bitmap).getDescription().contains("inSampleSize=100"));
  }

  @Test
  public void decodeResourceStream_shouldGetCorrectColorFromPngImage() {
    assertEquals(Color.BLACK, getPngImageColorFromResourceStream("res/drawable/pure_black.png"));
    assertEquals(Color.BLUE, getPngImageColorFromResourceStream("res/drawable/pure_blue.png"));
    assertEquals(Color.GREEN, getPngImageColorFromResourceStream("res/drawable/pure_green.png"));
    assertEquals(Color.RED, getPngImageColorFromResourceStream("res/drawable/pure_red.png"));
    assertEquals(Color.WHITE, getPngImageColorFromResourceStream("res/drawable/pure_white.png"));
  }

  @Test
  public void decodeFile_shouldGetWidthAndHeightFromHints() {
    ShadowBitmapFactory.provideWidthAndHeightHints("/some/file.jpg", 123, 456);

    Bitmap bitmap = BitmapFactory.decodeFile("/some/file.jpg");
    assertEquals("Bitmap for file:/some/file.jpg", shadowOf(bitmap).getDescription());
    assertEquals(123, bitmap.getWidth());
    assertEquals(456, bitmap.getHeight());
  }

  @Test
  public void decodeFileEtc_shouldSetOptionsOutWidthAndOutHeightFromHints() {
    ShadowBitmapFactory.provideWidthAndHeightHints("/some/file.jpg", 123, 456);

    BitmapFactory.Options options = new BitmapFactory.Options();
    BitmapFactory.decodeFile("/some/file.jpg", options);
    assertEquals(123, options.outWidth);
    assertEquals(456, options.outHeight);
  }

  @Test
  public void decodeUri_shouldGetWidthAndHeightFromHints() throws Exception {
    ShadowBitmapFactory.provideWidthAndHeightHints(Uri.parse("content:/path"), 123, 456);

    Bitmap bitmap =
        MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.parse("content:/path"));
    assertEquals("Bitmap for content:/path", shadowOf(bitmap).getDescription());
    assertEquals(123, bitmap.getWidth());
    assertEquals(456, bitmap.getHeight());
  }

  @SuppressWarnings("ObjectToString")
  @Test
  public void decodeFileDescriptor_shouldGetWidthAndHeightFromHints() throws Exception {
    File tmpFile = File.createTempFile("BitmapFactoryTest", null);
    try {
      tmpFile.deleteOnExit();
      try (FileInputStream is = new FileInputStream(tmpFile)) {
        FileDescriptor fd = is.getFD();
        ShadowBitmapFactory.provideWidthAndHeightHints(fd, 123, 456);

        Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fd);
        assertEquals("Bitmap for fd:" + fd, shadowOf(bitmap).getDescription());
        assertEquals(123, bitmap.getWidth());
        assertEquals(456, bitmap.getHeight());
      }
    } finally {
      tmpFile.delete();
    }
  }

  @Test
  public void decodeByteArray_shouldGetWidthAndHeightFromHints() {
    String data = "arbitrary bytes";
    ShadowBitmapFactory.provideWidthAndHeightHints(Uri.parse(data), 123, 456);

    byte[] bytes = data.getBytes(UTF_8);
    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    assertEquals("Bitmap for " + bytes.length + " bytes", shadowOf(bitmap).getDescription());
    assertEquals(123, bitmap.getWidth());
    assertEquals(456, bitmap.getHeight());
  }

  @Test
  public void decodeByteArray_shouldIncludeOffsets() {
    String data = "arbitrary bytes";
    ShadowBitmapFactory.provideWidthAndHeightHints(Uri.parse(data), 123, 456);

    byte[] bytes = data.getBytes(UTF_8);
    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 1, bytes.length - 2);
    assertEquals("Bitmap for " + bytes.length + " bytes 1..13", shadowOf(bitmap).getDescription());
  }

  @Test
  public void decodeByteArray_shouldGetCorrectColorFromPngImage() {
    assertEquals(Color.BLACK, getPngImageColorFromByteArray("res/drawable/pure_black.png"));
    assertEquals(Color.BLUE, getPngImageColorFromByteArray("res/drawable/pure_blue.png"));
    assertEquals(Color.GREEN, getPngImageColorFromByteArray("res/drawable/pure_green.png"));
    assertEquals(Color.RED, getPngImageColorFromByteArray("res/drawable/pure_red.png"));
    assertEquals(Color.WHITE, getPngImageColorFromByteArray("res/drawable/pure_white.png"));
  }

  @Test
  public void decodeStream_shouldGetWidthAndHeightFromHints() throws Exception {
    ShadowBitmapFactory.provideWidthAndHeightHints(Uri.parse("content:/path"), 123, 456);

    InputStream inputStream =
        context.getContentResolver().openInputStream(Uri.parse("content:/path"));
    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
    assertEquals("Bitmap for content:/path", shadowOf(bitmap).getDescription());
    assertEquals(123, bitmap.getWidth());
    assertEquals(456, bitmap.getHeight());
  }

  @Test
  public void decodeStream_shouldGetWidthAndHeightFromActualImage() {
    InputStream inputStream =
        getClass().getClassLoader().getResourceAsStream("res/drawable/fourth_image.jpg");
    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
    assertEquals("Bitmap", shadowOf(bitmap).getDescription());
    assertEquals(160, bitmap.getWidth());
    assertEquals(107, bitmap.getHeight());
  }

  @Test
  public void decodeStream_shouldGetCorrectMimeTypeFromJpegImage() throws Exception {
    InputStream inputStream =
        new BufferedInputStream(
            getClass().getClassLoader().getResourceAsStream("res/drawable/fourth_image.jpg"));
    inputStream.mark(inputStream.available());
    BitmapFactory.Options opts = new BitmapFactory.Options();
    BitmapFactory.decodeStream(inputStream, /* outPadding= */ null, opts);
    assertEquals("image/jpeg", opts.outMimeType);
  }

  @Test
  public void decodeStream_shouldGetCorrectMimeTypeFromPngImage() throws Exception {
    InputStream inputStream =
        new BufferedInputStream(
            getClass().getClassLoader().getResourceAsStream("res/drawable/an_image.png"));
    inputStream.mark(inputStream.available());
    BitmapFactory.Options opts = new BitmapFactory.Options();
    BitmapFactory.decodeStream(inputStream, /* outPadding= */ null, opts);
    assertEquals("image/png", opts.outMimeType);
  }

  @Test
  public void decodeStream_shouldGetCorrectMimeTypeFromGifImage() throws Exception {
    InputStream inputStream =
        new BufferedInputStream(
            getClass().getClassLoader().getResourceAsStream("res/drawable/an_other_image.gif"));
    inputStream.mark(inputStream.available());
    BitmapFactory.Options opts = new BitmapFactory.Options();
    BitmapFactory.decodeStream(inputStream, /* outPadding= */ null, opts);
    assertEquals("image/gif", opts.outMimeType);
  }

  @Test
  public void decodeStream_shouldGetCorrectColorFromPngImage() throws Exception {
    assertEquals(Color.BLACK, getPngImageColorFromStream("res/drawable/pure_black.png"));
    assertEquals(Color.BLUE, getPngImageColorFromStream("res/drawable/pure_blue.png"));
    assertEquals(Color.GREEN, getPngImageColorFromStream("res/drawable/pure_green.png"));
    assertEquals(Color.RED, getPngImageColorFromStream("res/drawable/pure_red.png"));
    assertEquals(Color.WHITE, getPngImageColorFromStream("res/drawable/pure_white.png"));
  }

  @Test
  public void decodeStream_shouldSameAsCompressedBefore() {
    Bitmap bitmap = Bitmap.createBitmap(/* width= */ 10, /* height= */ 10, Bitmap.Config.ARGB_8888);
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    bitmap.compress(Bitmap.CompressFormat.PNG, /* quality= */ 100, outStream);
    byte[] outBytes = outStream.toByteArray();
    ByteArrayInputStream inStream = new ByteArrayInputStream(outBytes);
    Bitmap newBitmap = BitmapFactory.decodeStream(inStream);
    assertThat(bitmap.sameAs(newBitmap)).isTrue();
  }

  @Test
  public void decodeWithDifferentSampleSize() {
    String name = "test";
    BitmapFactory.Options options = new BitmapFactory.Options();

    options.inSampleSize = 0;
    Bitmap bm = ShadowBitmapFactory.create(name, options, null);
    assertThat(bm.getWidth()).isEqualTo(100);
    assertThat(bm.getHeight()).isEqualTo(100);

    options.inSampleSize = 2;
    bm = ShadowBitmapFactory.create(name, options, null);
    assertThat(bm.getWidth()).isEqualTo(50);
    assertThat(bm.getHeight()).isEqualTo(50);

    options.inSampleSize = 101;
    bm = ShadowBitmapFactory.create(name, options, null);
    assertThat(bm.getWidth()).isEqualTo(1);
    assertThat(bm.getHeight()).isEqualTo(1);
  }

  @Test
  public void decodeFile_shouldGetCorrectColorFromPngImage() throws IOException {
    int color = Color.RED;
    File file = getBitmapFileFromResourceStream("res/drawable/pure_red.png");
    Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
    assertEquals(color, bitmap.getPixel(0, 0));
    bitmap.recycle();
  }

  @Test
  public void decodeFile_shouldHaveCorrectWidthAndHeight() throws IOException {
    Bitmap bitmap = getBitmapFromResourceStream("res/drawable/test_jpeg.jpg");
    assertThat(bitmap.getWidth()).isEqualTo(TEST_JPEG_WIDTH);
    assertThat(bitmap.getHeight()).isEqualTo(TEST_JPEG_HEIGHT);
    File tmpFile = File.createTempFile("ShadowBitmapFactoryTest", ".jpg");
    tmpFile.deleteOnExit();
    try (FileOutputStream fileOutputStream = new FileOutputStream(tmpFile)) {
      bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fileOutputStream);
    }
    bitmap.recycle();
    Bitmap loadedBitmap = BitmapFactory.decodeFile(tmpFile.getAbsolutePath());
    assertThat(loadedBitmap.getWidth()).isEqualTo(TEST_JPEG_WIDTH);
    assertThat(loadedBitmap.getHeight()).isEqualTo(TEST_JPEG_HEIGHT);
    loadedBitmap.recycle();
  }

  @Test
  public void decodeFile_shouldGetCorrectColorFromCompressedPngFile() throws IOException {
    decodeFile_shouldGetCorrectColorFromCompressedFile(
        Bitmap.CompressFormat.PNG,
        getBitmapByteArrayFromResourceStream("res/drawable/an_image.png"));
  }

  @Test
  public void decodeFile_shouldGetCorrectColorFromCompressedWebpFile() throws IOException {
    decodeFile_shouldGetCorrectColorFromCompressedFile(
        Bitmap.CompressFormat.WEBP,
        getBitmapByteArrayFromResourceStream("res/drawable/test_webp.webp"));
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.R)
  public void decodeFile_shouldGetCorrectColorFromCompressedWebpLossyFile() throws IOException {
    decodeFile_shouldGetCorrectColorFromCompressedFile(
        Bitmap.CompressFormat.WEBP_LOSSY,
        getBitmapByteArrayFromResourceStream("res/drawable/test_webp_lossy.webp"));
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.R)
  public void decodeFile_shouldGetCorrectColorFromCompressedWebpLosslessFile() throws IOException {
    decodeFile_shouldGetCorrectColorFromCompressedFile(
        Bitmap.CompressFormat.WEBP_LOSSLESS,
        getBitmapByteArrayFromResourceStream("res/drawable/test_webp_lossless.webp"));
  }

  @Test
  public void decodeFileDescriptor_shouldHaveCorrectWidthAndHeight() throws IOException {
    Bitmap bitmap = getBitmapFromResourceStream("res/drawable/test_jpeg.jpg");
    assertThat(bitmap.getWidth()).isEqualTo(TEST_JPEG_WIDTH);
    assertThat(bitmap.getHeight()).isEqualTo(TEST_JPEG_HEIGHT);

    File tmpFile = File.createTempFile("ShadowBitmapFactoryTest", ".jpg");
    tmpFile.deleteOnExit();
    try (FileOutputStream fileOutputStream = new FileOutputStream(tmpFile)) {
      bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fileOutputStream);
    }
    bitmap.recycle();
    try (FileInputStream fileInputStream = new FileInputStream(tmpFile)) {
      Bitmap loadedBitmap = BitmapFactory.decodeFileDescriptor(fileInputStream.getFD());
      assertThat(loadedBitmap.getWidth()).isEqualTo(TEST_JPEG_WIDTH);
      assertThat(loadedBitmap.getHeight()).isEqualTo(TEST_JPEG_HEIGHT);
      loadedBitmap.recycle();
    }
  }

  @Test
  public void decodeFileDescriptor_shouldGetCorrectColorFromPngImage() throws IOException {
    assertEquals(Color.BLACK, getPngImageColorFromFileDescriptor("res/drawable/pure_black.png"));
    assertEquals(Color.BLUE, getPngImageColorFromFileDescriptor("res/drawable/pure_blue.png"));
    assertEquals(Color.GREEN, getPngImageColorFromFileDescriptor("res/drawable/pure_green.png"));
    assertEquals(Color.RED, getPngImageColorFromFileDescriptor("res/drawable/pure_red.png"));
    assertEquals(Color.WHITE, getPngImageColorFromFileDescriptor("res/drawable/pure_white.png"));
  }

  /**
   * When methods such as {@link BitmapFactory#decodeStream(InputStream, android.graphics.Rect,
   * android.graphics.BitmapFactory.Options)} are called with invalid Bitmap data, the return value
   * should be null, and {@link BitmapFactory.Options#outWidth} and {@link
   * BitmapFactory.Options#outHeight} should be set to -1.
   */
  @Test
  public void decodeStream_options_setsOutWidthToMinusOne() {
    ShadowBitmapFactory.setAllowInvalidImageData(false);
    byte[] invalidBitmapPixels = "invalid bitmap pixels".getBytes(UTF_8);
    ByteArrayInputStream inputStream = new ByteArrayInputStream(invalidBitmapPixels);
    BitmapFactory.Options opts = new BitmapFactory.Options();
    Bitmap result = BitmapFactory.decodeStream(inputStream, null, opts);
    assertThat(result).isEqualTo(null);
    assertThat(opts.outWidth).isEqualTo(-1);
    assertThat(opts.outHeight).isEqualTo(-1);
  }

  private void decodeFile_shouldGetCorrectColorFromCompressedFile(
      Bitmap.CompressFormat format, byte[] bitmapData) throws IOException {
    Bitmap oldBitmap = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length);
    Path tempFile = Files.createTempFile("bitmap", null);
    FileOutputStream fileOutputStream = new FileOutputStream(tempFile.toFile());
    // lossless compression
    oldBitmap.compress(format, 100, fileOutputStream);
    fileOutputStream.close();
    Bitmap newBitmap = BitmapFactory.decodeFile(tempFile.toAbsolutePath().toString());

    ByteBuffer oldBuffer = ByteBuffer.allocate(oldBitmap.getHeight() * oldBitmap.getRowBytes());
    oldBitmap.copyPixelsToBuffer(oldBuffer);

    ByteBuffer newBuffer = ByteBuffer.allocate(newBitmap.getHeight() * newBitmap.getRowBytes());
    newBitmap.copyPixelsToBuffer(newBuffer);
    assertThat(oldBuffer.array()).isEqualTo(newBuffer.array());
  }

  private int getPngImageColorFromStream(String pngImagePath) throws IOException {
    InputStream inputStream =
        new BufferedInputStream(getClass().getClassLoader().getResourceAsStream(pngImagePath));
    inputStream.mark(inputStream.available());
    BitmapFactory.Options opts = new BitmapFactory.Options();
    Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, opts);
    return bitmap.getPixel(0, 0);
  }

  private int getPngImageColorFromFileDescriptor(String pngImagePath) throws IOException {
    File file = getBitmapFileFromResourceStream(pngImagePath);
    FileInputStream fileInputStream = new FileInputStream(file);
    Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fileInputStream.getFD());
    int color = bitmap.getPixel(0, 0);
    bitmap.recycle();
    return color;
  }

  private File getBitmapFileFromResourceStream(String imagePath) throws IOException {
    InputStream inputStream = com.google.common.io.Resources.getResource(imagePath).openStream();
    File tempFile = Files.createTempFile("ShadowBitmapFactoryTest", null).toFile();
    tempFile.deleteOnExit();
    ByteStreams.copy(inputStream, new FileOutputStream(tempFile));
    return tempFile;
  }

  private int getPngImageColorFromByteArray(String pngImagePath) {
    try (InputStream inputStream =
        new BufferedInputStream(getClass().getClassLoader().getResourceAsStream(pngImagePath))) {
      inputStream.mark(inputStream.available());
      byte[] array = new byte[inputStream.available()];
      inputStream.read(array);
      Bitmap bitmap = BitmapFactory.decodeByteArray(array, 0, array.length);
      return bitmap.getPixel(0, 0);
    } catch (IOException e) {
      return Integer.MIN_VALUE;
    }
  }

  private int getPngImageColorFromResourceStream(String pngImagePath) {
    Bitmap bitmap = getBitmapFromResourceStream(pngImagePath);
    return bitmap == null ? Integer.MIN_VALUE : bitmap.getPixel(0, 0);
  }

  private Bitmap getBitmapFromResourceStream(String imagePath) {
    try (InputStream inputStream =
        new BufferedInputStream(getClass().getClassLoader().getResourceAsStream(imagePath))) {
      inputStream.mark(inputStream.available());
      BitmapFactory.Options opts = new BitmapFactory.Options();
      Resources resources = context.getResources();
      return BitmapFactory.decodeResourceStream(resources, null, inputStream, null, opts);
    } catch (IOException e) {
      return null;
    }
  }

  private byte[] getBitmapByteArrayFromResourceStream(String imagePath) throws IOException {
    return toByteArray(com.google.common.io.Resources.getResource(imagePath));
  }
}
