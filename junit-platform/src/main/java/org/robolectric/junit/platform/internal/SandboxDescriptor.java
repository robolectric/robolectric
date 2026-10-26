package org.robolectric.junit.platform.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nullable;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.reporting.FileEntry;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.robolectric.internal.AndroidSandbox;
import org.robolectric.pluginapi.config.ConfigurationStrategy.Configuration;

/**
 * The tests that run in one Robolectric sandbox: one SDK, looper, graphics and resources mode, and
 * class loader configuration. Tests from different classes, JUnit Jupiter and JUnit 4, share the
 * sandbox.
 */
public final class SandboxDescriptor extends AbstractTestDescriptor {
  static final String SEGMENT_TYPE = "sandbox";

  private final SandboxSpec spec;
  @Nullable private Configuration representativeConfiguration;

  SandboxDescriptor(UniqueId uniqueId, SandboxSpec spec) {
    super(uniqueId, spec.displayName());
    this.spec = spec;
  }

  public SandboxSpec getSpec() {
    return spec;
  }

  @Override
  public Type getType() {
    return Type.CONTAINER;
  }

  /**
   * Records the configuration of one of the tests, used to configure the sandbox for code that runs
   * before any test, such as {@code @BeforeAll} methods.
   */
  void offerConfiguration(Configuration configuration) {
    if (representativeConfiguration == null) {
      representativeConfiguration = configuration;
    }
  }

  /** Adds the tests of a Jupiter test plan as children. */
  void addJupiterTests(TestPlan plan) {
    for (TestIdentifier engine : plan.getRoots()) {
      for (TestIdentifier child : plan.getChildren(engine)) {
        addMirror(plan, child, this);
      }
    }
  }

  private void addMirror(TestPlan plan, TestIdentifier identifier, TestDescriptor parent) {
    UniqueId uniqueId = JupiterDelegate.toOuter(identifier.getUniqueIdObject(), getUniqueId());
    TestDescriptor mirror = null;
    for (TestDescriptor child : parent.getChildren()) {
      if (child.getUniqueId().equals(uniqueId)) {
        mirror = child;
      }
    }
    if (mirror == null) {
      mirror = new MirrorDescriptor(uniqueId, identifier);
      parent.addChild(mirror);
    }
    for (TestIdentifier child : plan.getChildren(identifier)) {
      addMirror(plan, child, mirror);
    }
  }

  void execute(ExecutionRequest request, JupiterDelegate jupiter) {
    EngineExecutionListener listener = request.getEngineExecutionListener();
    listener.executionStarted(this);
    AndroidSandbox sandbox;
    try {
      RobolectricRuntime runtime = RobolectricRuntime.get();
      sandbox = runtime.getSandbox(spec);
      if (representativeConfiguration != null) {
        runtime.configureSandbox(sandbox, representativeConfiguration);
      }
    } catch (Throwable t) {
      // Also fail each test class: build tools report failures by class.
      for (TestDescriptor child : getChildren()) {
        listener.executionStarted(child);
        listener.executionFinished(child, TestExecutionResult.failed(t));
      }
      listener.executionFinished(this, TestExecutionResult.failed(t));
      return;
    }
    TestExecutionResult result;
    try {
      AtomicReference<Throwable> failure = new AtomicReference<>();
      sandbox.runOnMainThreadWithClassLoader(() -> executeJupiterTests(request, jupiter, failure));
      for (TestDescriptor child : new ArrayList<>(getChildren())) {
        if (!(child instanceof CompatClassDescriptor)) {
          continue;
        }
        CompatClassDescriptor compatClass = (CompatClassDescriptor) child;
        if (request.getCancellationToken().isCancellationRequested()) {
          listener.executionSkipped(child, "Execution cancelled");
        } else {
          sandbox.runOnMainThreadWithClassLoader(
              () -> JUnit4Compat.execute(compatClass, sandbox, listener));
        }
      }
      result =
          failure.get() == null
              ? TestExecutionResult.successful()
              : TestExecutionResult.failed(failure.get());
    } catch (Throwable t) {
      result = TestExecutionResult.failed(t);
    }
    listener.executionFinished(this, result);
  }

