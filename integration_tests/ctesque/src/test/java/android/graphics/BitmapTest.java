package android.graphics;

import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.P;
import static androidx.test.InstrumentationRegistry.getTargetContext;
import static com.google.common.truth.Truth.assertThat;

import android.content.res.Resources;
import android.graphics.Bitmap.CompressFormat;
import androidx.test.filters.SdkSuppress;
import androidx.test.runner.AndroidJUnit4;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
}
