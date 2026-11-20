package org.robolectric.junit.platform.internal;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;

/**
 * A test class, top-level or {@code @Nested}, containing its tests from every sandbox it runs in,
 * so that build tools and IDEs report it once, like {@code RobolectricTestRunner} does, instead of
 * once per sandbox.
 *
 * <p>It is started when the first of its sandboxes runs it and finished after the last one.
 */
final class TestClassDescriptor extends AbstractTestDescriptor {
  static final String SEGMENT_TYPE = "class";
  static final String NESTED_SEGMENT_TYPE = "nested-class";

  /** The sandboxes that run this class. */
  final List<SandboxDescriptor> sandboxes = new ArrayList<>();

  private final Map<String, TestClassDescriptor> nestedClasses = new LinkedHashMap<>();

  private final Set<TestTag> tags;
  private final String legacyReportingName;
  private boolean started;
  @Nullable private Throwable failure;

  TestClassDescriptor(UniqueId uniqueId, TestDescriptor jupiterClass) {
    super(uniqueId, jupiterClass.getDisplayName(), jupiterClass.getSource().orElse(null));
    this.tags = jupiterClass.getTags();
    this.legacyReportingName = jupiterClass.getLegacyReportingName();
  }

  @Override
  public Type getType() {
    return Type.CONTAINER;
  }

  @Override
  public Set<TestTag> getTags() {
    return tags;
  }

  @Override
  public String getLegacyReportingName() {
    return legacyReportingName;
  }

  /** Whether {@code descriptor}, a Jupiter descriptor, is a {@code @Nested} class. */
  static boolean isNestedClass(TestDescriptor descriptor) {
    return descriptor.getUniqueId().getLastSegment().getType().equals(NESTED_SEGMENT_TYPE);
  }

  /** Returns the child that reports {@code jupiterClass}, a {@code @Nested} class of this class. */
  TestClassDescriptor nestedClass(TestDescriptor jupiterClass) {
    String name = jupiterClass.getUniqueId().getLastSegment().getValue();
    return nestedClasses.computeIfAbsent(
        name,
        unused -> {
          TestClassDescriptor descriptor =
              new TestClassDescriptor(
                  getUniqueId().append(NESTED_SEGMENT_TYPE, name), jupiterClass);
          addChild(descriptor);
          return descriptor;
        });
  }

  void start(EngineExecutionListener listener) {
    if (!started) {
      started = true;
      listener.executionStarted(this);
    }
  }

  /** Records a failure of the class itself, e.g. of a {@code @BeforeAll} method, in a sandbox. */
  void fail(Throwable throwable) {
    if (failure == null) {
      failure = throwable;
    }
  }

  void finish(EngineExecutionListener listener) {
    if (!started && getParent().orElse(null) instanceof TestClassDescriptor) {
      // Like Jupiter, don't report a nested class that never ran, e.g. because @BeforeAll failed.
      return;
    }
    start(listener);
    listener.executionFinished(
        this,
        failure == null ? TestExecutionResult.successful() : TestExecutionResult.failed(failure));
  }
}
