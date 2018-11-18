package android.graphics;

import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.P;
import static androidx.test.InstrumentationRegistry.getTargetContext;
import static com.google.common.truth.Truth.assertThat;

import android.content.res.Resources;
import android.graphics.Bitmap.CompressFormat;
import androidx.test.filters.SdkSuppress;
import androidx.test.runner.AndroidJUnit4;
import java.io.ByteArrayOutputStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.internal.DoNotInstrument;

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
  public void compress() {
    Bitmap bitmap = BitmapFactory.decodeResource(resources, R.drawable.an_image);

    ByteArrayOutputStream stm = new ByteArrayOutputStream();
    assertThat(bitmap.compress(CompressFormat.JPEG, 0, stm)).isTrue();
    assertThat(stm.toByteArray()).isNotEmpty();
  }
}
