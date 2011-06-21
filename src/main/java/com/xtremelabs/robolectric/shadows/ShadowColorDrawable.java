package com.xtremelabs.robolectric.shadows;


import android.graphics.drawable.ColorDrawable;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

@Implements(ColorDrawable.class)
public class ShadowColorDrawable extends ShadowDrawable {

    int colorResourceId;

    public void __constructor__(int color) {
      colorResourceId = color;
    }

    @Override @Implementation
    public boolean equals(Object o) {
      if (realObject == o) return true;
      if (o == null || realObject.getClass() != o.getClass()) return false;

      if (!super.equals(o)) return false;

      ShadowColorDrawable that = shadowOf((ColorDrawable)o);

      if (colorResourceId != that.colorResourceId) return false;

      return true;
    }

    @Override @Implementation
    public int hashCode() {
      int result = super.hashCode();
      result = 31 * result + colorResourceId;
      return result;
    }
}
