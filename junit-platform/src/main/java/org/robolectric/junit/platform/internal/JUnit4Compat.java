package org.robolectric.junit.platform.internal;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nullable;
import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.builders.AllDefaultPossibilitiesBuilder;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.rules.RunRules;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RobolectricTestRunner.RobolectricFrameworkMethod;
import org.robolectric.TestLifecycle;
import org.robolectric.internal.AndroidSandbox;
import org.robolectric.internal.TestEnvironment;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;
import org.robolectric.internal.bytecode.Sandbox;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/**
 * Runs JUnit 4 classes that use {@code RobolectricTestRunner} (or a subclass, a {@code Suite} of
 * them such as {@code ParameterizedRobolectricTestRunner}, or {@code AndroidJUnit4}) in the
 * robolectric engine's sandboxes, so that they share sandboxes with JUnit Jupiter tests.
 *
 * <p>The declared runner provides the tests and their configuration: its {@code getChildren},
 * {@code describeChild}, {@code isIgnored}, {@code createClassLoaderConfig}, {@code
 * getTestLifecycleClass} and {@code getHelperTestRunner} are used, so subclasses that customize
 * them keep working. Choosing and setting up the sandbox, and reporting, is done by the engine.
 */
final class JUnit4Compat {
  static final String CLASS_SEGMENT_TYPE = "junit4-class";
  static final String TEST_SEGMENT_TYPE = "junit4-test";

  private static final String ANDROID_JUNIT4 = "androidx.test.ext.junit.runners.AndroidJUnit4";

  private JUnit4Compat() {}

  /** One runner of a JUnit 4 class: the class's own, or one child of its {@code Suite} runner. */
  static final class Unit {
    final Class<?> testClass;
    final RobolectricTestRunner runner;

    /** Null for the class's own runner, else e.g. {@code [0]} for a parameter set. */
    @Nullable final String name;

    Unit(Class<?> testClass, RobolectricTestRunner runner, @Nullable String name) {
      this.testClass = testClass;
      this.runner = runner;
      this.name = name;
    }
  }

  /** A JUnit 4 test, for one SDK. */
  static final class PlannedTest {
    final Unit unit;
    final RobolectricFrameworkMethod method;
    final Description description;
    final boolean ignored;
    final SandboxSpec spec;

    PlannedTest(
        Unit unit,
        RobolectricFrameworkMethod method,
        Description description,
        boolean ignored,
        SandboxSpec spec) {
      this.unit = unit;
      this.method = method;
      this.description = description;
      this.ignored = ignored;
      this.spec = spec;
    }
  }

  /** Whether {@code testClass} declares a runner this mode may be able to run. */
  static boolean isCandidate(Class<?> testClass) {
    RunWith runWith = testClass.getAnnotation(RunWith.class);
    if (runWith == null) {
      return false;
    }
    Class<? extends Runner> runnerClass = runWith.value();
    return RobolectricTestRunner.class.isAssignableFrom(runnerClass)
        || runnerClass.getName().equals(ANDROID_JUNIT4)
        || (ParentRunner.class.isAssignableFrom(runnerClass)
            && !BlockJUnit4ClassRunner.class.isAssignableFrom(runnerClass));
  }

  /**
   * Returns the selected tests of {@code testClass}, or null if its runner is not a Robolectric
   * runner, in which case the class is left to other engines.
   */
  @Nullable
  static List<PlannedTest> plan(Class<?> testClass, List<Selection.Item> items) throws Throwable {
    List<Unit> units = units(testClass);
    if (units == null) {
      return null;
    }
    RobolectricRuntime runtime = RobolectricRuntime.get();
    List<PlannedTest> result = new ArrayList<>();
    for (Unit unit : units) {
      List<FrameworkMethod> children =
          ReflectionHelpers.callInstanceMethod(unit.runner, "getChildren");
      for (FrameworkMethod child : children) {
        RobolectricFrameworkMethod method = (RobolectricFrameworkMethod) child;
        if (!Selection.selectsMethodNamed(items, method.getMethod().getName())) {
          continue;
        }
        ClassParameter<FrameworkMethod> parameter =
            ClassParameter.from(FrameworkMethod.class, method);
        Description description =
            ReflectionHelpers.callInstanceMethod(unit.runner, "describeChild", parameter);
        boolean ignored = ReflectionHelpers.callInstanceMethod(unit.runner, "isIgnored", parameter);
        InstrumentationConfiguration classLoaderConfig =
            RobolectricRuntime.withEnginePackagesNotAcquired(
                ReflectionHelpers.callInstanceMethod(
                    unit.runner, "createClassLoaderConfig", parameter));
        SandboxSpec spec =
            runtime.createSpec(method.getConfiguration(), method.getSdk(), classLoaderConfig);
        result.add(new PlannedTest(unit, method, description, ignored, spec));
      }
    }
    return result;
  }

