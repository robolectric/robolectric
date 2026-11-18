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
        // A parameter set is not the class: its CompatSuiteDescriptor parent is.
        unit.name == null ? ClassSource.from(unit.testClass) : null);
    this.unit = unit;
  }

  @Override
  public Type getType() {
    return Type.CONTAINER;
  }

  @Override
  public String getLegacyReportingName() {
    return unit.name == null ? unit.testClass.getName() : unit.name;
  }
}
