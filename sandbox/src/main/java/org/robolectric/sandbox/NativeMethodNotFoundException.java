package org.robolectric.sandbox;

/**
 * Thrown when a particular Robolectric native method cannot be found.
 *
 * <p>Instrumented native methods throw this exception when the NativeCallHandler is set to
 * throw-on-native and that the dedicated method signature has not been exempted.
 */
public class NativeMethodNotFoundException extends RuntimeException {

  public NativeMethodNotFoundException() {
    super();
  }

  public NativeMethodNotFoundException(String message) {
    super(message);
  }
}
