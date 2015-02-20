package org.robolectric.util;

/**
 * Logger for Robolectric. For now, it simply prints messages to stdout. Logging
 * can be enabled by setting the property: robolectric.logging.enabled = true.
 */
public class Logger {

  /**
   * Log a debug message.
   *
   * @param message Message text.
   * @param args    Message arguments.
   */
  public static void debug(String message, Object... args) {
    if (Boolean.getBoolean("robolectric.logging.enabled")) {
      System.out.println("DEBUG: " + String.format(message, args));
    }
  }
}
