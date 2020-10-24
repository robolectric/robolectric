package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.annotation.ColorInt;
import android.os.Build.VERSION_CODES;
import com.android.internal.util.AnnotationValidations;
import java.lang.annotation.Annotation;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.reflector.ForType;

@Implements(
    value = com.android.internal.util.AnnotationValidations.class,
    isInAndroidSdk = false,
    minSdk = VERSION_CODES.R)
public class ShadowAnnotationValidations {

  /** Re-implement to avoid use of android-only Class.getPackageName$ */
  @Implementation
  protected static void validate(
      Class<? extends Annotation> annotation, Annotation ignored, int value) {
    if (("android.annotation".equals(annotation.getPackage().getName())
            && annotation.getSimpleName().endsWith("Res"))
        || ColorInt.class.equals(annotation)) {
      if (value < 0) {
        reflector(ReflectorAnnotationValidations.class).invalid(annotation, value);
      }
    }
  }

  /** Accessor interface for {@link AnnotationValidations}'s internals. */
  @ForType(AnnotationValidations.class)
  private interface ReflectorAnnotationValidations {
    void invalid(Class<? extends Annotation> annotation, Object value);
  }
}
