package org.robolectric.junit.platform.internal;

import java.util.List;
import java.util.ServiceLoader;
import org.junit.platform.engine.CancellationToken;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.DiscoveryIssue;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.EngineDiscoveryListener;
import org.junit.platform.engine.OutputDirectoryCreator;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryListener;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherConfig;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherExecutionRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;

/**
 * Runs the JUnit Jupiter engine through the public Launcher API.
 *
 * <p>Robolectric needs Jupiter to discover and run tests twice: once with the application class
 * loader to build the test plan, and once per sandbox with the classes loaded by the sandbox.
 */
final class JupiterDelegate {
  static final String JUPITER_ENGINE_ID = "junit-jupiter";

  /** Robolectric tests have to run on their sandbox's main thread. */
  private static final String PARALLEL_EXECUTION = "junit.jupiter.execution.parallel.enabled";

  private final ConfigurationParameters configurationParameters;
  private final OutputDirectoryCreator outputDirectoryCreator;

  JupiterDelegate(
      ConfigurationParameters configurationParameters,
      OutputDirectoryCreator outputDirectoryCreator) {
    this.configurationParameters = configurationParameters;
    this.outputDirectoryCreator = outputDirectoryCreator;
  }

  /**
   * Discovers the tests selected by {@code selectors}, reporting discovery issues to {@code
   * issueListener} as issues of {@code engineId}.
   */
  TestPlan discover(
      List<? extends DiscoverySelector> selectors,
      EngineDiscoveryListener issueListener,
      UniqueId engineId) {
    LauncherDiscoveryListener forwardingListener =
        new LauncherDiscoveryListener() {
          @Override
          public void issueEncountered(UniqueId unused, DiscoveryIssue issue) {
            issueListener.issueEncountered(engineId, issue);
          }
        };
    return createLauncher().discover(request(selectors, forwardingListener));
  }

  /** Discovers and runs the tests selected by {@code selectors}. */
  void execute(
      List<? extends DiscoverySelector> selectors,
      TestExecutionListener listener,
      CancellationToken cancellationToken) {
    LauncherDiscoveryRequest request = request(selectors, new LauncherDiscoveryListener() {});
    createLauncher()
        .execute(
            LauncherExecutionRequestBuilder.request(request)
                .listeners(listener)
                .cancellationToken(cancellationToken)
                .build());
  }

  private LauncherDiscoveryRequest request(
      List<? extends DiscoverySelector> selectors, LauncherDiscoveryListener listener) {
    return LauncherDiscoveryRequestBuilder.request()
        .selectors(selectors)
        .parentConfigurationParameters(configurationParameters)
        .configurationParameter(PARALLEL_EXECUTION, "false")
        .outputDirectoryCreator(outputDirectoryCreator)
        .listeners(listener)
        .build();
  }

  private static Launcher createLauncher() {
    return LauncherFactory.create(
        LauncherConfig.builder()
            .enableTestEngineAutoRegistration(false)
            .enableLauncherSessionListenerAutoRegistration(false)
            .enableLauncherDiscoveryListenerAutoRegistration(false)
            .enablePostDiscoveryFilterAutoRegistration(false)
            .enableTestExecutionListenerAutoRegistration(false)
            .addTestEngines(jupiterEngine())
            .build());
  }

  private static TestEngine jupiterEngine() {
    for (TestEngine engine :
        ServiceLoader.load(TestEngine.class, JupiterDelegate.class.getClassLoader())) {
      if (engine.getId().equals(JUPITER_ENGINE_ID)) {
        return engine;
      }
    }
    throw new IllegalStateException(
        "The JUnit Jupiter engine (org.junit.jupiter:junit-jupiter-engine) is required to run"
            + " Robolectric tests that use RobolectricExtension");
  }

  /** Maps a unique ID of the delegate Jupiter engine to the same test under {@code parent}. */
  static UniqueId toOuter(UniqueId jupiterId, UniqueId parent) {
    UniqueId result = parent;
    List<UniqueId.Segment> segments = jupiterId.getSegments();
    for (UniqueId.Segment segment : segments.subList(1, segments.size())) {
      result = result.append(segment);
    }
    return result;
  }

  /** The inverse of {@link #toOuter}. */
  static UniqueId toJupiter(UniqueId outerId, UniqueId parent) {
    UniqueId result = UniqueId.forEngine(JUPITER_ENGINE_ID);
    List<UniqueId.Segment> segments = outerId.getSegments();
    for (UniqueId.Segment segment :
        segments.subList(parent.getSegments().size(), segments.size())) {
      result = result.append(segment);
    }
    return result;
  }
}