  /**
   * Runs the Jupiter tests of this sandbox. Must be called on the sandbox's main thread with the
   * sandbox class loader as the context class loader, so that Jupiter loads the test classes
   * through the sandbox.
   */
  private void executeJupiterTests(
      ExecutionRequest request, JupiterDelegate jupiter, AtomicReference<Throwable> failure) {
    Map<UniqueId, TestDescriptor> mirrors = new HashMap<>();
    List<DiscoverySelector> selectors = new ArrayList<>();
    for (TestDescriptor child : getChildren()) {
      if (child instanceof MirrorDescriptor) {
        collectMirrors(child, mirrors, selectors);
      }
    }
    if (selectors.isEmpty()) {
      return;
    }
    EngineExecutionListener listener = request.getEngineExecutionListener();
    // Descriptors whose children Jupiter should have run: started and finished successfully.
    Set<UniqueId> succeeded = new HashSet<>();
    Set<UniqueId> reported = new HashSet<>();
    jupiter.execute(
        selectors,
        new TestExecutionListener() {
          @Override
          public void dynamicTestRegistered(TestIdentifier identifier) {
            mirror(identifier);
          }

          @Override
          public void executionSkipped(TestIdentifier identifier, String reason) {
            if (!isRoot(identifier)) {
              reported.add(toOuter(identifier.getUniqueIdObject()));
              listener.executionSkipped(mirror(identifier), reason);
            }
          }

          @Override
          public void executionStarted(TestIdentifier identifier) {
            if (!isRoot(identifier)) {
              reported.add(toOuter(identifier.getUniqueIdObject()));
              listener.executionStarted(mirror(identifier));
            }
          }

          @Override
          public void executionFinished(TestIdentifier identifier, TestExecutionResult result) {
            if (isRoot(identifier)) {
              result.getThrowable().ifPresent(failure::set);
            } else {
              if (result.getStatus() == TestExecutionResult.Status.SUCCESSFUL) {
                succeeded.add(toOuter(identifier.getUniqueIdObject()));
              }
              listener.executionFinished(mirror(identifier), result);
            }
          }

          @Override
          public void reportingEntryPublished(TestIdentifier identifier, ReportEntry entry) {
            listener.reportingEntryPublished(
                isRoot(identifier) ? SandboxDescriptor.this : mirror(identifier), entry);
          }

          @Override
          public void fileEntryPublished(TestIdentifier identifier, FileEntry file) {
            listener.fileEntryPublished(
                isRoot(identifier) ? SandboxDescriptor.this : mirror(identifier), file);
          }

          private boolean isRoot(TestIdentifier identifier) {
            return !identifier.getParentIdObject().isPresent();
          }

          // Returns the descriptor of the identifier, registering it if it is a dynamic test.
          private TestDescriptor mirror(TestIdentifier identifier) {
            UniqueId uniqueId = toOuter(identifier.getUniqueIdObject());
            TestDescriptor mirror = mirrors.get(uniqueId);
            if (mirror == null) {
              TestDescriptor parent =
                  identifier
                      .getParentIdObject()
                      .map(id -> mirrors.get(toOuter(id)))
                      .orElse(SandboxDescriptor.this);
              mirror = new MirrorDescriptor(uniqueId, identifier);
              parent.addChild(mirror);
              mirrors.put(uniqueId, mirror);
              listener.dynamicTestRegistered(mirror);
            }
            return mirror;
          }
        },
        request.getCancellationToken());

    // A planned test that the sandboxed discovery did not find is reported, not dropped.
    if (failure.get() == null) {
      for (TestDescriptor child : new ArrayList<>(getChildren())) {
        if (child instanceof MirrorDescriptor) {
          reportUnexecuted(child, reported, succeeded, listener);
        }
      }
    }
  }

  private UniqueId toOuter(UniqueId jupiterId) {
    return JupiterDelegate.toOuter(jupiterId, getUniqueId());
  }

  private void collectMirrors(
      TestDescriptor descriptor,
      Map<UniqueId, TestDescriptor> mirrors,
      List<DiscoverySelector> selectors) {
    mirrors.put(descriptor.getUniqueId(), descriptor);
    if (descriptor.getChildren().isEmpty()) {
      // Selecting the leaves keeps filtering applied by the launcher, e.g. by tag or method name.
      selectors.add(
          DiscoverySelectors.selectUniqueId(
              JupiterDelegate.toJupiter(descriptor.getUniqueId(), getUniqueId())));
    } else {
      for (TestDescriptor child : descriptor.getChildren()) {
        collectMirrors(child, mirrors, selectors);
      }
    }
  }

  private static void reportUnexecuted(
      TestDescriptor descriptor,
      Set<UniqueId> reported,
      Set<UniqueId> succeeded,
      EngineExecutionListener listener) {
    UniqueId uniqueId = descriptor.getUniqueId();
    if (!reported.contains(uniqueId)) {
      listener.executionStarted(descriptor);
      listener.executionFinished(
          descriptor,
          TestExecutionResult.failed(
              new IllegalStateException(uniqueId + " was not found when running in the sandbox")));
    } else if (succeeded.contains(uniqueId)) {
      for (TestDescriptor child : new ArrayList<>(descriptor.getChildren())) {
        reportUnexecuted(child, reported, succeeded, listener);
      }
    }
  }
}
