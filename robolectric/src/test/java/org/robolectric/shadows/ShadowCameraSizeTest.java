package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.hardware.Camera;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.shadow.api.Shadow;

@RunWith(AndroidJUnit4.class)
public class ShadowCameraSizeTest {

  private Camera.Size cameraSize;

  @Before
  public void setUp() throws Exception {
    cameraSize = Shadow.newInstanceOf(Camera.class).new Size(480, 320);
  }

  @Test
  public void testConstructor() throws Exception {
    assertThat(cameraSize.width).isEqualTo(480);
    assertThat(cameraSize.height).isEqualTo(320);
  }

  @Test
  public void testSetWidth() throws Exception {
    assertThat(cameraSize.width).isNotEqualTo(640);
    cameraSize.width = 640;
    assertThat(cameraSize.width).isEqualTo(640);
  }

  @Test
  public void testSetHeight() throws Exception {
    assertThat(cameraSize.height).isNotEqualTo(480);
    cameraSize.height = 480;
    assertThat(cameraSize.height).isEqualTo(480);
  }

}
