package org.robolectric.android;

import android.view.View;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckPreset;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult.AccessibilityCheckResultType;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResultUtils;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityViewCheckResult;
import com.google.android.apps.common.testing.accessibility.framework.checks.DuplicateClickableBoundsCheck;
import com.google.android.apps.common.testing.accessibility.framework.checks.TouchTargetSizeCheck;
import com.google.android.apps.common.testing.accessibility.framework.integrations.espresso.AccessibilityValidator;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.robolectric.annotation.AccessibilityChecks;
import org.robolectric.annotation.AccessibilityChecks.ForRobolectricVersion;

/**
 * Utility class for checking Views for accessibility.
 *
 * This class is used by {@code ShadowView.checkedPerformClick} to check for accessibility problems.
 * There is some subtlety to checking a UI for accessibility when it hasn't been rendered. The
 * better initialized the View, the more accurate the checking will be. At a minimum, the view
 * should be attached to a proper view hierarchy similar to what's checked for in:q
 * {@code ShadowView.checkedPerformClick}.
 */
public class AccessibilityUtil {
  private static final String COMPAT_V4_CLASS_NAME = "android.support.v4.view.ViewCompat";
  /* The validator that this class configures and uses to run the checks */
  private static AccessibilityValidator validator;
  
  /* 
   * Slightly hacky way to deal with the legacy of allowing the annotation to configure the 
   * subset of checks to run from the annotation. {@code true} when a version set is
   * specified by setRunChecksForRobolectricVersion.
   */
  private static boolean forVersionSet = false;

  /* Flag indicating if the support library's presence has been verified */
  private static boolean v4SupportPresenceVerified = false;

  protected AccessibilityUtil() {}

  /**
   * Check a hierarchy of {@code View}s for accessibility. Only performs checks if (in decreasing
   * priority order) accessibility checking is enabled using an {@link AccessibilityChecks}
   * annotation, if the system property {@code robolectric.accessibility.enablechecks} is set to
   * {@code true}, or if the environment variable {@code robolectric.accessibility.enablechecks}
   * is set to {@code true}.
   *
   * @param view The {@code View} to examine
   *
   * @return A list of results from the check. If there are no results or checking is disabled, 
   * the list is empty.
   */
  public static List<AccessibilityViewCheckResult> checkViewIfCheckingEnabled(View view) {
    AccessibilityChecks classChecksAnnotation = getAnnotation();
    if (!isAccessibilityCheckingEnabled(classChecksAnnotation)) {
      return Collections.emptyList();
    }

    return checkView(view);
  }

  /**
   * Check a hierarchy of {@code View}s for accessibility, based on currently set options.
   *
   * @param view The {@code View} to examine
   *
   * @return A list of results from the check. If there are no results, the list is empty.
   */
  public static List<AccessibilityViewCheckResult> checkView(View view) {
    return checkView(view, getAnnotation());
  }

  /**
   * Check a hierarchy of {@code View}s for accessibility. Only performs checks if (in decreasing
   * priority order) accessibility checking is enabled using an {@link AccessibilityChecks}
   * annotation, if the system property {@code robolectric.accessibility.enablechecks} is set to
   * {@code true}, or if the environment variable {@code robolectric.accessibility.enablechecks}
   * is set to {@code true}.
   *
   * Implicitly calls {code setThrowExceptionForErrors(false)} to disable exception throwing. This
   * method is deprecated, both because of this side effect and because the other methods offer
   * more control over execution.
   *
   * @param view The {@code View} to examine
   *
   * @return A list of results from the check. If there are no results or checking is disabled,
   * the list is empty.
   */
  @Deprecated
  public static boolean passesAccessibilityChecksIfEnabled(View view) {
    setThrowExceptionForErrors(false);
    List<AccessibilityViewCheckResult> results = checkViewIfCheckingEnabled(view);
    List<AccessibilityViewCheckResult> errors = AccessibilityCheckResultUtils.getResultsForType(
        results, AccessibilityCheckResultType.ERROR);
    return (errors.size() == 0);
  }

  /**
   * Specify that a specific subset of accessibility checks be run. The subsets are specified based
   * on which Robolectric version particular checks were released with. By default, all checks are
   * run {@link ForRobolectricVersion}.
   *
   * If you call this method, the value you pass will take precedence over any value in any 
   * annotations. 
   * 
   * @param forVersion The version of checks to run for. If {@code null}, throws away the current
   * value and falls back on the annotation or default.
   */
  public static void setRunChecksForRobolectricVersion(ForRobolectricVersion forVersion) {
    initializeValidator();
    if (forVersion != null) {
      validator.setCheckPreset(convertRoboVersionToA11yTestVersion(forVersion));
      forVersionSet = true;
    } else {
      forVersionSet = false;
    }
  }
  
  /**
   * Specify that accessibility checks should be run for all views in the hierarchy whenever a
   * single view's accessibility is asserted.
   * 
   * @param runChecksFromRootView {@code true} if all views in the hierarchy should be checked.
   */
  public static void setRunChecksFromRootView(boolean runChecksFromRootView) {
    initializeValidator();
    validator.setRunChecksFromRootView(runChecksFromRootView);
  }
  
