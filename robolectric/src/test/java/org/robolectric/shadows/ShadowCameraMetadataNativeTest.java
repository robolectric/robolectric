package org.robolectric.shadows;

import static org.junit.Assert.assertThrows;

import android.hardware.camera2.impl.CameraMetadataNative;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class ShadowCameraMetadataNativeTest {
  @Test
  public void constructor_withOther_throwsNPE() {
    assertThrows(NullPointerException.class, () -> new CameraMetadataNative(null));
  }
}
