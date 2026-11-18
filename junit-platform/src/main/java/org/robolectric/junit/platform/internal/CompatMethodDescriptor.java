package org.robolectric.junit.platform.internal;

import java.util.Set;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.MethodSource;

/** A JUnit 4 test method, for one SDK. */
final class CompatMethodDescriptor extends AbstractTestDescriptor {
  final JUnit4Compat.PlannedTest planned;

  CompatMethodDescriptor(UniqueId uniqueId, JUnit4Compat.PlannedTest planned) {
    super(
        uniqueId,
        planned.description.getMethodName(),
        MethodSource.from(planned.unit.testClass, planned.method.getMethod()));
    this.planned = planned;
  }

  @Override
  public Type getType() {
    return Type.TEST;
  }

  @Override
  public Set<TestTag> getTags() {
    return JUnit4Compat.categoryTags(planned.unit.testClass, planned.method.getMethod());
  }

  @Override
  public String getLegacyReportingName() {
    return planned.description.getMethodName();
  }
}
