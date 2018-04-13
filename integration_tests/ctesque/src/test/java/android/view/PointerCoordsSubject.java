package android.view;

import android.view.MotionEvent.PointerCoords;
import android.view.MotionEvent.PointerProperties;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.FloatSubject;
import com.google.common.truth.Subject;
import com.google.common.truth.Truth;
import javax.annotation.Nullable;

/** {@link Subject} for {@link PointerProperties} */
public final class PointerCoordsSubject extends Subject<PointerCoordsSubject, PointerCoords> {

  public static PointerCoordsSubject assertThat(PointerCoords other) {
    return Truth.assertAbout(pointerCoords()).that(other);
  }

  public static Subject.Factory<PointerCoordsSubject, PointerCoords> pointerCoords() {
    return PointerCoordsSubject::new;
  }

  private PointerCoordsSubject(
      FailureMetadata failureMetadata, @Nullable PointerCoords pointerProperties) {
    super(failureMetadata, pointerProperties);
  }

  public FloatSubject x() {
    return check("x").that(actual().x);
  }

  public FloatSubject y() {
    return check("y").that(actual().y);
  }

  public FloatSubject orientation() {
    return check("orientation").that(actual().orientation);
  }

  public FloatSubject pressure() {
    return check("pressure").that(actual().pressure);
  }

  public FloatSubject size() {
    return check("size").that(actual().size);
  }

  public FloatSubject toolMajor() {
    return check("toolMajor").that(actual().toolMajor);
  }

  public FloatSubject toolMinor() {
    return check("toolMinor").that(actual().toolMinor);
  }

  public FloatSubject touchMinor() {
    return check("touchMinor").that(actual().touchMinor);
  }

  public FloatSubject touchMajor() {
    return check("touchMajor").that(actual().touchMajor);
  }

  public FloatSubject axisValue(int axis) {
    return check("getAxisValue(%s)", axis).that(actual().getAxisValue(axis));
  }
}
