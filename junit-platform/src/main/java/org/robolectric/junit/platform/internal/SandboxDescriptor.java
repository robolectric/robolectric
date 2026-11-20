package org.robolectric.junit.platform.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
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
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.reporting.FileEntry;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.robolectric.internal.AndroidSandbox;
import org.robolectric.pluginapi.config.ConfigurationStrategy.Configuration;

/**
 * The tests that run in one Robolectric sandbox: one SDK, looper, graphics and resources mode, and
 * class loader configuration. Tests from different classes share the sandbox.
 *
 * <p>The test plan reports the tests by class, so sandboxes are not part of it: the JUnit Jupiter
 * classes of a sandbox report their tests and events under their {@link TestClassDescriptor}.
 */
public final class SandboxDescriptor extends AbstractTestDescriptor {
  static final String SEGMENT_TYPE = "sandbox";

  private final SandboxSpec spec;
  private final Set<MethodSource> testsNamedWithSdk = new HashSet<>();
  @Nullable private Configuration representativeConfiguration;
  // The Jupiter classes of this sandbox, top-level and nested, each before its nested classes.
  private final Map<TestDescriptor, TestClassDescriptor> classes = new LinkedHashMap<>();

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

  /**
   * Appends this sandbox's SDK to the names of {@code test} and of the tests it contains, as {@code
   * RobolectricTestRunner} does for tests that run on several SDKs, so that reports, which don't
   * show the sandbox containers, can tell them apart.
   */
  void nameWithSdk(MethodSource test) {
    testsNamedWithSdk.add(test);
  }

