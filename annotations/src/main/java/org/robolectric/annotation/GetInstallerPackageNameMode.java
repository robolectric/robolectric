package org.robolectric.annotation;

import android.content.pm.PackageManager;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A {@link org.robolectric.pluginapi.config.Configurer Configurer} annotation for controlling how
 * Robolectric executes {@link PackageManager#getInstallerPackageName} method.
 *
 * <p>{@code getInstallerPackageName} method in {@code PackageManager} must throw an {@link
 * IllegalArgumentException} if the installer package is not present. The legacy Robolectric
 * behavior returns {@code null} in this case.
 *
 * <p>This annotation can be applied to tests to have Robolectric perform the legacy mechanism of
 * not throwing {@code IllegalArgumentException} and instead return {@code null}, when installer
 * package name is not found.
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
    /**
     * Robolectric's prior behavior when calling the {@code getInstallerPackageName} method.
     *
     * @deprecated This mode behaves differently than the Android framework.
     */
    @Deprecated
    LEGACY,

    /**
     * The new, real behavior when calling the {@code getInstallerPackageName} method.
     *
     * @deprecated This is the default mode. It doesn't need to be set explicitly.
     */
    @Deprecated
    REALISTIC,
  }

  Mode value();

  /**
   * Optional string for storing the issue / bug id tracking the fixing of the affected tests and
   * thus removal of this annotation.
   */
  String issueId() default "";
}
