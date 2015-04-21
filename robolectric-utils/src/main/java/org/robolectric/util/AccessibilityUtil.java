package org.robolectric.util;

import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheck;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckPreset;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResultUtils;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityViewCheck;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityViewCheckResult;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityViewHierarchyCheck;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult.AccessibilityCheckResultType;
import com.google.android.apps.common.testing.accessibility.framework.TouchTargetSizeViewCheck;

import android.view.View;

import org.robolectric.annotation.AccessibilityChecks;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Utility class for checking Views for accessibility
 */
public class AccessibilityUtil {
  private AccessibilityUtil() {}

  /**
   * Check a {@code View} for accessibility. Only performs checks if 
   * accessibility is enabled using an {@link AccessibilityChecks} annotation
   * or the environment variable {@code a11y_checks_enabled} is set to 
   * {@code true}. {@link AccessibilityChecks} values override those from 
   * environment variables.
   *
   * @param view The {@code View} to examine
   * @return {@code false} if accessibility checks are enabled and a problem
   * was found. {@code true} otherwise.
   */
  public static boolean passesAccessibilityChecksIfEnabled(View view) {
    boolean checksEnabled = false;

    /* Pull defaults from environment variables */
    String checksEnabledString = System.getenv("robolectric.accessibility.enablechecks");
    if (checksEnabledString != null) {
      checksEnabled = checksEnabledString.equals("true");
    }

    /* Update values from annotations in the stack, if any */
    StackTraceElement[] stack = new Throwable().fillInStackTrace().getStackTrace();
    for (StackTraceElement element : stack) {
      /* Look for annotations on the method or the class */
      Class<?> clazz;
      try {
        clazz = Class.forName(element.getClassName());
        Method method;
        method = clazz.getMethod(element.getMethodName());
        /* Assume the method is void, as that is the case for tests */
        AccessibilityChecks classChecksAnnotation = method.getAnnotation(AccessibilityChecks.class);
        if (classChecksAnnotation == null) {
          classChecksAnnotation = clazz.getAnnotation(AccessibilityChecks.class);
        }
        if (classChecksAnnotation != null) {
          checksEnabled = classChecksAnnotation.enabled();
          break;
        }
        /* If we've crawled up the stack far enough to find the test, stop looking */
        if (clazz.getAnnotation(org.junit.Test.class) != null) {
          break;
        }
      }
      /* 
       * The reflective calls may throw exceptions if the stack trace elements
       * don't look like junit test methods. In that case we simply go on
       * to the next element
       */
      catch (ClassNotFoundException | SecurityException | NoSuchMethodException e) {}
    }

    if (!checksEnabled) {
      return true;
    }

    return passesAccessibilityChecks(view, System.out);
  }

  /**
   * Check a {@code View} for accessibility. Prints details of issues
   * found to System.out.
   *
   * @param view The {@code View} to examine
   * @param printStream A stream to print error messages to
   * @return {@code false} if accessibility checks are enabled and a problem
   * was found. {@code true} otherwise.
   */
  public static boolean passesAccessibilityChecks(View view,
                                                  PrintStream printStream) {
    /* 
     * For the initial release, it's fine to ignore the forRobolectricVersion 
     * because there is only one version. These sets need to be adjusted in 
     * later versions to take forVersion into account
     */
    Set<AccessibilityCheck> viewChecks = new HashSet<>(AccessibilityCheckPreset
        .getAllChecksForPreset(AccessibilityCheckPreset.VIEW_CHECKS));
    /* Robolectric Views are reported as 0x0, so skip the test target size check */
    Iterator<AccessibilityCheck> viewCheckIterator = viewChecks.iterator();
    while (viewCheckIterator.hasNext()) {
      AccessibilityCheck viewCheck = viewCheckIterator.next();
      if (viewCheck instanceof TouchTargetSizeViewCheck) {
        viewCheckIterator.remove();
      }
    }
    
    Set<AccessibilityCheck> viewHierarchyChecks = AccessibilityCheckPreset
        .getAllChecksForPreset(AccessibilityCheckPreset.VIEW_HIERARCHY_CHECKS);

    List<AccessibilityViewCheckResult> results = new LinkedList<>();

    for (AccessibilityCheck check : viewChecks) {
      results.addAll(((AccessibilityViewCheck) check).runCheckOnView(view));
    }

    for (AccessibilityCheck check : viewHierarchyChecks) {
      results.addAll(((AccessibilityViewHierarchyCheck) check)
          .runCheckOnViewHierarchy(view));
    }

    // Throw an exception for the first error
    List<AccessibilityViewCheckResult> errors = AccessibilityCheckResultUtils.getResultsForType(
        results, AccessibilityCheckResultType.ERROR);
    if (printStream != null) {
      for (AccessibilityViewCheckResult error : errors) {
        printStream.println("Accessibility Issue: " + error.getMessage().toString());
      }
    }
    return (errors.size() == 0);
  }
}