  private String variantMarker(TestIdentifier identifier, TestDescriptor parent) {
    if (parent instanceof MirrorDescriptor mirror && !mirror.getVariantMarker().isEmpty()) {
      return mirror.getVariantMarker();
    }
    TestSource source = identifier.getSource().orElse(null);
    return testsNamedWithSdk.contains(source) ? "[" + spec.getApiLevel() + "]" : "";
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
      mirror = new MirrorDescriptor(uniqueId, identifier, variantMarker(identifier, parent));
      parent.addChild(mirror);
    }
    for (TestIdentifier child : plan.getChildren(identifier)) {
      addMirror(plan, child, mirror);
    }
  }

  /**
   * Reports the tests of {@code jupiterClass}, a class of this sandbox, under {@code parent}, and
   * those of its {@code @Nested} classes under the corresponding children of {@code parent}.
   */
  void reportUnder(TestDescriptor jupiterClass, TestClassDescriptor parent) {
    classes.put(jupiterClass, parent);
    parent.sandboxes.add(this);
    for (TestDescriptor child : jupiterClass.getChildren()) {
      if (TestClassDescriptor.isNestedClass(child)) {
        reportUnder(child, parent.nestedClass(child));
      } else {
        parent.addChild(child);
      }
    }
  }

  /** The classes of this sandbox, each nested class before its enclosing class. */
  List<TestClassDescriptor> classes() {
    List<TestClassDescriptor> result = new ArrayList<>(classes.values());
    Collections.reverse(result);
    return result;
  }

  void execute(ExecutionRequest request, JupiterDelegate jupiter) {
    EngineExecutionListener listener = request.getEngineExecutionListener();
    AndroidSandbox sandbox;
    try {
      RobolectricRuntime runtime = RobolectricRuntime.get();
      sandbox = runtime.getSandbox(spec);
      if (representativeConfiguration != null) {
        runtime.configureSandbox(sandbox, representativeConfiguration);
      }
    } catch (Throwable t) {
      // Like RobolectricTestRunner, fail each test, e.g. with "API level 23 is not available".
      for (TestDescriptor jupiterClass : getChildren()) {
        failTests(jupiterClass, t, listener);
      }
      return;
    }
    AtomicReference<Throwable> failure = new AtomicReference<>();
    try {
      sandbox.runOnMainThreadWithClassLoader(() -> executeJupiterTests(request, jupiter, failure));
    } catch (Throwable t) {
      failure.set(t);
    }
    if (failure.get() != null) {
      for (TestClassDescriptor testClassDescriptor : classes.values()) {
        testClassDescriptor.fail(failure.get());
      }
    }
  }

  private void failTests(
      TestDescriptor descriptor, Throwable failure, EngineExecutionListener listener) {
    TestClassDescriptor testClassDescriptor = classes.get(descriptor);
    if (testClassDescriptor != null) {
      // Finished once all of its sandboxes ran it.
      testClassDescriptor.start(listener);
      for (TestDescriptor child : descriptor.getChildren()) {
        failTests(child, failure, listener);
      }
      return;
    }
    listener.executionStarted(descriptor);
    if (descriptor.getChildren().isEmpty()) {
      listener.executionFinished(descriptor, TestExecutionResult.failed(failure));
      return;
    }
    for (TestDescriptor child : descriptor.getChildren()) {
      failTests(child, failure, listener);
    }
    listener.executionFinished(descriptor, TestExecutionResult.successful());
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
              skip(mirror(identifier), reason, listener);
            }
          }

          @Override
          public void executionStarted(TestIdentifier identifier) {
            if (!isRoot(identifier)) {
              reported.add(toOuter(identifier.getUniqueIdObject()));
              TestDescriptor mirror = mirror(identifier);
              TestClassDescriptor testClassDescriptor = classes.get(mirror);
              if (testClassDescriptor == null) {
                listener.executionStarted(mirror);
              } else {
                testClassDescriptor.start(listener);
              }
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
              TestDescriptor mirror = mirror(identifier);
              TestClassDescriptor testClassDescriptor = classes.get(mirror);
              if (testClassDescriptor == null) {
                listener.executionFinished(mirror, result);
              } else {
                // Finished once all of its sandboxes ran it.
                result.getThrowable().ifPresent(testClassDescriptor::fail);
              }
            }
          }

          @Override
          public void reportingEntryPublished(TestIdentifier identifier, ReportEntry entry) {
            listener.reportingEntryPublished(reported(identifier), entry);
          }

          @Override
          public void fileEntryPublished(TestIdentifier identifier, FileEntry file) {
            listener.fileEntryPublished(reported(identifier), file);
          }

          // The descriptor that events of the identifier are reported on.
          private TestDescriptor reported(TestIdentifier identifier) {
            if (isRoot(identifier)) {
              return request.getRootTestDescriptor();
            }
            TestDescriptor mirror = mirror(identifier);
            return classes.containsKey(mirror) ? classes.get(mirror) : mirror;
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
              mirror =
                  new MirrorDescriptor(uniqueId, identifier, variantMarker(identifier, parent));
              (classes.containsKey(parent) ? classes.get(parent) : parent).addChild(mirror);
              mirrors.put(uniqueId, mirror);
              listener.dynamicTestRegistered(mirror);
            }
            return mirror;
          }
        });

    // A planned test that the sandboxed discovery did not find is reported, not dropped.
    if (failure.get() == null) {
      for (Map.Entry<TestDescriptor, TestClassDescriptor> entry : classes.entrySet()) {
        UniqueId uniqueId = entry.getKey().getUniqueId();
        if (!reported.contains(uniqueId)) {
          entry.getValue().start(listener);
          entry
              .getValue()
              .fail(
                  new IllegalStateException(
                      uniqueId + " was not found when running in the sandbox"));
        } else if (succeeded.contains(uniqueId)) {
          for (TestDescriptor child : new ArrayList<>(entry.getKey().getChildren())) {
            reportUnexecuted(child, reported, succeeded, listener);
          }
        }
      }
    }
  }

  private void skip(TestDescriptor descriptor, String reason, EngineExecutionListener listener) {
    TestClassDescriptor testClassDescriptor = classes.get(descriptor);
    if (testClassDescriptor == null) {
      listener.executionSkipped(descriptor, reason);
    } else {
      testClassDescriptor.start(listener);
      for (TestDescriptor child : descriptor.getChildren()) {
        skip(child, reason, listener);
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

  private void reportUnexecuted(
      TestDescriptor descriptor,
      Set<UniqueId> reported,
      Set<UniqueId> succeeded,
      EngineExecutionListener listener) {
    if (classes.containsKey(descriptor)) {
      return; // Checked as a class of this sandbox.
    }
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
