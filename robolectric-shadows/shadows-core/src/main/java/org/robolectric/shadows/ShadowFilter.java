package org.robolectric.shadows;

import android.widget.Filter;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;

import static org.robolectric.util.ReflectionHelpers.ClassParameter.*;

@Implements(Filter.class)
public class ShadowFilter {
  @RealObject private Filter realObject;

  @Implementation
  public void filter(CharSequence constraint, Filter.FilterListener listener) {
    try {
      Class<?> forName = Class.forName("android.widget.Filter$FilterResults");
      Object filtering = ReflectionHelpers.callInstanceMethodReflectively(realObject, "performFiltering",
         from(CharSequence.class, constraint));

      ReflectionHelpers.callInstanceMethodReflectively(realObject, "publishResults",
          from(CharSequence.class, constraint),
          from(forName, filtering));

    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Cannot load android.widget.Filter$FilterResults");
    }
  }
}
