package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.camera2.params.Face;
import android.os.Build.VERSION_CODES;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = VERSION_CODES.LOLLIPOP)
public final class FaceBuilderTest {

  @Test
  public void build_createsFaceWithLandmarks() {
    Rect bounds = new Rect(0, 0, 100, 100);
    Point leftEye = new Point(25, 25);
    Point rightEye = new Point(75, 25);
    Point mouth = new Point(50, 75);

    Face face =
        FaceBuilder.newBuilder()
            .setBounds(bounds)
            .setScore(80)
            .setId(1)
            .setLeftEyePosition(leftEye)
            .setRightEyePosition(rightEye)
            .setMouthPosition(mouth)
            .build();

    assertThat(face).isNotNull();
    assertThat(face.getBounds()).isEqualTo(bounds);
    assertThat(face.getScore()).isEqualTo(80);
    assertThat(face.getId()).isEqualTo(1);
    assertThat(face.getLeftEyePosition()).isEqualTo(leftEye);
    assertThat(face.getRightEyePosition()).isEqualTo(rightEye);
    assertThat(face.getMouthPosition()).isEqualTo(mouth);
  }
}
