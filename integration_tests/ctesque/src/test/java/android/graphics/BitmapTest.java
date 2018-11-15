package android.graphics;

import static android.os.Build.VERSION_CODES.P;
import static com.google.common.truth.Truth.assertThat;

import androidx.test.filters.SdkSuppress;
import androidx.test.runner.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.internal.DoNotInstrument;

/** Compatibility test for {@link Bitmap} */
@DoNotInstrument
@RunWith(AndroidJUnit4.class)
public class BitmapTest {

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
}
