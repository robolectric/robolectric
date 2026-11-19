package org.robolectric.junit.platform.internal;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.TestPlan;
import org.robolectric.junit.jupiter.RobolectricExtension;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.pluginapi.Sdk;
import org.robolectric.pluginapi.config.ConfigurationStrategy.Configuration;

/**
 * Builds the robolectric engine's test plan: the tests of JUnit Jupiter classes that use {@code
 * RobolectricExtension}, grouped by the sandbox that runs them, and reported by class.
 */
public final class Planner {
  private final TestDescriptor engineDescriptor;
  private final EngineDiscoveryRequest request;
  private final JupiterDelegate jupiter;
  private final Map<SandboxSpec, SandboxDescriptor> sandboxes = new LinkedHashMap<>();
  private final Map<SandboxSpec, List<DiscoverySelector>> jupiterSelectors = new LinkedHashMap<>();
  private final Map<String, Integer> keyCounts = new HashMap<>();
  private final boolean alwaysIncludeVariantMarkersInTestName =
      Boolean.parseBoolean(
          System.getProperty("robolectric.alwaysIncludeVariantMarkersInTestName", "false"));

  public Planner(TestDescriptor engineDescriptor, EngineDiscoveryRequest request) {
    this.engineDescriptor = engineDescriptor;
    this.request = request;
    this.jupiter =
        new JupiterDelegate(
            request.getConfigurationParameters(), request.getOutputDirectoryCreator());
  }

  /** Whether the robolectric engine runs {@code testClass}. */
  public boolean isTestClass(Class<?> testClass) {
    return isTopLevelClass(testClass) && usesRobolectricExtension(testClass);
  }

  static boolean isTopLevelClass(Class<?> clazz) {
    return !clazz.isAnnotation()
        && !clazz.isInterface()
        && !clazz.isLocalClass()
        && !clazz.isAnonymousClass()
        && !Modifier.isAbstract(clazz.getModifiers())
        && (clazz.getEnclosingClass() == null || Modifier.isStatic(clazz.getModifiers()));
  }

  static boolean usesRobolectricExtension(Class<?> testClass) {
    for (ExtendWith extendWith :
        AnnotationSupport.findRepeatableAnnotations(testClass, ExtendWith.class)) {
      for (Class<?> extension : extendWith.value()) {
        if (RobolectricExtension.class.isAssignableFrom(extension)) {
          return true;
        }
      }
    }
    return false;
  }

  /** Plans the selected tests. */
  public void plan(Selection selection) {
    for (Map.Entry<Class<?>, List<Selection.Item>> entry : selection.itemsByClass().entrySet()) {
      Class<?> testClass = entry.getKey();
      try {
        planJupiterClass(testClass, entry.getValue());
      } catch (RuntimeException | LinkageError e) {
        // An invalid configuration (e.g. an unsupported SDK) fails the class, not the whole run.
        UniqueId id = engineDescriptor.getUniqueId().append("invalid-class", testClass.getName());
        engineDescriptor.addChild(new InvalidClassDescriptor(id, testClass, e));
      }
      ClaimRegistry.claim(testClass);
    }
    for (Map.Entry<SandboxSpec, List<DiscoverySelector>> entry : jupiterSelectors.entrySet()) {
      // Planned with the application class loader: building the test plan needs no sandbox.
      TestPlan plan =
          jupiter.discover(
              entry.getValue(), request.getDiscoveryListener(), engineDescriptor.getUniqueId());
      sandbox(entry.getKey()).addJupiterTests(plan);
    }
    for (SandboxDescriptor sandbox : sandboxes.values()) {
      if (!sandbox.getChildren().isEmpty()) {
        engineDescriptor.addChild(sandbox);
      }
    }
    selection.prune(engineDescriptor);
    groupByClass();
  }

  /**
   * Replaces the sandboxes in the test plan by one {@link TestClassDescriptor} per test class, with
   * the class's tests from all of its sandboxes, which still run them.
   */
  private void groupByClass() {
    Map<String, TestClassDescriptor> classes = new LinkedHashMap<>();
    for (TestDescriptor child : new ArrayList<>(engineDescriptor.getChildren())) {
      if (child instanceof SandboxDescriptor sandbox) {
        engineDescriptor.removeChild(sandbox);
        for (TestDescriptor jupiterClass : sandbox.getChildren()) {
          String className = jupiterClass.getUniqueId().getLastSegment().getValue();
          TestClassDescriptor testClassDescriptor =
              classes.computeIfAbsent(
                  className,
                  unused -> {
                    TestClassDescriptor descriptor =
                        new TestClassDescriptor(
                            engineDescriptor
                                .getUniqueId()
                                .append(TestClassDescriptor.SEGMENT_TYPE, className),
                            jupiterClass);
                    engineDescriptor.addChild(descriptor);
                    return descriptor;
                  });
          testClassDescriptor.sandboxes.add(sandbox);
          sandbox.reportUnder(jupiterClass, testClassDescriptor);
        }
      }
    }
  }

