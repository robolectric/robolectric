package org.robolectric.junit.platform.internal;

import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.ClassSource;

/** A class whose Robolectric configuration is invalid; reported as a failed test. */
final class InvalidClassDescriptor extends AbstractTestDescriptor {
  final Throwable error;

  InvalidClassDescriptor(UniqueId uniqueId, Class<?> testClass, Throwable error) {
    super(uniqueId, testClass.getSimpleName(), ClassSource.from(testClass));
    this.error = error;
  }

  @Override
  public Type getType() {
    return Type.TEST;
  }
}
