package org.robolectric.util;

/**
 * Logger for Robolectric. For now, it simply prints messages to stdout.
 *
 * <p>
 * Logging can be enabled by setting the property: {@code robolectric.logging.enabled = true}.
 * </p>
 */
public class Logger {
  private static final String LOGGING_ENABLED = "robolectric.logging.enabled";

  /**
   * Log an info message.
   *
   * @param message Message text.
   * @param args    Message arguments.
   */
  public static void info(String message, Object... args) {
    if (Boolean.getBoolean(LOGGING_ENABLED)) {
      System.out.println(String.format(message, args));
    }
  }

  /**
   * Log an error message.
   *
   * @param message Message text.
   * @param args    Message arguments.
   */
  public static void error(String message, Object... args) {
    System.err.println(String.format(message, args));
  }

  /**
   * Log a debug message.
   *
   * @param message Message text.
   * @param args    Message arguments.
   */
  public static void debug(String message, Object... args) {
    if (Boolean.getBoolean(LOGGING_ENABLED)) {
      System.out.println("DEBUG: " + String.format(message, args));
    }
  }
}