  @Nullable
  private static List<Unit> units(Class<?> testClass) throws Throwable {
    Runner runner = new AllDefaultPossibilitiesBuilder().runnerForClass(testClass);
    if (runner.getClass().getName().equals(ANDROID_JUNIT4)) {
      runner = ReflectionHelpers.getField(runner, "delegate");
    }
    List<Unit> units = new ArrayList<>();
    if (runner instanceof RobolectricTestRunner) {
      units.add(new Unit(testClass, (RobolectricTestRunner) runner, null));
      return units;
    }
    if (!(runner instanceof ParentRunner)) {
      return null;
    }
    List<?> children = ReflectionHelpers.callInstanceMethod(runner, "getChildren");
    if (children.isEmpty()) {
      return null;
    }
    for (Object child : children) {
      if (!(child instanceof RobolectricTestRunner)) {
        return null;
      }
      RobolectricTestRunner childRunner = (RobolectricTestRunner) child;
      Class<?> childClass = childRunner.getTestClass().getJavaClass();
      // A parameterized runner has one child per parameter set, for the same class; an Enclosed
      // runner has one per inner class.
      units.add(
          new Unit(
              childClass,
              childRunner,
              childClass == testClass ? childRunner.getDescription().getDisplayName() : null));
    }
    return units;
  }

  /** Adds the planned tests to their sandboxes. */
  static void addToSandboxes(
      List<PlannedTest> tests, Function<SandboxSpec, SandboxDescriptor> sandboxes) {
    Map<SandboxSpec, Map<Unit, List<PlannedTest>>> grouped = new LinkedHashMap<>();
    for (PlannedTest test : tests) {
      grouped
          .computeIfAbsent(test.spec, unused -> new LinkedHashMap<>())
          .computeIfAbsent(test.unit, unused -> new ArrayList<>())
          .add(test);
    }
    for (Map.Entry<SandboxSpec, Map<Unit, List<PlannedTest>>> bySandbox : grouped.entrySet()) {
      SandboxDescriptor sandbox = sandboxes.apply(bySandbox.getKey());
      for (Map.Entry<Unit, List<PlannedTest>> byUnit : bySandbox.getValue().entrySet()) {
        Unit unit = byUnit.getKey();
        UniqueId classId =
            sandbox
                .getUniqueId()
                .append(
                    CLASS_SEGMENT_TYPE,
                    unit.testClass.getName() + (unit.name == null ? "" : "#" + unit.name));
        CompatClassDescriptor classDescriptor = new CompatClassDescriptor(classId, unit);
        Set<String> names = new LinkedHashSet<>();
        for (PlannedTest test : byUnit.getValue()) {
          sandbox.offerConfiguration(test.method.getConfiguration());
          String name = test.description.getMethodName();
          if (names.add(name)) {
            classDescriptor.addChild(
                new CompatMethodDescriptor(classId.append(TEST_SEGMENT_TYPE, name), test));
          }
        }
        sandbox.addChild(classDescriptor);
      }
    }
  }

