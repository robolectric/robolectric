package org.robolectric.junit.jupiter;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.Nullable;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.LifecycleMethodExecutionExceptionHandler;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.junit.jupiter.api.extension.TestInstanceFactoryContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.junit.jupiter.api.extension.TestInstancePreConstructCallback;
import org.opentest4j.TestAbortedException;
import org.robolectric.TestLifecycle;
import org.robolectric.internal.AndroidSandbox;
import org.robolectric.junit.platform.internal.RobolectricRuntime;
import org.robolectric.junit.platform.internal.RobolectricTestScope;
import org.robolectric.junit.platform.internal.SandboxRegistry;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.pluginapi.config.ConfigurationStrategy.Configuration;

/**
 * Runs a JUnit Jupiter test class in Robolectric's simulated Android environment.
 *
 * <pre>{@code
 * @ExtendWith(RobolectricExtension.class)
 * @Config(sdk = {34, 36})
 * class MyTest {
 *   private final Context context = ApplicationProvider.getApplicationContext();
 *
 *   @Test
 *   void works() { ... }
 * }
 * }</pre>
 *
 * <p>The test classes are run by the {@code robolectric} JUnit Platform engine, which loads them
 * through the right Robolectric sandbox and runs each test on the sandbox's main thread. This
 * extension sets up and tears down the Android environment of each test, the same way {@code
 * RobolectricTestRunner} does for JUnit 4: {@code @Config}, qualifiers, shadows, the application,
 * and the reset of Android state afterwards. The application is set up before the test instance is
 * constructed, so field initializers can use it.
 *
 * <p>{@code @BeforeAll} methods run in the sandbox, before any test sets up the application.
 * {@code @Config} applies from the top-level test class and the test method; {@code @Config} on
 * {@code @Nested} classes is not supported yet.
 */
