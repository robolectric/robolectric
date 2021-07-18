package android.graphics;

import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.P;
import static androidx.test.InstrumentationRegistry.getTargetContext;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import android.content.res.Resources;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import androidx.test.filters.SdkSuppress;
import androidx.test.runner.AndroidJUnit4;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.internal.DoNotInstrument;
import org.robolectric.testapp.R;

/** Compatibility test for {@link Bitmap} */
@DoNotInstrument
@RunWith(AndroidJUnit4.class)
public class BitmapTest {

  private Resources resources;

  @Before
  public void setUp() {
    resources = getTargetContext().getResources();
  }

  @Config(minSdk = P)
  @SdkSuppress(minSdkVersion = P)
  @Test public void createBitmap() {

    Picture picture = new Picture();
    Canvas canvas = picture.beginRecording(200, 100);

    Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

    p.setColor(0x88FF0000);
    canvas.drawCircle(50, 50, 40, p);

    p.setColor(Color.GREEN);
    p.setTextSize(30);
    canvas.drawText("Pictures", 60, 60, p);
    picture.endRecording();

    Bitmap bitmap = Bitmap.createBitmap(picture);
    assertThat(bitmap.isMutable()).isFalse();
  }

  @Test
  public void testEraseColor() {
    Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    bitmap.eraseColor(0xffff0000);
    assertThat(bitmap.getPixel(10, 10)).isEqualTo(0xffff0000);
    assertThat(bitmap.getPixel(50, 50)).isEqualTo(0xffff0000);
  }

  @Test
  @SdkSuppress(minSdkVersion = M) // getAlpha() returns 0 on less than M
  public void testExtractAlpha() {
    // normal case
    Bitmap bitmap =
        BitmapFactory.decodeResource(resources, R.drawable.an_image, new BitmapFactory.Options());
    Bitmap ret = bitmap.extractAlpha();
    int source = bitmap.getPixel(10, 20);
    int result = ret.getPixel(10, 20);
    assertThat(Color.alpha(result)).isEqualTo(Color.alpha(source));
  }

  @Test
  public void testCopy() {
    Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    Bitmap copy = bitmap.copy(Bitmap.Config.ARGB_8888, false);
    assertThat(copy.getWidth()).isEqualTo(bitmap.getWidth());
    assertThat(copy.getHeight()).isEqualTo(bitmap.getHeight());
    assertThat(copy.getConfig()).isEqualTo(bitmap.getConfig());
  }

  @Test
  public void testCopyAndEraseColor() {
    Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    bitmap.eraseColor(0xffffff00);
    assertThat(bitmap.getPixel(10, 10)).isEqualTo(0xffffff00);
    assertThat(bitmap.getPixel(50, 50)).isEqualTo(0xffffff00);

    Bitmap copy = bitmap.copy(Bitmap.Config.ARGB_8888, true);
    assertThat(copy.getPixel(10, 10)).isEqualTo(0xffffff00);
    assertThat(copy.getPixel(50, 50)).isEqualTo(0xffffff00);

    copy.eraseColor(0xffff0000);
    assertThat(copy.getPixel(10, 10)).isEqualTo(0xffff0000);
    assertThat(copy.getPixel(50, 50)).isEqualTo(0xffff0000);
  }

  @Test
  public void compress() {
    Bitmap bitmap = BitmapFactory.decodeResource(resources, R.drawable.an_image);

    ByteArrayOutputStream stm = new ByteArrayOutputStream();
    assertThat(bitmap.compress(CompressFormat.JPEG, 0, stm)).isTrue();
    assertThat(stm.toByteArray()).isNotEmpty();
  }

