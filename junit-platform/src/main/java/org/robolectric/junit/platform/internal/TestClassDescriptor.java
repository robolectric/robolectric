package org.robolectric.junit.platform.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;

/**
 * A top-level test class, containing its tests from every sandbox it runs in, so that build tools
 * and IDEs report it once, like {@code RobolectricTestRunner} does, instead of once per sandbox.
 *
 * <p>It is started when the first of its sandboxes runs it and finished after the last one.
 */
final class TestClassDescriptor extends AbstractTestDescriptor {
  static final String SEGMENT_TYPE = "class";

  /** The sandboxes that run this class. */
  final List<SandboxDescriptor> sandboxes = new ArrayList<>();

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
    start(listener);
    listener.executionFinished(
        this,
        failure == null ? TestExecutionResult.successful() : TestExecutionResult.failed(failure));
  }
}
