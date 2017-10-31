package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import com.google.common.io.ByteStreams;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.res.FsFile;
import org.robolectric.util.TestUtil;

@RunWith(RobolectricTestRunner.class)
public class ShadowBitmapRegionDecoderTest {

  private static final FsFile IMAGE_FILE = TestUtil.resourcesBaseDir().join("res/drawable-hdpi/robolectric.png");

  @Test
  public void testNewInstance() throws Exception {
    assertThat(BitmapRegionDecoder.newInstance(ByteStreams.toByteArray(getImageInputStream()), 0, 0, false))
        .isNotNull();
    assertThat(BitmapRegionDecoder.newInstance(getImageFd(), false))
        .isNotNull();
    assertThat(BitmapRegionDecoder.newInstance(getImageInputStream(), false))
        .isNotNull();
    assertThat(BitmapRegionDecoder.newInstance(IMAGE_FILE.getPath(), false))
        .isNotNull();
  }

  @Test
  public void getWidthAndGetHeight_shouldReturnCorrectValuesForImage() throws Exception {
    BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(getImageInputStream(), true);
    assertThat(decoder.getWidth()).isEqualTo(297);
    assertThat(decoder.getHeight()).isEqualTo(251);
  }

  @Test
  public void testDecodeRegionReturnsExpectedSize() throws IOException {
    BitmapRegionDecoder bitmapRegionDecoder = BitmapRegionDecoder.newInstance(getImageInputStream(), false);
    Bitmap bitmap = bitmapRegionDecoder.decodeRegion(new Rect(10, 20, 110, 220), new BitmapFactory.Options());
    assertThat(bitmap.getWidth())
        .isEqualTo(100);
    assertThat(bitmap.getHeight())
        .isEqualTo(200);
  }

  @Test
  public void testDecodeRegionReturnsExpectedConfig() throws IOException {
    BitmapRegionDecoder bitmapRegionDecoder = BitmapRegionDecoder.newInstance(getImageInputStream(), false);

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

  private static InputStream getImageInputStream() {
    return RuntimeEnvironment.application.getResources().openRawResource(R.drawable.robolectric);
  }

  private static FileDescriptor getImageFd() throws Exception {
    return new FileInputStream(IMAGE_FILE.getPath()).getFD();
  }
}
