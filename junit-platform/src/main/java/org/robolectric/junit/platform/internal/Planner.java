package org.robolectric.junit.platform.internal;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.EngineDiscoveryRequest;
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
 * Builds the robolectric engine's test plan: one {@link SandboxDescriptor} per sandbox, containing
 * the tests of JUnit Jupiter classes that use {@code RobolectricExtension} that run in it.
 */
public final class Planner {
  private final TestDescriptor engineDescriptor;
  private final EngineDiscoveryRequest request;
  private final JupiterDelegate jupiter;
  private final Map<SandboxSpec, SandboxDescriptor> sandboxes = new LinkedHashMap<>();
  private final Map<SandboxSpec, List<DiscoverySelector>> jupiterSelectors = new LinkedHashMap<>();
  private final Map<String, Integer> keyCounts = new HashMap<>();

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
  }

  private void planJupiterClass(Class<?> testClass, List<Selection.Item> items) {
    RobolectricRuntime runtime = RobolectricRuntime.get();
    List<TestMethod> tests = TestMethod.collect(testClass);
    Map<SandboxSpec, List<TestMethod>> testsBySandbox = new LinkedHashMap<>();
    Map<SandboxSpec, Configuration> configurations = new LinkedHashMap<>();
    int selectedTests = 0;
    for (TestMethod test : tests) {
      if (!Selection.selects(items, test)) {
        continue;
      }
      selectedTests++;
      Configuration configuration =
          runtime.getConfiguration(test.configurationClass(), test.method);
      AndroidManifest manifest = runtime.getAppManifest(configuration);
      for (Sdk sdk : runtime.selectSdks(configuration, manifest)) {
        SandboxSpec spec =
            runtime.createSpec(
                configuration,
                sdk,
                runtime.createClassLoaderConfig(
                    configuration, test.configurationClass(), test.method));
        testsBySandbox.computeIfAbsent(spec, unused -> new ArrayList<>()).add(test);
        configurations.putIfAbsent(spec, configuration);
      }
    }
    // Only add the class to the plan once all of its tests could be planned.
    boolean wholeClass = Selection.selectsWholeClass(items) && selectedTests == tests.size();
    for (Map.Entry<SandboxSpec, List<TestMethod>> entry : testsBySandbox.entrySet()) {
      SandboxSpec spec = entry.getKey();
      sandbox(spec).offerConfiguration(configurations.get(spec));
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

  /** Runs one child of the engine descriptor. */
  public static void execute(TestDescriptor child, ExecutionRequest request) {
    if (child instanceof SandboxDescriptor) {
      ((SandboxDescriptor) child)
          .execute(
              request,
              new JupiterDelegate(
                  request.getConfigurationParameters(), request.getOutputDirectoryCreator()));
    } else if (child instanceof InvalidClassDescriptor) {
      Throwable error = ((InvalidClassDescriptor) child).error;
      request.getEngineExecutionListener().executionStarted(child);
      request
          .getEngineExecutionListener()
          .executionFinished(child, TestExecutionResult.failed(error));
    }
  }
}
