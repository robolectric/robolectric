package org.robolectric.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

/**
 * Robolectric can check for certain kinds of accessibility bugs while running
 * your tests. These are bugs that interfere with services that allow users with
 * disabilities to access your UI. When these checks are enabled, calling
 * {@code Robolectric.onClick()} will run a series of checks on your UI and
 * throw exceptions if errors are present.
 */
@java.lang.annotation.Documented
@java.lang.annotation.Retention(RetentionPolicy.RUNTIME)
@java.lang.annotation.Target({ElementType.TYPE, ElementType.METHOD})
public @interface AccessibilityChecks {
  public enum ForRobolectricVersion {
    /**
     * Only perform checks that were present in Robolectric 3.0
     */
    VERSION_3_0,
    /**
     * Perform all checks
     */
    LATEST
  }

  /**
   * Enable or disable accessibility checking. The default value is true.
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
   * <p/>
   * The default is to perform all available checks.
   */
  ForRobolectricVersion forRobolectricVersion() default ForRobolectricVersion.LATEST;
}