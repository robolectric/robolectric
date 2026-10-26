package org.robolectric.junit.platform;

import org.junit.platform.engine.FilterResult;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.PostDiscoveryFilter;
import org.robolectric.junit.platform.internal.ClaimRegistry;

/**
 * Removes the test classes run by the {@code robolectric} engine from the test plans of other
 * engines.
 *
 * <p>JUnit Jupiter discovers every class with {@code @Test} methods, including the ones that use
 * {@link org.robolectric.junit.jupiter.RobolectricExtension}, and JUnit Vintage discovers the JUnit
 * 4 classes that the robolectric engine runs; without this filter they would run twice. Registered
 * with {@link java.util.ServiceLoader}.
 */
public final class RobolectricClaimFilter implements PostDiscoveryFilter {
  @Override
  public FilterResult apply(TestDescriptor descriptor) {
    if (descriptor
        .getUniqueId()
        .getEngineId()
        .filter(RobolectricTestEngine.ENGINE_ID::equals)
        .isPresent()) {
      return FilterResult.included("Run by the robolectric engine");
    }
    String className = className(descriptor.getSource().orElse(null));
    if (className != null && ClaimRegistry.isClaimed(className)) {
      return FilterResult.excluded("Run by the robolectric engine");
    }
    return FilterResult.included("Not a Robolectric test class");
  }

  private static String className(TestSource source) {
    if (source instanceof ClassSource) {
      return ((ClassSource) source).getClassName();
    }
    if (source instanceof MethodSource) {
      return ((MethodSource) source).getClassName();
    }
    return null;
  }
}
