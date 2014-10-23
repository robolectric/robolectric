package org.robolectric.shadows;

import android.widget.Filter;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Implements(Filter.class)
public class ShadowFilter {
  @RealObject private Filter realObject;

  @Implementation
  public void filter(CharSequence constraint, Filter.FilterListener listener) {
    try {
      Class<?> forName = Class.forName("android.widget.Filter$FilterResults");
      Object filtering;
      try {
        Method performFiltering = Filter.class.getDeclaredMethod("performFiltering", CharSequence.class);
        performFiltering.setAccessible(true);
        filtering = performFiltering.invoke(realObject, constraint);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      } catch (InvocationTargetException e) {
        throw new RuntimeException(e);
      } catch (NoSuchMethodException e) {
        throw new RuntimeException(e);
      }

      try {
        Method publishResults = Filter.class.getDeclaredMethod("publishResults", CharSequence.class, forName);
        publishResults.setAccessible(true);
        publishResults.invoke(realObject, constraint, filtering);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      } catch (InvocationTargetException e) {
        throw new RuntimeException(e);
      } catch (NoSuchMethodException e) {
        throw new RuntimeException(e);
      }
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Cannot load android.widget.Filter$FilterResults");
    }
  }
}
