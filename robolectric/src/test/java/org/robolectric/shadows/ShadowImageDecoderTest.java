package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.os.Build.VERSION_CODES;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import org.robolectric.annotation.GraphicsMode.Mode;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = VERSION_CODES.P)
@GraphicsMode(Mode.LEGACY) // Non-legacy native mode is tested in ShadowNativeImageDecoderTest
public class ShadowImageDecoderTest {

  private final Context context = RuntimeEnvironment.getApplication();

  @Test
  public void mimeType_png_returnsPng() throws Exception {
    Bitmap bmp =
        ImageDecoder.decodeBitmap(
            ImageDecoder.createSource(context.getResources(), R.drawable.an_image),
            (imageDecoder, imageInfo, source) -> {
              assertThat(imageInfo.getSize().getHeight()).isEqualTo(53);
              assertThat(imageInfo.getSize().getWidth()).isEqualTo(64);
              assertThat(imageInfo.getMimeType()).isEqualTo("image/png");
            });
    assertThat(bmp).isNotNull();
  }

  @Test
  public void mimeType_jpg_returnsJpg() throws Exception {
    Bitmap bmp =
        ImageDecoder.decodeBitmap(
            ImageDecoder.createSource(context.getResources(), R.drawable.test_jpeg),
            (imageDecoder, imageInfo, source) -> {
              assertThat(imageInfo.getSize().getHeight()).isEqualTo(50);
              assertThat(imageInfo.getSize().getWidth()).isEqualTo(50);
              assertThat(imageInfo.getMimeType()).isEqualTo("image/jpeg");
            });
    assertThat(bmp).isNotNull();
  }

  @Test
  public void mimeType_gif_returnsGif() throws Exception {
    Bitmap bmp =
        ImageDecoder.decodeBitmap(
            ImageDecoder.createSource(context.getResources(), R.drawable.an_other_image),
            (imageDecoder, imageInfo, source) -> {
              assertThat(imageInfo.getSize().getWidth()).isEqualTo(32);
              assertThat(imageInfo.getSize().getHeight()).isEqualTo(18);
              assertThat(imageInfo.getMimeType()).isEqualTo("image/gif");
            });
    assertThat(bmp).isNotNull();
  }

  @Test
  public void mimeType_webp_returnsWebp() throws Exception {
    Bitmap bmp =
        ImageDecoder.decodeBitmap(
            ImageDecoder.createSource(context.getResources(), R.drawable.test_webp),
            (imageDecoder, imageInfo, source) -> {
              // The Legacy decoder doesn't support WebP images so it'll return unknown MIME type.
              assertEquals("image/unknown", imageInfo.getMimeType());
            });
    assertThat(bmp).isNotNull();
  }
}
