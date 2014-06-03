package org.robolectric.shadows;

import android.widget.Filter;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Implementation;
import static org.fest.reflect.core.Reflection.*;

@Implements(Filter.class)
public class ShadowFilter {
  @RealObject private Filter realObject;

  @Implementation
  public void filter(CharSequence constraint, Filter.FilterListener listener) {
    try {
      Class<?> forName = Class.forName("android.widget.Filter$FilterResults");
      Object filtering = method("performFiltering").withReturnType(forName).withParameterTypes(CharSequence.class).in(realObject).invoke(constraint);
      method("publishResults").withParameterTypes(CharSequence.class, forName).in(realObject).invoke(constraint, filtering);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Cannot load android.widget.Filter$FilterResults");
    }
  }
}
