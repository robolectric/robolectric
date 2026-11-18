package org.robolectric.junit.platform.internal;

import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.ClassSource;

/**
 * A JUnit 4 test class in a sandbox whose runner splits it into several {@link
 * CompatClassDescriptor}s, one per parameter set.
 */
final class CompatSuiteDescriptor extends AbstractTestDescriptor {
  private final Class<?> testClass;

  CompatSuiteDescriptor(UniqueId uniqueId, Class<?> testClass) {
    super(uniqueId, testClass.getSimpleName(), ClassSource.from(testClass));
    this.testClass = testClass;
  }

  @Override
  public Type getType() {
    return Type.CONTAINER;
  }

  @Override
  public String getLegacyReportingName() {
    return testClass.getName();
  }
}
