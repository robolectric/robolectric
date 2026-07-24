package org.robolectric.internal.bytecode;

import java.io.InputStream;
import javax.annotation.Nullable;

/**
 * Thread-local class byte source hook used by framework integrations with custom classloaders.
 *
 * <p>The sandbox classloader still owns class definition/instrumentation. This hook only lets it
 * ask for alternate source bytes (for example, framework-transformed test classes) on the current
 * execution thread.
 */
public final class ClassBytesSource {
  private static final ThreadLocal<Provider> CURRENT_PROVIDER = new ThreadLocal<>();

  private ClassBytesSource() {}

  public interface Provider {
    @Nullable
    InputStream openClassBytes(String classResName);
  }

  @Nullable
  public static Provider setProvider(@Nullable Provider provider) {
    Provider prior = CURRENT_PROVIDER.get();
    if (provider == null) {
      CURRENT_PROVIDER.remove();
    } else {
      CURRENT_PROVIDER.set(provider);
    }
    return prior;
  }

  public static void restoreProvider(@Nullable Provider provider) {
    if (provider == null) {
      CURRENT_PROVIDER.remove();
    } else {
      CURRENT_PROVIDER.set(provider);
    }
  }

  @Nullable
  static InputStream openClassBytes(String classResName) {
    Provider provider = CURRENT_PROVIDER.get();
    if (provider == null) {
      return null;
    }
    return provider.openClassBytes(classResName);
  }
}
