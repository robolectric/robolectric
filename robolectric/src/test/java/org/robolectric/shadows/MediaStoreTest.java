package org.robolectric.shadows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static android.provider.MediaStore.Images;
import static android.provider.MediaStore.Video;
import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class MediaStoreTest {
  @Test
  public void shouldInitializeFields() throws Exception {
    assertThat(Images.Media.EXTERNAL_CONTENT_URI.toString()).isEqualTo("content://media/external/images/media");
    assertThat(Images.Media.INTERNAL_CONTENT_URI.toString()).isEqualTo("content://media/internal/images/media");
    assertThat(Video.Media.EXTERNAL_CONTENT_URI.toString()).isEqualTo("content://media/external/video/media");
    assertThat(Video.Media.INTERNAL_CONTENT_URI.toString()).isEqualTo("content://media/internal/video/media");
  }
}
