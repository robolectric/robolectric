package org.robolectric.shadows;


import android.graphics.drawable.ColorDrawable;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;

import static org.robolectric.Robolectric.shadowOf;

@Implements(ColorDrawable.class)
public class ShadowColorDrawable extends ShadowDrawable {

    int color;

    public void __constructor__(int color) {
      this.color = color;
    }

    @Override @Implementation
    public boolean equals(Object o) {
      if (realObject == o) return true;
      if (o == null || realObject.getClass() != o.getClass()) return false;

      if (!super.equals(o)) return false;

      ShadowColorDrawable that = shadowOf((ColorDrawable)o);

      if (color != that.color) return false;

      return true;
    }

    @Override @Implementation
    public int hashCode() {
      int result = super.hashCode();
      result = 31 * result + color;
      return result;
    }

    @Override @Implementation
    public String toString() {
        return "ColorDrawable{color=" + color + '}';
    }
}
