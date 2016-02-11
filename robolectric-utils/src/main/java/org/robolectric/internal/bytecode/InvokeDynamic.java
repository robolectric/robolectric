package org.robolectric.internal.bytecode;

import org.robolectric.util.JavaVersion;

public class InvokeDynamic {
  public static final boolean ENABLED = useInvokeDynamic();

  private static final String ENABLE_INVOKEDYNAMIC = "robolectric.invokedynamic.enable";
  // We currently crash on versions earlier than 8u40 because of a bug in the C2 compiler.
  // This seems to be the bug http://bugs.java.com/view_bug.do?bug_id=8059556 but I have been
  // unable to pinpoint exactly why this affects us.
  private static final String INVOKEDYNAMIC_MINIMUM_VERSION = "1.8.0_40";

  private static boolean useInvokeDynamic() {
    String property = System.getProperty(ENABLE_INVOKEDYNAMIC);
    if (property != null) {
      return Boolean.valueOf(property);
    } else {
      JavaVersion javaVersion = new JavaVersion(System.getProperty("java.version"));
      return javaVersion.compareTo(new JavaVersion(INVOKEDYNAMIC_MINIMUM_VERSION)) >= 0;
    }
  }
}
