package org.robolectric.shadows;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import org.apache.tools.ant.filters.StringInputStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import java.io.FileDescriptor;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(TestRunners.MultiApiWithDefaults.class)
public class ShadowBitmapRegionDecoderTest {
  @Test
  public void testNewInstance() throws IOException {
    assertThat(BitmapRegionDecoder.newInstance(new byte[] {}, 0, 0, false))
        .isNotNull();
    assertThat(BitmapRegionDecoder.newInstance(new FileDescriptor(), false))
        .isNotNull();
    assertThat(BitmapRegionDecoder.newInstance(new StringInputStream("test"), false))
        .isNotNull();
    assertThat(BitmapRegionDecoder.newInstance("test", false))
        .isNotNull();
  }

  @Test
  public void testDecodeRegionReturnsExpectedSize() throws IOException {
    BitmapRegionDecoder bitmapRegionDecoder = BitmapRegionDecoder.newInstance("test", false);
    Bitmap bitmap = bitmapRegionDecoder.decodeRegion(new Rect(10, 20, 110, 220), new BitmapFactory.Options());
    assertThat(bitmap.getWidth())
        .isEqualTo(100);
    assertThat(bitmap.getHeight())
        .isEqualTo(200);
  }

  @Test
  public void testDecodeRegionReturnsExpectedConfig() throws IOException {
    BitmapRegionDecoder bitmapRegionDecoder = BitmapRegionDecoder.newInstance("test", false);

    BitmapFactory.Options options = new BitmapFactory.Options();
    assertThat(bitmapRegionDecoder.decodeRegion(new Rect(0, 0, 1, 1), options).getConfig())
        .isEqualTo(Bitmap.Config.ARGB_8888);
    options.inPreferredConfig = null;
    assertThat(bitmapRegionDecoder.decodeRegion(new Rect(0, 0, 1, 1), options).getConfig())
        .isEqualTo(Bitmap.Config.ARGB_8888);
    options.inPreferredConfig = Bitmap.Config.RGB_565;
    assertThat(bitmapRegionDecoder.decodeRegion(new Rect(0, 0, 1, 1), options).getConfig())
        .isEqualTo(Bitmap.Config.RGB_565);
  }
}
