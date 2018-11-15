package org.robolectric.shadows;

import android.widget.Filter;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

@Implements(Filter.class)
public class ShadowFilter {
  @RealObject private Filter realObject;

  @Implementation
  protected void filter(CharSequence constraint, Filter.FilterListener listener) {
    try {
      Class<?> forName = Class.forName("android.widget.Filter$FilterResults");
      Object filtering;
      try {
        filtering = ReflectionHelpers.callInstanceMethod(realObject, "performFiltering",
            ClassParameter.from(CharSequence.class, constraint));
      } catch (Exception e) {
        e.printStackTrace();
        filtering = ReflectionHelpers.newInstance(forName);
      }

      ReflectionHelpers.callInstanceMethod(realObject, "publishResults",
          ClassParameter.from(CharSequence.class, constraint),
          ClassParameter.from(forName, filtering));

      if (listener != null) {
        int count = filtering == null ? -1 : (int) ReflectionHelpers.getField(filtering, "count");
        listener.onFilterComplete(count);
      }
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Cannot load android.widget.Filter$FilterResults");
    }
  }
}
