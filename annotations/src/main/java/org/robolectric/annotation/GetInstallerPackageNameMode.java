package org.robolectric.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A {@link org.robolectric.pluginapi.config.Configurer} annotation for controlling how Robolectric
 * executes {@code PackageManager#getInstallerPackageName} method.
 *
 * <p>'getInstallerPackageName' method in PackageManager must throw IllegalArgumentException if the
 * installer package is not present. The legacy robolectric behavior returns a null value for these
 * cases.
 *
 * <p>This annotation can be applied to tests to have Robolectric perform the legacy mechanism of
 * not throwing IllegalArgumentException and instead return 'null', when installer package name is
 * not found.
 *
 * <p>This annotation will be deleted in a forthcoming Robolectric release.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PACKAGE, ElementType.TYPE, ElementType.METHOD})
public @interface GetInstallerPackageNameMode {

  /**
   * Specifies the different {@code ShadowApplicationPackageManager#getInstallerPackageName} modes.
   */
  enum Mode {
    /** Robolectric's prior behavior when calling getInstallerPackageName method. */
    LEGACY,
    /** The new, real behavior when calling getInstallerPackageName method. */
    REALISTIC,
  }

  Mode value();

  /**
   * Optional string for storing the issue / bug id tracking the fixing of the affected tests and
   * thus removal of this annotation.
   */
  String issueId() default "";
}
