package org.robolectric.shadows;

import android.widget.Filter;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;

@Implements(Filter.class)
public class ShadowFilter {
  @RealObject private Filter realObject;

  @Implementation
  public void filter(CharSequence constraint, Filter.FilterListener listener) {
    try {
      Class<?> forName = Class.forName("android.widget.Filter$FilterResults");
      Object filtering;
      filtering = ReflectionHelpers.callInstanceMethodReflectively(realObject, "performFiltering", new ReflectionHelpers.ClassParameter(CharSequence.class, constraint));
      ReflectionHelpers.callInstanceMethodReflectively(realObject, "publishResults", new ReflectionHelpers.ClassParameter(CharSequence.class, constraint),
          new ReflectionHelpers.ClassParameter(forName, filtering));
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Cannot load android.widget.Filter$FilterResults");
    }
  }
}
