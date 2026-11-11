package org.robolectric.junit.platform.internal;

import java.util.Map;
import java.util.WeakHashMap;
import javax.annotation.Nullable;
import org.robolectric.internal.AndroidSandbox;

/** Finds the sandbox that loaded a class, for code that only sees the sandboxed test class. */
public final class SandboxRegistry {
  private static final Map<ClassLoader, AndroidSandbox> SANDBOXES = new WeakHashMap<>();

  private SandboxRegistry() {}

  static synchronized void register(AndroidSandbox sandbox) {
    SANDBOXES.put(sandbox.getRobolectricClassLoader(), sandbox);
  }

  /** Returns the sandbox whose class loader loaded {@code clazz}, or null. */
  @Nullable
  public static synchronized AndroidSandbox forClass(Class<?> clazz) {
    return SANDBOXES.get(clazz.getClassLoader());
  }
}