  /** Runs the tests of one class in {@code sandbox}. Must be called on the sandbox main thread. */
  static void execute(
      CompatClassDescriptor classDescriptor,
      AndroidSandbox sandbox,
      EngineExecutionListener listener) {
    listener.executionStarted(classDescriptor);
    List<CompatMethodDescriptor> tests = new ArrayList<>();
    for (TestDescriptor child : classDescriptor.getChildren()) {
      tests.add((CompatMethodDescriptor) child);
    }
    Unit unit = classDescriptor.unit;
    TestExecutionResult classResult;
    try {
      Class<?> bootstrappedClass = sandbox.bootstrappedClass(unit.testClass);
      BlockJUnit4ClassRunner helper =
          ReflectionHelpers.callInstanceMethod(
              unit.runner,
              "getHelperTestRunner",
              ClassParameter.from(Class.class, bootstrappedClass));
      if (!tests.isEmpty()) {
        RobolectricRuntime.get()
            .configureSandbox(sandbox, tests.get(0).planned.method.getConfiguration());
      }
      Statement children =
          new Statement() {
            @Override
            public void evaluate() {
              for (CompatMethodDescriptor test : tests) {
                executeTest(test, helper, sandbox, listener);
              }
            }
          };
      Statement statement =
          ReflectionHelpers.callInstanceMethod(
              helper, "withBeforeClasses", ClassParameter.from(Statement.class, children));
      statement =
          ReflectionHelpers.callInstanceMethod(
              helper, "withAfterClasses", ClassParameter.from(Statement.class, statement));
      List<TestRule> classRules = ReflectionHelpers.callInstanceMethod(helper, "classRules");
      if (!classRules.isEmpty()) {
        statement = new RunRules(statement, classRules, unit.runner.getDescription());
      }
      statement.evaluate();
      classResult = TestExecutionResult.successful();
    } catch (AssumptionViolatedException e) {
      classResult = TestExecutionResult.aborted(e);
    } catch (Throwable t) {
      classResult = TestExecutionResult.failed(t);
    }
    listener.executionFinished(classDescriptor, classResult);
  }

  private static void executeTest(
      CompatMethodDescriptor descriptor,
      BlockJUnit4ClassRunner helper,
      AndroidSandbox sandbox,
      EngineExecutionListener listener) {
    PlannedTest planned = descriptor.planned;
    if (planned.ignored) {
      listener.executionSkipped(
          descriptor, "Ignored by " + planned.unit.runner.getClass().getName());
      return;
    }
    listener.executionStarted(descriptor);
    RobolectricFrameworkMethod method = planned.method;
    Throwable failure = null;
    boolean aborted = false;
    RobolectricTestScope scope = null;
    try {
      Method bootstrappedMethod = bootstrappedMethod(sandbox, method.getMethod());
      Class<? extends TestLifecycle> testLifecycleClass =
          ReflectionHelpers.callInstanceMethod(planned.unit.runner, "getTestLifecycleClass");
      AndroidManifest manifest = ReflectionHelpers.callInstanceMethod(method, "getAppManifest");
      scope =
          new RobolectricTestScope(
              sandbox, method.getConfiguration(), manifest, bootstrappedMethod, testLifecycleClass);
      // RobolectricTestRunner.HelperTestRunner reads these from the RobolectricFrameworkMethod.
      ReflectionHelpers.setField(method, "testLifecycle", scope.getTestLifecycle());
      ReflectionHelpers.callInstanceMethod(
          method,
          "setStuff",
          ClassParameter.from(Sandbox.class, sandbox),
          ClassParameter.from(TestEnvironment.class, scope.getTestEnvironment()));
      scope.setUp();
      ReflectionHelpers.setField(helper, "frameworkMethod", method);
      Statement statement =
          ReflectionHelpers.callInstanceMethod(
              helper,
              "methodBlock",
              ClassParameter.from(FrameworkMethod.class, new FrameworkMethod(bootstrappedMethod)));
      statement.evaluate();
    } catch (AssumptionViolatedException e) {
      aborted = true;
      failure = e;
    } catch (Throwable t) {
      failure = t;
    } finally {
      if (scope != null) {
        try {
          scope.close();
        } catch (Throwable t) {
          if (failure == null) {
            failure = t;
          } else {
            failure.addSuppressed(t);
          }
        }
      }
      ReflectionHelpers.setField(method, "testLifecycle", null);
    }
    TestExecutionResult result;
    if (failure == null) {
      result = TestExecutionResult.successful();
    } else if (aborted) {
      result = TestExecutionResult.aborted(failure);
    } else {
      result = TestExecutionResult.failed(failure);
    }
    listener.executionFinished(descriptor, result);
  }

  private static Method bootstrappedMethod(AndroidSandbox sandbox, Method method)
      throws NoSuchMethodException {
    Class<?> declaringClass = sandbox.bootstrappedClass(method.getDeclaringClass());
    List<Class<?>> parameterTypes = new ArrayList<>();
    for (Class<?> type : method.getParameterTypes()) {
      parameterTypes.add(type.isPrimitive() ? type : sandbox.bootstrappedClass(type));
    }
    return declaringClass.getMethod(method.getName(), parameterTypes.toArray(new Class<?>[0]));
  }
}
