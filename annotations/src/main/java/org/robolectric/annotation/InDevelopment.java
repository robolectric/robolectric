package org.robolectric.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * InDevelopment applies to @Implementation methods and @Implements classes that are affected by
 * changes in unreleased versions of Android. <br>
 * Only unreleased versions of android, as defined in the AndroidVersions honor this annotation
 * during validation.
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface InDevelopment {}
