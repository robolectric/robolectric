package org.robolectric.shadows;

import android.graphics.ColorMatrix;
import java.util.ArrayList;
import java.util.List;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.Join;
import org.robolectric.util.ReflectionHelpers;

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