  @Test
  public void getConfigAfterCompress() throws IOException {
    InputStream inputStream = resources.getAssets().open("robolectric.png");
    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
    Matrix matrix = new Matrix();
    matrix.setScale(0.5f, 0.5f);
    Bitmap scaledBitmap =
        Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, /* filter */ true);
    assertThat(scaledBitmap.getConfig()).isEqualTo(Bitmap.Config.ARGB_8888);
  }

  @Test
  public void getConfigAfterCreateScaledBitmap() throws IOException {
    InputStream inputStream = resources.getAssets().open("robolectric.png");
    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
    Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 50, 50, /* filter= */ false);
    assertThat(scaledBitmap.getConfig()).isEqualTo(Bitmap.Config.ARGB_8888);
  }

  @Test
  public void scaledBitmap_sameAs() {
    Bitmap bitmap1 = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    bitmap1.eraseColor(0xffff0000);
    Bitmap bitmap2 = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    bitmap2.eraseColor(0xff00ff00);
    assertThat(bitmap1.sameAs(bitmap2)).isFalse();

    Bitmap scaled1 = Bitmap.createScaledBitmap(bitmap1, 200, 200, false);
    Bitmap scaled2 = Bitmap.createScaledBitmap(bitmap2, 200, 200, false);
    assertThat(scaled1.sameAs(scaled2)).isFalse();
  }

  @Test
  @Config(minSdk = JELLY_BEAN)
  public void checkBitmapNotRecycled() throws IOException {
    InputStream inputStream = resources.getAssets().open("robolectric.png");
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inScaled = true;
    options.inDensity = 100;
    options.inTargetDensity = 500;
    Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
    assertThat(bitmap.isRecycled()).isFalse();
  }

  @Test
  public void decodeResource_withMutableOpt_isMutable() {
    BitmapFactory.Options opt = new BitmapFactory.Options();
    opt.inMutable = true;
    Bitmap bitmap = BitmapFactory.decodeResource(resources, R.drawable.an_image, opt);
    assertThat(bitmap.isMutable()).isTrue();
  }

  @Test
  public void scaledBitmap_isMutable() throws IOException {
    InputStream inputStream = resources.getAssets().open("robolectric.png");
    BitmapFactory.Options opt = new BitmapFactory.Options();
    opt.inMutable = true;
    Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, opt);
    Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 50, 50, false);
    assertThat(scaledBitmap.isMutable()).isTrue();
  }

  @Test
  public void colorDrawable_drawToBitmap() {
    Drawable colorDrawable = new ColorDrawable(Color.RED);
    Bitmap bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    assertThat(canvas.getWidth()).isEqualTo(1);
    assertThat(canvas.getHeight()).isEqualTo(1);
    colorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
    colorDrawable.draw(canvas);
    assertThat(bitmap.getPixel(0, 0)).isEqualTo(Color.RED);
  }

  @Test
  public void drawCanvas_bitmap_sameSize() {
    Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    bitmap.eraseColor(0xff00ff00);
    Bitmap output = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(output);
    canvas.drawBitmap(bitmap, 0, 0, null);
    assertThat(bitmap.sameAs(output)).isTrue();
  }

  @Test
  public void drawCanvas_bitmap_centered() {
    Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    bitmap.eraseColor(0xff00ff00);
    Bitmap output = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(output);
    canvas.drawBitmap(bitmap, 50, 50, null);
    assertThat(output.getPixel(49, 49)).isEqualTo(0);
    assertThat(output.getPixel(50, 50)).isEqualTo(0xff00ff00);
    assertThat(output.getPixel(149, 149)).isEqualTo(0xff00ff00);
    assertThat(output.getPixel(150, 150)).isEqualTo(0);
  }

  @Test
  public void drawCanvas_overflow_topLeft() {
    Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    bitmap.eraseColor(0xff00ff00);
    Bitmap output = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(output);
    canvas.drawBitmap(bitmap, -50, -50, null);
    assertThat(output.getPixel(0, 0)).isEqualTo(0xff00ff00);
    assertThat(output.getPixel(49, 49)).isEqualTo(0xff00ff00);
    assertThat(output.getPixel(50, 50)).isEqualTo(0);
  }

  @Test
  public void drawCanvas_overflow_topRight() {
    Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    bitmap.eraseColor(0xff00ff00);
    Bitmap output = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(output);
    canvas.drawBitmap(bitmap, 50, -50, null);
    assertThat(output.getPixel(99, 0)).isEqualTo(0xff00ff00);
    assertThat(output.getPixel(50, 49)).isEqualTo(0xff00ff00);
    assertThat(output.getPixel(49, 50)).isEqualTo(0);
  }

  @Test
  public void drawCanvas_overflow_bottomLeft() {
    Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    bitmap.eraseColor(0xff00ff00);
    Bitmap output = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(output);
    canvas.drawBitmap(bitmap, -50, 50, null);
    assertThat(output.getPixel(0, 99)).isEqualTo(0xff00ff00);
    assertThat(output.getPixel(49, 50)).isEqualTo(0xff00ff00);
    assertThat(output.getPixel(50, 49)).isEqualTo(0);
  }

  @Test
  public void drawCanvas_overflow_bottomRight() {
    Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    bitmap.eraseColor(0xff00ff00);
    Bitmap output = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(output);
    canvas.drawBitmap(bitmap, 50, 50, null);
    assertThat(output.getPixel(99, 99)).isEqualTo(0xff00ff00);
    assertThat(output.getPixel(50, 50)).isEqualTo(0xff00ff00);
    assertThat(output.getPixel(49, 49)).isEqualTo(0);
  }

  @Test
  public void createScaledBitmap_zeroWidthAndHeight_error() {
    Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    Exception exception =
        assertThrows(
            IllegalArgumentException.class, () -> Bitmap.createScaledBitmap(bitmap, 0, 0, false));

    assertThat(exception).hasMessageThat().contains("width and height must be > 0");
  }

  @Test
  public void getBitmapPixels_strideTooLong() {
    int[] bitmapPixels = {1, 2, 3, 4, 5, 6, 7, 8, 9};
    Bitmap bitmap = Bitmap.createBitmap(bitmapPixels, 3, 3, Bitmap.Config.ARGB_8888);
    int[] pixelsCopy = new int[bitmap.getHeight() * bitmap.getWidth()];
    assertThrows(
        ArrayIndexOutOfBoundsException.class,
        () ->
            bitmap.getPixels(
                pixelsCopy, 0, bitmap.getRowBytes(), 0, 0, bitmap.getWidth(), bitmap.getHeight()));
  }

  @Test
  public void eraseColor_toTransparent() {
    Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    bitmap.eraseColor(0);
    assertThat(bitmap.getPixel(0, 0)).isEqualTo(0);
  }

  @Test
  @Config(minSdk = KITKAT)
  @SdkSuppress(minSdkVersion = KITKAT)
  public void reconfigure_drawPixel() {
    Bitmap bitmap = Bitmap.createBitmap(100, 50, Bitmap.Config.ARGB_8888);
    bitmap.reconfigure(50, 100, Bitmap.Config.ARGB_8888);
    bitmap.setPixel(0, 99, Color.RED);
    assertThat(bitmap.getPixel(0, 99)).isEqualTo(Color.RED);
  }

  /**
   * Questionable ARGB_8888 pixel values like '10' may be simplified by some graphics engines to
   * '0'. This happens because '10' has alpha transparency '0', so the values for RGB don't matter.
   * This happens when Java's Graphics2d is used for certain.
   */
  @Test
  public void recompress_png100_samePixelss() {
    Bitmap applicationIconBitmap =
        Bitmap.createBitmap(new int[] {10, 11, 12, 13}, 2, 2, Bitmap.Config.ARGB_8888);

    BitmapDrawable applicationIcon = new BitmapDrawable(resources, applicationIconBitmap);

    ByteArrayOutputStream outputStream1 = new ByteArrayOutputStream();
    applicationIconBitmap.compress(CompressFormat.PNG, 100, outputStream1);

    Bitmap bitmap =
        Bitmap.createBitmap(
            applicationIcon.getIntrinsicWidth(),
            applicationIcon.getIntrinsicHeight(),
            Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    applicationIcon.draw(canvas);
    ByteArrayOutputStream outputStream2 = new ByteArrayOutputStream();
    bitmap.compress(CompressFormat.PNG, 100, outputStream2);
    assertThat(outputStream2.toByteArray()).isEqualTo(outputStream1.toByteArray());
  }

  @Test
  public void compress_thenDecodeStream_sameAs() {
    Bitmap bitmap = Bitmap.createBitmap(/* width= */ 10, /* height= */ 10, Bitmap.Config.ARGB_8888);
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    bitmap.compress(CompressFormat.PNG, /* quality= */ 100, outStream);
    byte[] outBytes = outStream.toByteArray();
    ByteArrayInputStream inStream = new ByteArrayInputStream(outBytes);
    Bitmap bitmap2 = BitmapFactory.decodeStream(inStream);
    assertThat(bitmap.sameAs(bitmap2)).isTrue();
  }

  @Test
  public void compress_asJpeg_convertsTransparentToBlack() {
    Bitmap bitmap = Bitmap.createBitmap(/* width= */ 10, /* height= */ 10, Bitmap.Config.ARGB_8888);
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    bitmap.compress(CompressFormat.JPEG, /* quality= */ 90, outStream);
    byte[] outBytes = outStream.toByteArray();
    assertThat(outBytes).isNotEmpty();
    ByteArrayInputStream inStream = new ByteArrayInputStream(outBytes);
    Bitmap bitmap2 = BitmapFactory.decodeStream(inStream);
    assertThat(bitmap2.getPixel(0, 0)).isEqualTo(Color.BLACK);
  }

  @Test
  public void createBitmapWithOffsetAndStride() {
    int[] pixels = new int[10];
    Bitmap result = Bitmap.createBitmap(pixels, 0, 2, 2, 5, Bitmap.Config.ARGB_8888);
    assertThat(result).isNotNull();
  }

  @Test
  public void extractAlpha_isMutable() {
    Bitmap result = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    Bitmap alphaBitmap = result.extractAlpha();
    assertThat(alphaBitmap.isMutable()).isTrue();
  }

  @Test
  public void createBitmap_withBitmap_containsImageData() {
    Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    bitmap.eraseColor(Color.BLUE);
    Bitmap cropped = Bitmap.createBitmap(bitmap, 0, 0, 50, 50);
    assertThat(cropped.isMutable()).isTrue();
    assertThat(cropped.getPixel(0, 0)).isEqualTo(Color.BLUE);
  }

  @Test
  public void createBitmap_withBitmap_thenCopy_isValid() {
    Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    bitmap.eraseColor(Color.BLUE);
    Bitmap cropped = Bitmap.createBitmap(bitmap, 50, 50, 50, 50);
    Bitmap copy = cropped.copy(Bitmap.Config.ARGB_8888, true);
    assertThat(copy.isMutable()).isTrue();
    assertThat(copy.getPixel(0, 0)).isEqualTo(Color.BLUE);
  }

  @Test
  public void copyPixelsFromBuffer_intBuffer() {
    Bitmap bitmap = Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888);
    IntBuffer input = IntBuffer.allocate(bitmap.getWidth() * bitmap.getHeight());
    for (int i = 0; i < input.capacity(); i++) {
      // IntBuffer is interpreted as ABGR. Use A=255 to avoid premultiplication.
      input.put((0xFF << 24) | (i + 2) << 16 | (i + 1) << 8 | i);
    }
    input.rewind();
    bitmap.copyPixelsFromBuffer(input);

    IntBuffer output = IntBuffer.allocate(input.capacity());
    bitmap.copyPixelsToBuffer(output);

    input.rewind();
    output.rewind();

    assertThat(output).isEqualTo(input);
  }

  @Test
  public void copyPixelsFromBuffer_byteBuffer() {
    Bitmap bitmap = Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888);
    ByteBuffer input = ByteBuffer.allocate(bitmap.getWidth() * bitmap.getHeight() * 4);
    for (int i = 0; i < bitmap.getWidth() * bitmap.getHeight(); i++) {
      // ByteBuffer is interpreted as RGBA. Use A=255 to avoid premultiplication.
      input.put((byte) i);
      input.put((byte) (i + 1));
      input.put((byte) (i + 2));
      input.put((byte) 0xFF);
    }
    input.rewind();
    bitmap.copyPixelsFromBuffer(input);

    ByteBuffer output = ByteBuffer.allocate(input.capacity());
    bitmap.copyPixelsToBuffer(output);

    input.rewind();
    output.rewind();

    assertThat(output).isEqualTo(input);
  }
}
