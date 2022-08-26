package org.robolectric.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Annotation for Conscrypt modes in Robolectric. */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PACKAGE, ElementType.TYPE, ElementType.METHOD})
public @interface ConscryptMode {

  /**
   * Specifies the different supported Conscrypt modes. If ConscryptMode is ON, it will install
   * Conscrypt. If it is OFF, it won't do that but either way BouncyCastle is still installed.
   */
  enum Mode {
    ON,

    OFF,
  }

  Mode value();
}