  /**
   * Suppress all results that match the given matcher. Suppressed results will not be included
   * in any logs or cause any {@code Exception} to be thrown. This capability is useful if there
   * are known issues, but checks should still look for regressions.
   * 
   * @param matcher A matcher to match a {@link AccessibilityViewCheckResult}. {@code null}
   * disables suppression and is the default.
   */
  public static void setSuppressingResultMatcher(
      final Matcher<? super AccessibilityViewCheckResult> matcher) {
    initializeValidator();
    /* Suppress all touch target results, since views all report size as 0x0 */
    Matcher<AccessibilityCheckResult> touchTargetResultMatcher =
        AccessibilityCheckResultUtils.matchesCheck(TouchTargetSizeCheck.class);
    Matcher<AccessibilityCheckResult> duplicateBoundsResultMatcher =
        AccessibilityCheckResultUtils.matchesCheck(DuplicateClickableBoundsCheck.class);
    if (matcher == null) {
      validator.setSuppressingResultMatcher(
          Matchers.anyOf(touchTargetResultMatcher, duplicateBoundsResultMatcher));
    } else {
      validator.setSuppressingResultMatcher(
          Matchers.anyOf(matcher, touchTargetResultMatcher, duplicateBoundsResultMatcher));
    }
  }
  
  /**
   * Control whether or not to throw exceptions when accessibility errors are found.
   *
   * @param throwExceptionForErrors {@code true} to throw an {@code AccessibilityViewCheckException}
   * when there is at least one error result. Default: {@code true}.
   */
  public static void setThrowExceptionForErrors(boolean throwExceptionForErrors) {
    initializeValidator();
    validator.setThrowExceptionForErrors(throwExceptionForErrors);
  }

  private static List<AccessibilityViewCheckResult> checkView(View view,
      AccessibilityChecks classChecksAnnotation) {
    /*
     * Accessibility Checking requires the v4 support library. If the support library isn't present,
     * throw a descriptive exception now.
     */
    if (!v4SupportPresenceVerified) {
      try {
        View.class.getClassLoader().loadClass(COMPAT_V4_CLASS_NAME);
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(
            "Accessibility Checking requires the Android support library (v4).\n"
            + "Either include it in the project or disable accessibility checking.");
      }
      v4SupportPresenceVerified = true;
    }

    initializeValidator();
    if (!forVersionSet) {
      if (classChecksAnnotation != null) {
        validator.setCheckPreset(
            convertRoboVersionToA11yTestVersion(classChecksAnnotation.forRobolectricVersion()));
      } else {
        validator.setCheckPreset(AccessibilityCheckPreset.LATEST);
      }
    }
    return validator.checkAndReturnResults(view);
  }

  private static boolean isAccessibilityCheckingEnabled(AccessibilityChecks classChecksAnnotation) {
    boolean checksEnabled = false;

    String checksEnabledString = System.getenv("robolectric.accessibility.enablechecks");
    if (checksEnabledString != null) {
      checksEnabled = checksEnabledString.equals("true");
    }

    /* Allow test arg to enable checking (and override environment variables) */
    checksEnabledString = System.getProperty("robolectric.accessibility.enablechecks");
    if (checksEnabledString != null) {
      checksEnabled = checksEnabledString.equals("true");
    }

    if (classChecksAnnotation != null) {
      checksEnabled = classChecksAnnotation.enabled();
    }

    return checksEnabled;
  }

  private static AccessibilityChecks getAnnotation() {
    AccessibilityChecks classChecksAnnotation = null;
    StackTraceElement[] stack = new Throwable().fillInStackTrace().getStackTrace();
    for (StackTraceElement element : stack) {
      /* Look for annotations on the method or the class */
      Class<?> clazz;
      try {
        clazz = Class.forName(element.getClassName());
        Method method;
        method = clazz.getMethod(element.getMethodName());
        /* Assume the method is void, as that is the case for tests */
        classChecksAnnotation = method.getAnnotation(AccessibilityChecks.class);
        if (classChecksAnnotation == null) {
          classChecksAnnotation = clazz.getAnnotation(AccessibilityChecks.class);
        }
        /* Stop looking when we find an annotation */
        if (classChecksAnnotation != null) {
          break;
        }

        /* If we've crawled up the stack far enough to find the test, stop looking */
        for (Annotation annotation : clazz.getAnnotations()) {
          if (annotation.annotationType().getName().equals("org.junit.Test")) {
            break;
          }
        }
      }
      /* 
       * The reflective calls may throw exceptions if the stack trace elements
       * don't look like junit test methods. In that case we simply go on
       * to the next element
       */
      catch (ClassNotFoundException | SecurityException | NoSuchMethodException e) {}
    }
    return classChecksAnnotation;
  }

  private static void initializeValidator() {
    if (validator == null) {
      validator = new AccessibilityValidator();
      setSuppressingResultMatcher(null);
    }
  }

  private static AccessibilityCheckPreset convertRoboVersionToA11yTestVersion(
      ForRobolectricVersion robolectricVersion) {
    if (robolectricVersion == ForRobolectricVersion.LATEST) {
      return AccessibilityCheckPreset.LATEST;
    }
    AccessibilityCheckPreset preset = AccessibilityCheckPreset.VERSION_1_0_CHECKS;
    if (robolectricVersion.ordinal() >= ForRobolectricVersion.VERSION_3_1.ordinal()) {
      preset = AccessibilityCheckPreset.VERSION_2_0_CHECKS;
    }
    return preset;
  }  
}
