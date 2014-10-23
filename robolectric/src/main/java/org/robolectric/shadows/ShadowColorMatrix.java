package org.robolectric.shadows;

import android.graphics.ColorMatrix;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.Join;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(ColorMatrix.class)
public class ShadowColorMatrix {

  @RealObject ColorMatrix realColorMatrix;
  
  @Override @Implementation
  public String toString() {
    List<String> floats = new ArrayList<String>();
    for (float f : getMatrix()) {
      String format = String.format("%.2f", f);
      format = format.replace(".00", "");
      floats.add(format);
    }
    return Join.join(",", floats);
  }
  
  private float[] getMatrix() {
    try {
      Field mArray = ColorMatrix.class.getDeclaredField("mArray");
      mArray.setAccessible(true);
      return (float[]) mArray.get(realColorMatrix);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (NoSuchFieldException e) {
      throw new RuntimeException(e);
    }
  }
}
