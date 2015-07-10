package org.robolectric.shadows;

import android.graphics.Matrix;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.robolectric.Shadows.shadowOf;

@RunWith(TestRunners.MultiApiWithDefaults.class)
public class ShadowMatrixTest {
  @Test
  public void preOperationsAreStacked() {
    Matrix m = new Matrix();
    m.preRotate(4, 8, 15);
    m.preTranslate(16, 23);
    m.preSkew(42, 108);

    assertThat(shadowOf(m).getPreOperations()).containsExactly(
        "skew 42.0 108.0",
        "translate 16.0 23.0",
        "rotate 4.0 8.0 15.0"
    );
  }

  @Test
  public void postOperationsAreQueued() {
    Matrix m = new Matrix();
    m.postRotate(4, 8, 15);
    m.postTranslate(16, 23);
    m.postSkew(42, 108);

    assertThat(shadowOf(m).getPostOperations()).containsExactly(
        "rotate 4.0 8.0 15.0",
        "translate 16.0 23.0",
        "skew 42.0 108.0"
    );
  }

  @Test
  public void setOperationsOverride() {
    Matrix m = new Matrix();
    m.setRotate(4);
    m.setRotate(8);
    m.setRotate(15);
    m.setRotate(16);
    m.setRotate(23);
    m.setRotate(42);
    m.setRotate(108);

    assertThat(shadowOf(m).getSetOperations()).contains(entry("rotate", "108.0"));
  }

  @Test
  public void set_shouldAddOpsToMatrix() {
    final Matrix matrix = new Matrix();
    matrix.setScale(1, 1);
    matrix.preScale(2, 2, 2, 2);
    matrix.postScale(3, 3, 3, 3);

    final ShadowMatrix shadow = shadowOf(matrix);
    assertThat(shadow.getSetOperations().get("scale")).isEqualTo("1.0 1.0");
    assertThat(shadow.getPreOperations().get(0)).isEqualTo("scale 2.0 2.0 2.0 2.0");
    assertThat(shadow.getPostOperations().get(0)).isEqualTo("scale 3.0 3.0 3.0 3.0");
  }

  @Test
  public void setScale_shouldAddOpsToMatrix() {
    final Matrix matrix = new Matrix();
    matrix.setScale(1, 2, 3, 4);

    final ShadowMatrix shadow = shadowOf(matrix);
    assertThat(shadow.getSetOperations().get("scale")).isEqualTo("1.0 2.0 3.0 4.0");
  }

  @Test
  public void set_shouldOverrideValues(){
    final Matrix matrix1 = new Matrix();
    matrix1.setScale(1, 2);

    final Matrix matrix2 = new Matrix();
    matrix2.setScale(3, 4);
    matrix2.set(matrix1);

    final ShadowMatrix shadow = shadowOf(matrix2);
    assertThat(shadow.getSetOperations().get("scale")).isEqualTo("1.0 2.0");
  }

  @Test
  public void set_whenNull_shouldReset() {
    final Matrix matrix1 = new Matrix();
    matrix1.setScale(1, 2);

    final Matrix matrix2 = new Matrix();
    matrix2.set(matrix1);
    matrix2.set(null);

    final ShadowMatrix shadow = shadowOf(matrix2);
    assertThat(shadow.getSetOperations()).isEmpty();
  }
}
