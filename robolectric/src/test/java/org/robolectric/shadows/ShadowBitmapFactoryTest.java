package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.robolectric.Shadows.shadowOf;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.InputStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;

@RunWith(AndroidJUnit4.class)
public class ShadowBitmapFactoryTest {

  private Application context;

  @Before
  public void setUp() throws Exception {
    context = ApplicationProvider.getApplicationContext();
  }

  @Test
  public void decodeResource_shouldSetDescriptionAndCreatedFrom() {
    Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.an_image);
    ShadowBitmap shadowBitmap = shadowOf(bitmap);
    assertEquals("Bitmap for resource:org.robolectric:drawable/an_image", shadowBitmap.getDescription());
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
    bitmap.getPixels(new int[bitmap.getHeight() * bitmap.getWidth()], 0, 0, 0, 0, bitmap.getWidth(), bitmap.getHeight());
  }

  @Test
  public void decodeBytes_shouldSetDescriptionAndCreatedFrom() throws Exception {
    byte[] yummyBites = "Hi!".getBytes("UTF-8");
    Bitmap bitmap = BitmapFactory.decodeByteArray(yummyBites, 100, 100);
    ShadowBitmap shadowBitmap = shadowOf(bitmap);
    assertEquals("Bitmap for Hi! bytes 100..100", shadowBitmap.getDescription());
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
    assertEquals("Bitmap for resource:org.robolectric:drawable/an_image", shadowOf(bitmap).getDescription());
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
    assertEquals("Bitmap for " + data, shadowOf(bitmap).getDescription());
    assertEquals(123, bitmap.getWidth());
    assertEquals(456, bitmap.getHeight());
  }

  @Test
  public void decodeByteArray_shouldIncludeOffsets() {
    String data = "arbitrary bytes";
    ShadowBitmapFactory.provideWidthAndHeightHints(Uri.parse(data), 123, 456);

    byte[] bytes = data.getBytes(UTF_8);
    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 1, bytes.length - 2);
    assertEquals("Bitmap for " + data + " bytes 1..13", shadowOf(bitmap).getDescription());
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
  public void decodeWithDifferentSampleSize() {
    String name = "test";
    BitmapFactory.Options options = new BitmapFactory.Options();

    options.inSampleSize = 0;
    Bitmap bm = ShadowBitmapFactory.create(name, options);
    assertThat(bm.getWidth()).isEqualTo(100);
    assertThat(bm.getHeight()).isEqualTo(100);

    options.inSampleSize = 2;
    bm = ShadowBitmapFactory.create(name, options);
    assertThat(bm.getWidth()).isEqualTo(50);
    assertThat(bm.getHeight()).isEqualTo(50);

    options.inSampleSize = 101;
    bm = ShadowBitmapFactory.create(name, options);
    assertThat(bm.getWidth()).isEqualTo(1);
    assertThat(bm.getHeight()).isEqualTo(1);
  }

  @Test
  public void createShouldSetSizeToValueFromMapAsFirstPriority() {
    ShadowBitmapFactory.provideWidthAndHeightHints("image.png", 111, 222);

    final Bitmap bitmap = ShadowBitmapFactory.create("file:image.png", null, new Point(50, 60));

    assertThat(bitmap.getWidth()).isEqualTo(111);
    assertThat(bitmap.getHeight()).isEqualTo(222);
  }

  @Test
  public void createShouldSetSizeToParameterAsSecondPriority() {
    final Bitmap bitmap = ShadowBitmapFactory.create(null, null, new Point(70, 80));

    assertThat(bitmap.getWidth()).isEqualTo(70);
    assertThat(bitmap.getHeight()).isEqualTo(80);
  }

  @Test
  public void createShouldSetSizeToHardcodedValueAsLastPriority() {
    final Bitmap bitmap = ShadowBitmapFactory.create(null, null, null);

    assertThat(bitmap.getWidth()).isEqualTo(100);
    assertThat(bitmap.getHeight()).isEqualTo(100);
  }
}
