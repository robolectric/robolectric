package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.graphics.Picture;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
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