  private void planJupiterClass(Class<?> testClass, List<Selection.Item> items) {
    RobolectricRuntime runtime = RobolectricRuntime.get();
    List<TestMethod> tests = TestMethod.collect(testClass);
    Map<SandboxSpec, List<TestMethod>> testsBySandbox = new LinkedHashMap<>();
    Map<SandboxSpec, Configuration> configurations = new LinkedHashMap<>();
    Map<SandboxSpec, Set<TestMethod>> namedWithSdk = new HashMap<>();
    int selectedTests = 0;
    for (TestMethod test : tests) {
      if (!Selection.selects(items, test)) {
        continue;
      }
      selectedTests++;
      Configuration configuration =
          runtime.getConfiguration(test.configurationClass(), test.method);
      AndroidManifest manifest = runtime.getAppManifest(configuration);
      List<Sdk> sdks = runtime.selectSdks(configuration, manifest);
      for (int i = 0; i < sdks.size(); i++) {
        SandboxSpec spec =
            runtime.createSpec(
                configuration,
                sdks.get(i),
                runtime.createClassLoaderConfig(
                    configuration, test.configurationClass(), test.method));
        testsBySandbox.computeIfAbsent(spec, unused -> new ArrayList<>()).add(test);
        configurations.putIfAbsent(spec, configuration);
        // Like RobolectricTestRunner, name a test that runs on several SDKs after the SDK, except
        // on the last one, which keeps the plain name.
        if (alwaysIncludeVariantMarkersInTestName || i < sdks.size() - 1) {
          namedWithSdk.computeIfAbsent(spec, unused -> new HashSet<>()).add(test);
        }
      }
    }
    // Only add the class to the plan once all of its tests could be planned.
    boolean wholeClass = Selection.selectsWholeClass(items) && selectedTests == tests.size();
    for (Map.Entry<SandboxSpec, List<TestMethod>> entry : testsBySandbox.entrySet()) {
      SandboxSpec spec = entry.getKey();
      SandboxDescriptor sandbox = sandbox(spec);
      sandbox.offerConfiguration(configurations.get(spec));
      for (TestMethod test : namedWithSdk.getOrDefault(spec, Set.of())) {
        sandbox.nameWithSdk(test.source());
      }
      List<DiscoverySelector> selectors =
          jupiterSelectors.computeIfAbsent(spec, unused -> new ArrayList<>());
      if (wholeClass && entry.getValue().size() == tests.size()) {
        // Every test of the class runs in this sandbox: let Jupiter resolve the class as usual.
        selectors.add(DiscoverySelectors.selectClass(testClass));
      } else {
        for (TestMethod test : entry.getValue()) {
          selectors.add(test.selector());
        }
      }
    }
  }

  private SandboxDescriptor sandbox(SandboxSpec spec) {
    return sandboxes.computeIfAbsent(
        spec,
        unused -> {
          // Different specs with the same key (a hash collision) still need unique IDs.
          String key = spec.key();
          int count = keyCounts.merge(key, 1, Integer::sum);
          UniqueId id =
              engineDescriptor
                  .getUniqueId()
                  .append(SandboxDescriptor.SEGMENT_TYPE, count == 1 ? key : key + "#" + count);
          return new SandboxDescriptor(id, spec);
        });
  }

  /**
   * Runs the children of the engine descriptor: the sandboxes one after the other, finishing each
   * class after the last sandbox that runs it.
   */
  public static void execute(TestDescriptor root, ExecutionRequest request) {
    EngineExecutionListener listener = request.getEngineExecutionListener();
    Set<SandboxDescriptor> sandboxes = new LinkedHashSet<>();
    for (TestDescriptor child : new ArrayList<>(root.getChildren())) {
      if (child instanceof TestClassDescriptor testClassDescriptor) {
        sandboxes.addAll(testClassDescriptor.sandboxes);
      } else if (child instanceof InvalidClassDescriptor invalidClass) {
        listener.executionStarted(child);
        listener.executionFinished(child, TestExecutionResult.failed(invalidClass.error));
      }
    }
    Map<TestClassDescriptor, Integer> remainingSandboxes = new HashMap<>();
    JupiterDelegate jupiter =
        new JupiterDelegate(
            request.getConfigurationParameters(), request.getOutputDirectoryCreator());
    for (SandboxDescriptor sandbox : sandboxes) {
      sandbox.execute(request, jupiter);
      for (TestClassDescriptor testClassDescriptor : sandbox.classes()) {
        int remaining =
            remainingSandboxes.merge(
                testClassDescriptor,
                testClassDescriptor.sandboxes.size() - 1,
                (a, unused) -> a - 1);
        if (remaining == 0) {
          testClassDescriptor.finish(listener);
        }
      }
    }
  }
}
