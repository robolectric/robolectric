package org.robolectric.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Robolectric can check for certain kinds of accessibility bugs while running
 * your tests. These are bugs that interfere with services that allow users with
 * disabilities to access your UI. When these checks are enabled, calling
 * {@code Robolectric.onClick()} will run a series of checks on your UI and
 * throw exceptions if errors are present.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface AccessibilityChecks {
  enum ForRobolectricVersion { VERSION_3_0, VERSION_3_1, LATEST }

  /**
   * Enable or disable accessibility checking.
   *
   * @return  True if accessibility checking is enabled.
   */
  boolean enabled() default true;

  /**
   * Accessibility checking can be a moving target. As new checks are added to
   * Robolectric, these checks may reveal issues with a UI that previously
   * passed all checks (but were probably affecting users all along). This
   * option forces Robolectric to run only those checks that were present in a
   * specified version of Robolectric, which reduces the opportunity for a
   * new bug to be discovered in an old test. Note that this option does not
   * guarantee that the behavior of individual checks won't change as bugs are
   * fixed and/or features added to checks that existed in the specified
   * version.
   *
   * @return  Run all checks corresponding to this version of Robolectric.
   */
  ForRobolectricVersion forRobolectricVersion() default ForRobolectricVersion.LATEST;
}