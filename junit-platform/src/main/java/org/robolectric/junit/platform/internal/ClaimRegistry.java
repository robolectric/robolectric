package org.robolectric.junit.platform.internal;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The test classes the robolectric engine is running in the current discovery, so that other
 * engines that would also pick them up (such as JUnit Jupiter) leave them alone.
 */
public final class ClaimRegistry {
  private static final Set<String> CLAIMED = ConcurrentHashMap.newKeySet();

  private ClaimRegistry() {}

  /** Called when the launcher starts a new discovery. */
  public static void reset() {
    CLAIMED.clear();
  }

  static void claim(Class<?> testClass) {
    CLAIMED.add(testClass.getName());
  }

  /** Whether {@code className}, or a class enclosing it, is run by the robolectric engine. */
  public static boolean isClaimed(String className) {
    String name = className;
    while (true) {
      if (CLAIMED.contains(name)) {
        return true;
      }
      int dollar = name.lastIndexOf('$');
      if (dollar < 0) {
        return false;
      }
      name = name.substring(0, dollar);
    }
  }
}
