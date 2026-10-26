package org.robolectric.junit.platform;

import java.util.ArrayList;
import java.util.Optional;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.robolectric.junit.platform.internal.Planner;
import org.robolectric.junit.platform.internal.Selection;

/**
 * The {@code robolectric} JUnit Platform test engine, which runs JUnit Jupiter test classes that
 * use {@link org.robolectric.junit.jupiter.RobolectricExtension}.
 *
 * <p>Robolectric picks a sandbox for each test based on its configuration, loads the test class
 * through that sandbox's instrumenting class loader, and runs the test on the sandbox's main
 * thread. A Jupiter extension cannot load test classes, since Jupiter loads them before any
 * extension runs. This engine therefore owns discovery and class loading, groups the tests by
 * sandbox, and runs the JUnit Jupiter engine once per sandbox, with the test classes loaded by the
 * sandbox. Jupiter provides the test semantics: nested classes, parameterized and dynamic tests,
 * and third-party extensions all work as usual.
 *
 * <p>The engine is registered with {@link java.util.ServiceLoader}, so it is found by any JUnit
 * Platform launcher (Gradle, Maven Surefire, IDEs) once it is on the test runtime classpath.
 */
public final class RobolectricTestEngine implements TestEngine {
  /** The ID of this engine. */
  public static final String ENGINE_ID = "robolectric";

  @Override
  public String getId() {
    return ENGINE_ID;
  }

  @Override
  public Optional<String> getGroupId() {
    return Optional.of("org.robolectric");
  }

  @Override
  public Optional<String> getArtifactId() {
    return Optional.of("junit-platform");
  }

  @Override
  public TestDescriptor discover(EngineDiscoveryRequest request, UniqueId uniqueId) {
    EngineDescriptor engineDescriptor = new EngineDescriptor(uniqueId, "Robolectric");
    Planner planner = new Planner(engineDescriptor, request);
    planner.plan(Selection.of(request, uniqueId, planner::isTestClass));
    return engineDescriptor;
  }

  @Override
  public void execute(ExecutionRequest request) {
    EngineExecutionListener listener = request.getEngineExecutionListener();
    TestDescriptor root = request.getRootTestDescriptor();
    listener.executionStarted(root);
    for (TestDescriptor child : new ArrayList<>(root.getChildren())) {
      if (request.getCancellationToken().isCancellationRequested()) {
        listener.executionSkipped(child, "Execution cancelled");
      } else {
        Planner.execute(child, request);
      }
    }
    listener.executionFinished(root, TestExecutionResult.successful());
  }
}
