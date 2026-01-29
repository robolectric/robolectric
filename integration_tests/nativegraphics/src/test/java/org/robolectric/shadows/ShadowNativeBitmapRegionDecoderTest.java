package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM;
import static com.google.common.truth.Truth.assertThat;

import android.graphics.Bitmap;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import androidx.test.core.app.ApplicationProvider;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(minSdk = VANILLA_ICE_CREAM)
public class ShadowNativeBitmapRegionDecoderTest {
  @Test
  public void decodeRegion_extractsCorrectColor() throws IOException {
    Bitmap sourceBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(sourceBitmap);
    Paint paint = new Paint();
    paint.setColor(Color.BLUE);
    canvas.drawRect(0, 0, 50, 50, paint);

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    sourceBitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
    byte[] bitmapData = bos.toByteArray();

    BitmapRegionDecoder decoder =
        BitmapRegionDecoder.newInstance(bitmapData, 0, bitmapData.length, false);

    Rect regionRect = new Rect(0, 0, 50, 50);
    Bitmap regionBitmap = decoder.decodeRegion(regionRect, null);

    assertThat(regionBitmap.getWidth()).isEqualTo(50);
    assertThat(regionBitmap.getHeight()).isEqualTo(50);
    assertThat(regionBitmap.getPixel(25, 25)).isEqualTo(Color.BLUE);
    decoder.recycle();
    sourceBitmap.recycle();
    regionBitmap.recycle();
  }

  @Test
  public void decodeRegion_assetInputStream() throws IOException {
    try (InputStream inputStream =
        ApplicationProvider.getApplicationContext()
            .getResources()
            .openRawResource(R.drawable.robot)) {
      BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(inputStream, false);
      assertThat(decoder.getWidth()).isEqualTo(128);
      assertThat(decoder.getHeight()).isEqualTo(128);
      Bitmap regionBitmap = decoder.decodeRegion(new Rect(0, 0, 100, 100), null);
      assertThat(regionBitmap.getWidth()).isEqualTo(100);
      assertThat(regionBitmap.getHeight()).isEqualTo(100);
      decoder.recycle();
    }
  }
}
