package org.robolectric;

public interface Plugin {
  int DEFAULT_PLUGIN_PRIORITY = 0;

  float getPriority();

  class UnsuitablePluginException extends RuntimeException {

    public UnsuitablePluginException() {
    }

    public UnsuitablePluginException(String message) {
      super(message);
    }

    public UnsuitablePluginException(String message, Throwable cause) {
      super(message, cause);
    }

    public UnsuitablePluginException(Throwable cause) {
      super(cause);
    }
  }
}
