package android.graphics;

import static androidx.test.InstrumentationRegistry.getTargetContext;
import static com.google.common.truth.Truth.assertThat;

import android.content.res.Resources;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory.Options;
import androidx.test.runner.AndroidJUnit4;
import java.io.ByteArrayOutputStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.annotation.internal.DoNotInstrument;

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
    resources = getTargetContext().getResources();
  }

  @Test
  public void decodeResource() {
    Bitmap bitmap = BitmapFactory.decodeResource(resources, R.drawable.an_image);
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
}
