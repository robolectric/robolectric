package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;

import android.graphics.Picture;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ShadowPictureTest {

  @Test
  public void beginRecordingSetsHeightAndWidth() {
    Picture picture = new Picture();
    picture.beginRecording(100, 100);
    assertThat(picture.getHeight()).isEqualTo(100);
    assertThat(picture.getWidth()).isEqualTo(100);
  }

  @Test
  public void copyConstructor() {
    Picture originalPicture = new Picture();
    originalPicture.beginRecording(100, 100);

    Picture copiedPicture = new Picture(originalPicture);
    assertThat(copiedPicture.getHeight()).isEqualTo(100);
    assertThat(copiedPicture.getWidth()).isEqualTo(100);
  }
}
