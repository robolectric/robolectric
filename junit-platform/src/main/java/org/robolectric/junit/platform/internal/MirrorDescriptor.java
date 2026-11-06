package org.robolectric.junit.platform.internal;

import java.util.Optional;
import java.util.Set;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.launcher.TestIdentifier;

/**
 * A test or container of the JUnit Jupiter engine, re-parented under a {@link SandboxDescriptor}.
 *
 * <p>The robolectric engine runs Jupiter through the public Launcher API, so it only sees {@link
 * TestIdentifier}s; this descriptor exposes them as part of the robolectric engine's test plan.
 */
final class MirrorDescriptor extends AbstractTestDescriptor {
  private final Type type;
  private final Set<TestTag> tags;
  private final String legacyReportingName;
  private final boolean mayRegisterTests;

  MirrorDescriptor(UniqueId uniqueId, TestIdentifier identifier) {
    super(uniqueId, identifier.getDisplayName(), identifier.getSource().orElse(null));
    this.type = identifier.getType();
    this.tags = identifier.getTags();
    this.legacyReportingName = identifier.getLegacyReportingName();
    this.mayRegisterTests = isDynamicContainer(identifier.getUniqueIdObject());
  }

  @Override
  public Type getType() {
    return type;
  }

  @Override
  public Set<TestTag> getTags() {
    return tags;
  }

  @Override
  public String getLegacyReportingName() {
    return legacyReportingName;
  }

  @Override
  public boolean mayRegisterTests() {
    return mayRegisterTests;
  }

  /** Whether Jupiter adds the children of this descriptor while running it. */
  static boolean isDynamicContainer(UniqueId uniqueId) {
    Optional<UniqueId.Segment> last = Optional.ofNullable(uniqueId.getLastSegment());
    return last.map(UniqueId.Segment::getType)
        .filter(
            type ->
                type.equals("test-template")
                    || type.equals("test-factory")
                    || type.equals("class-template")
                    || type.equals("dynamic-container"))
        .isPresent();
  }
}
