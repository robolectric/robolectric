package org.robolectric.internal;

import java.util.Properties;
import org.robolectric.util.inject.Injector;

/**
 * The injector of runners that don't bring their own, shared with Robolectric's JUnit Platform
 * engine so that JUnit 4 and JUnit Jupiter tests in the same JVM share one {@link SandboxManager},
 * and therefore sandboxes. Has no JUnit dependency.
 */
public final class DefaultInjector {
  private static final Injector INSTANCE = builder().build();

  /** Returns a builder for an injector configured like the default one. */
  public static Injector.Builder builder() {
    return new Injector.Builder().bind(Properties.class, System.getProperties());
  }

  /** Returns the JVM-wide default injector. */
  public static Injector get() {
    return INSTANCE;
  }

  private DefaultInjector() {}
}
