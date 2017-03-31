package org.robolectric.shadows;

import android.graphics.ColorMatrix;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.Join;

import java.util.ArrayList;
import java.util.List;

/**
 * Shadow for {@link android.graphics.ColorMatrix}.
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(ColorMatrix.class)
public class ShadowColorMatrix {

  @RealObject ColorMatrix realColorMatrix;
  
  @Override @Implementation
  public String toString() {
    List<String> floats = new ArrayList<>();
    for (float f : getMatrix()) {
      String format = String.format("%.2f", f);
      format = format.replace(".00", "");
      floats.add(format);
    }
    return Join.join(",", floats);
  }
  
  private float[] getMatrix() {
    return ReflectionHelpers.getField(realColorMatrix, "mArray");
  }
}
