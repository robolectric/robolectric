package org.robolectric.junit.platform.internal;

import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.ClassSource;

/** A JUnit 4 test class (or one parameter set of it) in a sandbox. */
final class CompatClassDescriptor extends AbstractTestDescriptor {
  final JUnit4Compat.Unit unit;

  CompatClassDescriptor(UniqueId uniqueId, JUnit4Compat.Unit unit) {
    super(
        uniqueId,
        unit.name == null ? unit.testClass.getSimpleName() : unit.name,
        ClassSource.from(unit.testClass));
    this.unit = unit;
  }

  @Override
  public Type getType() {
    return Type.CONTAINER;
  }

  @Override
  public String getLegacyReportingName() {
    return unit.testClass.getName();
  }
}
