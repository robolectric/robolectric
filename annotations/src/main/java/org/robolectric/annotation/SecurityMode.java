package org.robolectric.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PACKAGE, ElementType.TYPE, ElementType.METHOD})
public @interface SecurityMode {

    /** Specifies the different supported Security modes. */
    enum Mode {

        BOUNCY_CASTLE,

        CONSCRYPT,
    }

    Mode value();
}

