package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.animation.PropertyValuesHolder;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/**
 * Shadow for {@link PropertyValuesHolder} that works around the ART/JVM differences of accessing
 * methods.
 */
@Implements(value = PropertyValuesHolder.class, isInAndroidSdk = false)
public class ShadowPropertyValuesHolder {

  @RealObject PropertyValuesHolder realPropertyValuesHolder;

  @Implementation
  protected Method setupSetterOrGetter(
      Class<?> targetClass,
      HashMap<Class<?>, HashMap<String, Method>> propertyMapMap,
      String prefix,
      Class<?> valueType) {
    Method result =
        reflector(PropertyValuesHolderReflector.class, realPropertyValuesHolder)
            .setupSetterOrGetter(targetClass, propertyMapMap, prefix, valueType);
    if (result != null && Modifier.isPublic(result.getModifiers())) {
      // ART allows calling public methods on private or package-private classes, but the
      // JVM does not. Calling setAccessible(true) is required.
      result.setAccessible(true);
    }
    return result;
  }

  @ForType(PropertyValuesHolder.class)
  interface PropertyValuesHolderReflector {
    @Direct
    Method setupSetterOrGetter(
        Class<?> targetClass,
        HashMap<Class<?>, HashMap<String, Method>> propertyMapMap,
        String prefix,
        Class<?> valueType);
  }
}
