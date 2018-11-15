package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.io.ByteStreams;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(qualifiers = "hdpi")
public class ShadowBitmapRegionDecoderTest {

  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Test
  public void testNewInstance() throws Exception {
    assertThat(BitmapRegionDecoder.newInstance(ByteStreams.toByteArray(getImageInputStream()), 0, 0, false))
        .isNotNull();
    assertThat(BitmapRegionDecoder.newInstance(getImageFd(), false))
        .isNotNull();
    assertThat(BitmapRegionDecoder.newInstance(getImageInputStream(), false))
        .isNotNull();
    assertThat(BitmapRegionDecoder.newInstance(getGeneratedImageFile(), false))
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
    return ApplicationProvider.getApplicationContext()
        .getResources()
        .openRawResource(R.drawable.robolectric);
  }

  private static FileDescriptor getImageFd() throws Exception {
    return ApplicationProvider.getApplicationContext()
        .getResources()
        .getAssets()
        .openFd("robolectric.png")
        .getFileDescriptor();
  }

  private String getGeneratedImageFile() throws Exception {
    BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    File tempImage = temporaryFolder.newFile();
    ImageIO.write(img, "png", tempImage);
    return tempImage.getPath();
  }
}