public class RobolectricExtension
    implements BeforeAllCallback,
        TestInstancePreConstructCallback,
        TestInstancePostProcessor,
        BeforeEachCallback,
        TestExecutionExceptionHandler,
        LifecycleMethodExecutionExceptionHandler {
  private static final Namespace NAMESPACE = Namespace.create(RobolectricExtension.class);

  /**
   * Returns the {@link TestLifecycle} to use, like {@code RobolectricTestRunner
   * #getTestLifecycleClass}; null for the default.
   */
  @Nullable
  protected Class<? extends TestLifecycle> getTestLifecycleClass() {
    return null;
  }

  /** Receive the test method's context when the test instance is created, not the class's. */
  @Override
  public ExtensionContextScope getTestInstantiationExtensionContextScope(
      ExtensionContext rootContext) {
    return ExtensionContextScope.TEST_METHOD;
  }

  @Override
  public void beforeAll(ExtensionContext context) {
    Class<?> testClass = context.getRequiredTestClass();
    if (SandboxRegistry.forClass(testClass) == null) {
      throw new ExtensionConfigurationException(
          testClass.getName()
              + " uses RobolectricExtension but is not running in a Robolectric sandbox. Tests"
              + " using RobolectricExtension are run by the 'robolectric' test engine"
              + " (org.robolectric:junit-platform); make sure that it is on the test runtime"
              + " classpath and that it is not excluded, e.g. by Gradle's includeEngines.");
    }
  }

  @Override
  public void preConstructTestInstance(
      TestInstanceFactoryContext factoryContext, ExtensionContext context) {
    if (context.getTestMethod().isPresent()) {
      scope(context);
    }
  }

  @Override
  public void postProcessTestInstance(Object testInstance, ExtensionContext context) {
    if (context.getTestMethod().isPresent()) {
      scope(context).prepareTest(testInstance);
    }
  }

  @Override
  public void beforeEach(ExtensionContext context) {
    checkNoJUnit4Rules(context);
    // With the PER_CLASS lifecycle, the test instance is created before any test method's context.
    scope(context);
  }

  /** Fails the test if its class has JUnit 4 rules, which JUnit Jupiter would silently not run. */
  private static void checkNoJUnit4Rules(ExtensionContext context) {
    Set<String> rules = new LinkedHashSet<>();
    for (Object testInstance : context.getRequiredTestInstances().getAllInstances()) {
      for (Class<?> c = testInstance.getClass(); c != null; c = c.getSuperclass()) {
        for (Field field : c.getDeclaredFields()) {
          if (isJUnit4Rule(field)) {
            rules.add("'" + field.getName() + "'");
          }
        }
        // Kotlin's @get:Rule annotates the getter.
        for (Method method : c.getDeclaredMethods()) {
          if (isJUnit4Rule(method)) {
            rules.add("'" + method.getName() + "()'");
          }
        }
      }
    }
    if (!rules.isEmpty()) {
      throw new ExtensionConfigurationException(
          context.getRequiredTestClass().getName()
              + " has JUnit 4 rules, which JUnit Jupiter does not run: "
              + String.join(", ", rules)
              + ". Keep the class on JUnit 4, which JUnit Vintage runs alongside JUnit Jupiter"
              + " classes, or replace the rules with JUnit Jupiter extensions.");
    }
  }

  /** Checks by name, since JUnit 4 does not have to be on the classpath. */
  private static boolean isJUnit4Rule(AnnotatedElement element) {
    for (Annotation annotation : element.getAnnotations()) {
      String name = annotation.annotationType().getName();
      if (name.equals("org.junit.Rule") || name.equals("org.junit.ClassRule")) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void handleTestExecutionException(ExtensionContext context, Throwable throwable)
      throws Throwable {
    checkStateAfterFailure(context, throwable);
    throw throwable;
  }

  @Override
  public void handleBeforeEachMethodExecutionException(
      ExtensionContext context, Throwable throwable) throws Throwable {
    checkStateAfterFailure(context, throwable);
    throw throwable;
  }

  @Override
  public void handleAfterEachMethodExecutionException(ExtensionContext context, Throwable throwable)
      throws Throwable {
    checkStateAfterFailure(context, throwable);
    throw throwable;
  }

  private static void checkStateAfterFailure(ExtensionContext context, Throwable throwable)
      throws Throwable {
    if (isAssumptionFailure(throwable)) {
      return;
    }
    RobolectricTestScope scope =
        context.getStore(NAMESPACE).get(RobolectricTestScope.class, RobolectricTestScope.class);
    if (scope != null) {
      scope.checkStateAfterTestFailure(throwable);
    }
  }

  /** Checks by name, since JUnit 4 does not have to be on the classpath. */
  private static boolean isAssumptionFailure(Throwable throwable) {
    if (throwable instanceof TestAbortedException) {
      return true;
    }
    for (Class<?> c = throwable.getClass(); c != null; c = c.getSuperclass()) {
      if (c.getName().equals("org.junit.internal.AssumptionViolatedException")) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns the test's Android environment, setting it up the first time. It is torn down when
   * Jupiter closes the test method's store, after all {@code @AfterEach} methods.
   */
  private RobolectricTestScope scope(ExtensionContext context) {
    // Store.computeIfAbsent is only in JUnit 6, and JUnit 6 deprecates getOrComputeIfAbsent.
    ExtensionContext.Store store = context.getStore(NAMESPACE);
    RobolectricTestScope scope = store.get(RobolectricTestScope.class, RobolectricTestScope.class);
    if (scope == null) {
      scope = setUp(context);
      store.put(RobolectricTestScope.class, scope);
    }
    return scope;
  }

  private RobolectricTestScope setUp(ExtensionContext context) {
    Class<?> testClass = context.getRequiredTestClass();
    Method method = context.getRequiredTestMethod();
    AndroidSandbox sandbox = SandboxRegistry.forClass(testClass);
    if (sandbox == null) {
      throw new ExtensionConfigurationException(
          testClass.getName() + " is not running in a Robolectric sandbox");
    }
    // The configuration is resolved on the classes of the application class loader, exactly like
    // the engine did when it chose the sandbox.
    ClassLoader applicationClassLoader = RobolectricRuntime.class.getClassLoader();
    Class<?> configurationClass;
    try {
      configurationClass =
          Class.forName(topLevelClass(testClass).getName(), false, applicationClassLoader);
    } catch (ClassNotFoundException e) {
      throw new ExtensionConfigurationException("Cannot load " + testClass.getName(), e);
    }
    Method applicationMethod = RobolectricRuntime.findCounterpart(method, applicationClassLoader);
    RobolectricRuntime runtime = RobolectricRuntime.get();
    Configuration configuration = runtime.getConfiguration(configurationClass, applicationMethod);
    AndroidManifest manifest = runtime.getAppManifest(configuration);
    RobolectricTestScope scope =
        new RobolectricTestScope(sandbox, configuration, manifest, method, getTestLifecycleClass());
    scope.setUp();
    return scope;
  }

  private static Class<?> topLevelClass(Class<?> clazz) {
    Class<?> result = clazz;
    while (result.getEnclosingClass() != null && !Modifier.isStatic(result.getModifiers())) {
      result = result.getEnclosingClass();
    }
    return result;
  }
}
