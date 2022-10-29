package org.robolectric.shadows;


import android.graphics.Matrix;
import java.util.List;
import java.util.Map;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.ShadowPicker;
import org.robolectric.shadows.ShadowMatrix.Picker;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(value = Matrix.class, shadowPicker = Picker.class)
public abstract class ShadowMatrix {
  public static final String TRANSLATE = "translate";
  public static final String SCALE = "scale";
  public static final String ROTATE = "rotate";
  public static final String SINCOS = "sincos";
  public static final String SKEW = "skew";
  public static final String MATRIX = "matrix";

  /**
   * A list of all 'pre' operations performed on this Matrix. The last operation performed will be
   * first in the list.
   *
   * @return A list of all 'pre' operations performed on this Matrix.
   */
  public abstract List<String> getPreOperations();

  /**
   * A list of all 'post' operations performed on this Matrix. The last operation performed will be
   * last in the list.
   *
   * @return A list of all 'post' operations performed on this Matrix.
   */
  public abstract List<String> getPostOperations();

  /**
   * A map of all 'set' operations performed on this Matrix.
   *
   * @return A map of all 'set' operations performed on this Matrix.
   */
  public abstract Map<String, String> getSetOperations();

  public abstract String getDescription();

  /** A {@link ShadowPicker} that always selects the legacy ShadowPath */
  public static class Picker implements ShadowPicker<ShadowMatrix> {
    @Override
    public Class<? extends ShadowMatrix> pickShadowClass() {
      return ShadowLegacyMatrix.class;
    }
  }
}
