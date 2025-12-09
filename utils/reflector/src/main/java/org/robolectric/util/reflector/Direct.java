package org.robolectric.util.reflector;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated method should use a $$robo$$-prefixed implementation so that it can
 * be invoked in the shadow for the method.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Direct {
  /**
   * The format to use for the direct method. It can either be {@link DEFAULT} or {@link NATIVE}.
   *
   * <p>In the default format, the method that ends up getting invoked is {@code
   * $$robo$$<classname>$<methodname>}.
   *
   * <p>In the native format, the method that ends up getting invoked is {@code
   * $$robo$$<methodname>$nativeBinding}.
   *
   * @see ShadowImpl#directMethodName(String, String)
   * @see ShadowImpl#directNativeMethodName(String, String)
   */
  public static enum DirectFormat {
    DEFAULT,
    NATIVE
  }

  DirectFormat format() default DirectFormat.DEFAULT;
}
