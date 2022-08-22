package android.graphics;

import static com.google.common.truth.Truth.assertThat;
import static java.lang.Math.round;

import android.content.res.Resources;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory.Options;
import android.os.Build;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.google.common.truth.TruthJUnit;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.internal.DoNotInstrument;
import org.robolectric.testapp.R;

/** Compatibility test for {@link BitmapFactory} */
@DoNotInstrument
@RunWith(AndroidJUnit4.class)
public class BitmapFactoryTest {

  // height and width of start.jpg
  private static final int START_HEIGHT = 53;
  private static final int START_WIDTH = 64;

  private Resources resources;

  @Before
  public void setUp() {
    resources = InstrumentationRegistry.getInstrumentation().getTargetContext().getResources();
  }

  @Test
  public void decodeResource() {
    Options opt = new BitmapFactory.Options();
    opt.inScaled = false;
    Bitmap bitmap = BitmapFactory.decodeResource(resources, R.drawable.an_image, opt);
    assertThat(bitmap.getHeight()).isEqualTo(START_HEIGHT);
    assertThat(bitmap.getWidth()).isEqualTo(START_WIDTH);
  }

  @Test
  public void testDecodeByteArray1() {
    byte[] array = obtainArray();

    Options options1 = new Options();
    options1.inScaled = false;

    Bitmap b = BitmapFactory.decodeByteArray(array, 0, array.length, options1);
    assertThat(b).isNotNull();
    // Test the bitmap size
    assertThat(b.getHeight()).isEqualTo(START_HEIGHT);
    assertThat(b.getWidth()).isEqualTo(START_WIDTH);
  }

  @Test
  public void testDecodeByteArray2() {
    byte[] array = obtainArray();
    Bitmap b = BitmapFactory.decodeByteArray(array, 0, array.length);
    assertThat(b).isNotNull();
    // Test the bitmap size
    assertThat(b.getHeight()).isEqualTo(START_HEIGHT);
    assertThat(b.getWidth()).isEqualTo(START_WIDTH);
  }

  private byte[] obtainArray() {
    ByteArrayOutputStream stm = new ByteArrayOutputStream();
    Options opt = new BitmapFactory.Options();
    opt.inScaled = false;
    Bitmap bitmap = BitmapFactory.decodeResource(resources, R.drawable.an_image, opt);
    bitmap.compress(CompressFormat.PNG, 0, stm);
    return stm.toByteArray();
  }

  /**
   * When methods such as {@link BitmapFactory#decodeStream(InputStream, Rect, Options)} are called
   * with invalid Bitmap data, the return value should be null, and {@link
   * BitmapFactory.Options#outWidth} and {@link BitmapFactory.Options#outHeight} should be set to
   * -1. This tests fails in Robolectric due to legacy BitmapFactory behavior of always returning a
   * Bitmap object, even if the bitmap data is invalid. Once {@link
   * org.robolectric.shadows.ShadowBitmap} defaults to not allowing invalid Bitmap data, this test
   * can be enabled for Robolectric.
   */
  @Test
  public void decodeStream_options_setsOutWidthToMinusOne() {
    TruthJUnit.assume().that(Build.FINGERPRINT).isNotEqualTo("robolectric");
    byte[] invalidBitmapPixels = "invalid bitmap pixels".getBytes(Charset.defaultCharset());
    ByteArrayInputStream inputStream = new ByteArrayInputStream(invalidBitmapPixels);
    BitmapFactory.Options opts = new Options();
    Bitmap result = BitmapFactory.decodeStream(inputStream, null, opts);
    assertThat(result).isEqualTo(null);
    assertThat(opts.outWidth).isEqualTo(-1);
    assertThat(opts.outHeight).isEqualTo(-1);
  }

  @Test
  public void decodeFile_scaledDensity_shouldHaveCorrectWidthAndHeight() throws Exception {
    BitmapFactory.Options opts = new BitmapFactory.Options();
    opts.inScaled = true;
    opts.inDensity = 2;
    opts.inTargetDensity = 1;
    Bitmap bitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(obtainArray()), null, opts);

    assertThat(bitmap.getWidth()).isEqualTo(round(START_WIDTH / 2f));
    assertThat(bitmap.getHeight()).isEqualTo(round(START_HEIGHT / 2f));
  }
}
