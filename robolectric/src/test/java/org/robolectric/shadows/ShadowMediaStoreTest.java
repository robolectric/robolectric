package org.robolectric.shadows;

import static android.provider.MediaStore.Images;
import static android.provider.MediaStore.Video;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ShadowMediaStoreTest {
  @Test
  public void shouldInitializeFields() throws Exception {
    assertThat(Images.Media.EXTERNAL_CONTENT_URI.toString()).isEqualTo("content://media/external/images/media");
    assertThat(Images.Media.INTERNAL_CONTENT_URI.toString()).isEqualTo("content://media/internal/images/media");
    assertThat(Video.Media.EXTERNAL_CONTENT_URI.toString()).isEqualTo("content://media/external/video/media");
    assertThat(Video.Media.INTERNAL_CONTENT_URI.toString()).isEqualTo("content://media/internal/video/media");
  }
}
