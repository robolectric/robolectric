package org.robolectric.util.inject;

public class InjectionException extends RuntimeException {
  public InjectionException(Class<?> clazz, String message, Throwable cause) {
    super(clazz.getName() + ": " + message, cause);
  }

  public InjectionException(Injector.Key<?> key, String message, Throwable cause) {
    super(key + ": " + message, cause);
  }

  public InjectionException(Class<?> clazz, String message) {
    super(clazz.getName() + ": " + message);
  }
  public InjectionException(Injector.Key<?> key, String message) {
    super(key + ": " + message);
  }

  public InjectionException(Class<?> clazz, Throwable cause) {
    super(clazz.getName() + ": failed to inject", cause);
  }

  public InjectionException(Injector.Key<?> key, Throwable cause) {
    super(key + ": failed to inject", cause);
  }
}
