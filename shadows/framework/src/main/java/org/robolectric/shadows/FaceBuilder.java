package org.robolectric.shadows;

import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.camera2.params.Face;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

/** Builder for {@link Face}. */
public class FaceBuilder {
  private Rect bounds;
  private int score = 1;
  private int id = Face.ID_UNSUPPORTED;
  private Point leftEyePosition;
  private Point rightEyePosition;
  private Point mouthPosition;

  private FaceBuilder() {}

  public static FaceBuilder newBuilder() {
    return new FaceBuilder();
  }

  @CanIgnoreReturnValue
  public FaceBuilder setBounds(Rect bounds) {
    this.bounds = bounds;
    return this;
  }

  @CanIgnoreReturnValue
  public FaceBuilder setScore(int score) {
    this.score = score;
    return this;
  }

  @CanIgnoreReturnValue
  public FaceBuilder setId(int id) {
    this.id = id;
    return this;
  }

  @CanIgnoreReturnValue
  public FaceBuilder setLeftEyePosition(Point leftEyePosition) {
    this.leftEyePosition = leftEyePosition;
    return this;
  }

  @CanIgnoreReturnValue
  public FaceBuilder setRightEyePosition(Point rightEyePosition) {
    this.rightEyePosition = rightEyePosition;
    return this;
  }

  @CanIgnoreReturnValue
  public FaceBuilder setMouthPosition(Point mouthPosition) {
    this.mouthPosition = mouthPosition;
    return this;
  }

  public Face build() {
    return new Face(bounds, score, id, leftEyePosition, rightEyePosition, mouthPosition);
  }
}
