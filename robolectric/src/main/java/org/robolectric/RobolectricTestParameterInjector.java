package org.robolectric;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import com.google.common.collect.ImmutableList;
import com.google.testing.junit.testparameterinjector.TestParameter;
import com.google.testing.junit.testparameterinjector.TestParameterInjector;
import com.google.testing.junit.testparameterinjector.TestParameters;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;
import javax.annotation.Nonnull;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.robolectric.internal.AndroidSandbox;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;
import org.robolectric.pluginapi.Sdk;
import org.robolectric.pluginapi.config.ConfigurationStrategy.Configuration;

/**
 * A {@link RobolectricTestRunner} that supports {@link TestParameterInjector} parameter injection.
 *
 * <p>This runner provides several benefits over the {@link ParameterizedRobolectricTestRunner}, in
 * particular:
 *
 * <ul>
 *   <li>It provides convenient syntax for supporting field parameter injection.
 *   <li>It allows parameterizing individual test methods using {@link TestParameter}.
 *   <li>It provides easy to use declarative syntax for parameter combinations using {@link
 *       TestParameters}.
 * </ul>
 *
 * <p>See https://github.com/google/TestParameterInjector for more details.
 */
public final class RobolectricTestParameterInjector extends RobolectricTestRunner {
  private final Map<Sdk, Map<Configuration, Map<Method, List<FrameworkMethod>>>>
      sandboxedInjectedMethodsCache = new HashMap<>();

  public RobolectricTestParameterInjector(Class<?> testClass) throws InitializationError {
    super(testClass);
  }

  @Override
  protected void validateConstructor(List<Throwable> errors) {
    // Allow TestParameterInjector to handle validation.
  }

  @Override
  protected void validateTestMethods(List<Throwable> errors) {
    // Allow TestParameterInjector to handle validation.
  }

  @Override
  @Nonnull
  protected InstrumentationConfiguration createClassLoaderConfig(FrameworkMethod method) {
    return new InstrumentationConfiguration.Builder(super.createClassLoaderConfig(method))
        .doNotAcquireClass(DelegateTestRunner.class)
        .build();
  }

  @Override
  protected ImmutableList<FrameworkMethod> getChildren() {
    return ImmutableList.copyOf(
        super.getChildren().stream()
            .map(RobolectricFrameworkMethod.class::cast)
            .map(Objects::requireNonNull)
            .flatMap(
                robolectricFrameworkMethod -> {
                  AndroidSandbox sandbox = getSandbox(robolectricFrameworkMethod);
                  Method bootstrappedMethod =
                      getBootstrappedMethod(sandbox, robolectricFrameworkMethod.getMethod());
                  // TODO: This is computing all methods from the test class, when we only need to
                  //  expand the parameters for the current method. To do this we'll need to expose
                  //  a new method on the TestParameterInjector interface that we can use to
                  //  compute the test methods for a single method. This will also simplify the need
                  //  to key the methods on the bootstrapped method, instead we'll directly obtain
                  //  the injected method from the TestParameterInjector.
                  Map<Method, List<FrameworkMethod>> injectedMethodsByMethod =
                      sandboxedInjectedMethodsCache
                          .computeIfAbsent(
                              robolectricFrameworkMethod.getSdk(), k -> new HashMap<>())
                          .computeIfAbsent(
                              robolectricFrameworkMethod.getConfiguration(),
                              k ->
                                  runInSandbox(
                                      sandbox,
                                      robolectricFrameworkMethod,
                                      bootstrappedMethod,
                                      getTestClass().getJavaClass(),
                                      sandboxedTestClass ->
                                          createSandboxedDelegateTestParameterInjector(
                                                  sandboxedTestClass)
                                              .computeTestMethods()
                                              .stream()
                                              .collect(groupingBy(FrameworkMethod::getMethod))));

                  List<FrameworkMethod> injectedMethods =
                      Objects.requireNonNull(injectedMethodsByMethod.get(bootstrappedMethod));
                  return IntStream.range(0, injectedMethods.size())
                      .mapToObj(
                          index ->
                              new RobolectricInjectedFrameworkMethod(
                                  robolectricFrameworkMethod, injectedMethods.get(index), index));
                })
            .collect(toList()));
  }

  @FunctionalInterface
  private interface SandboxFunction<T, R> {
    R apply(T t) throws Throwable;
  }

  private <R> R runInSandbox(
      AndroidSandbox sandbox,
      RobolectricFrameworkMethod method,
      Method bootstrappedMethod,
      Class<?> testClass,
      SandboxFunction<Class<?>, R> function) {
    return sandbox.runOnMainThread(
        () -> {
          configureSandbox(sandbox, method);
          Class<?> sandboxedTestClass = sandbox.bootstrappedClass(testClass);
          ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
          Thread.currentThread().setContextClassLoader(sandbox.getRobolectricClassLoader());
          R result = null;
          Throwable throwable = null;
          try {
            beforeTest(sandbox, method, bootstrappedMethod);
            result = function.apply(sandboxedTestClass);
          } catch (Throwable t) {
            throwable = t;
          } finally {
            try {
              afterTest(method, bootstrappedMethod);
            } catch (Throwable t) {
              throwable = addSuppressed(throwable, t);
            } finally {
              try {
                finallyAfterTest(method);
              } catch (Throwable t) {
                throwable = addSuppressed(throwable, t);
              } finally {
                Thread.currentThread().setContextClassLoader(originalClassLoader);
              }
            }
          }
          if (throwable != null) {
            if (throwable instanceof Exception e) {
              throw e;
            } else {
              throw new RuntimeException(throwable);
            }
          }
          return result;
        });
  }

  private static Method getBootstrappedMethod(AndroidSandbox sandbox, Method originalMethod) {
    Class<?> bootstrappedMethodClass =
        sandbox.bootstrappedClass(originalMethod.getDeclaringClass());
    try {
      Class<?>[] parameterTypes =
          stream(originalMethod.getParameterTypes())
              .map(type -> type.isPrimitive() ? type : sandbox.bootstrappedClass(type))
              .toArray(Class[]::new);
      return bootstrappedMethodClass.getMethod(originalMethod.getName(), parameterTypes);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  private static Throwable addSuppressed(Throwable currentThrowable, Throwable newThrowable) {
    if (currentThrowable == null) {
      return newThrowable;
    } else {
      currentThrowable.addSuppressed(newThrowable);
      return currentThrowable;
    }
  }

  @Override
  protected HelperTestRunner getHelperTestRunner(Class<?> sandboxedTestClass)
      throws InitializationError {
    return new DelegateHelperTestRunner(sandboxedTestClass);
  }

  private static DelegateTestRunner createSandboxedDelegateTestParameterInjector(
      Class<?> sandboxedTestClass) {
    // Use reflection to class load an instance of the TestParameterInjector with the sandboxed
    // class loader, this allows it to inject objects using the sandboxed class loader into the
    // test class and methods.
    try {
      ClassLoader cl = Objects.requireNonNull(sandboxedTestClass.getClassLoader());
      Class<?> injectorClass = cl.loadClass(DelegateTestParameterInjector.class.getName());
      Constructor<?> injectorConstructor = injectorClass.getDeclaredConstructor(Class.class);
      injectorConstructor.setAccessible(true);
      return (DelegateTestRunner) injectorConstructor.newInstance(sandboxedTestClass);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  /** Interface to allow sandboxed injected runner to be accessed from the helper runner. */
  public interface DelegateTestRunner {
    List<FrameworkMethod> computeTestMethods();

    Statement methodBlock(FrameworkMethod method);

    Statement methodInvoker(FrameworkMethod method, Object test);
  }

  /**
   * Proxy for TestParameterInjector to allow the classloader to load TestParameterInjector in the
   * SDK sandbox.
   */
  private static final class DelegateTestParameterInjector implements DelegateTestRunner {
    private final TestParameterInjector delegate;

    @SuppressWarnings("UnusedMethod") // Used by reflection.
    DelegateTestParameterInjector(Class<?> klass) throws InitializationError {
      delegate = new TestParameterInjector(klass);
    }

    @Override
    public List<FrameworkMethod> computeTestMethods() {
      return delegate.computeTestMethods();
    }

    @Override
    public Statement methodBlock(FrameworkMethod method) {
      return delegate.methodBlock(method);
    }

    @Override
    public Statement methodInvoker(FrameworkMethod method, Object test) {
      return delegate.methodInvoker(method, test);
    }
  }

  /**
   * Helper test runner that delegates to a test parameter injector that has been class loaded in
   * the SDK sandbox.
   */
  private static final class DelegateHelperTestRunner extends HelperTestRunner {
    private DelegateTestRunner testParameterInjector;
    private Map<Method, List<FrameworkMethod>> injectedMethods;

    DelegateHelperTestRunner(Class<?> bootstrappedTestClass) throws InitializationError {
      super(bootstrappedTestClass);
    }

    // This is currently called from a single thread so no need to synchronize.
    private DelegateTestRunner getTestParameterInjector() {
      if (testParameterInjector == null) {
        testParameterInjector =
            createSandboxedDelegateTestParameterInjector(getTestClass().getJavaClass());
        injectedMethods =
            testParameterInjector.computeTestMethods().stream()
                .collect(groupingBy(FrameworkMethod::getMethod));
      }
      return testParameterInjector;
    }

    @Override
    protected void validateConstructor(List<Throwable> errors) {
      // Allow TestParameterInjector to handle validation.
    }

    @Override
    protected void validateTestMethods(List<Throwable> errors) {
      // Allow TestParameterInjector to handle validation.
    }

    @Override
    protected Statement methodBlock(FrameworkMethod method) {
      RobolectricInjectedFrameworkMethod robolectricInjectedFrameworkMethod =
          (RobolectricInjectedFrameworkMethod) frameworkMethod;
      // Map the method to one that has been class loaded in ths SDK sandbox.
      return getTestParameterInjector()
          .methodBlock(
              Objects.requireNonNull(injectedMethods.get(method.getMethod()))
                  .get(robolectricInjectedFrameworkMethod.getInjectedMethodIndex()));
    }

    @Override
    protected Statement methodInvoker(FrameworkMethod method, Object test) {
      return getTestParameterInjector().methodInvoker(method, test);
    }
  }

  private static final class RobolectricInjectedFrameworkMethod extends RobolectricFrameworkMethod {
    private final FrameworkMethod injectedMethod;
    private final int injectedMethodIndex;

    RobolectricInjectedFrameworkMethod(
        RobolectricFrameworkMethod other, FrameworkMethod injectedMethod, int injectedMethodIndex) {
      super(other);
      this.injectedMethod = injectedMethod;
      this.injectedMethodIndex = injectedMethodIndex;
    }

    FrameworkMethod getInjectedMethod() {
      return injectedMethod;
    }

    int getInjectedMethodIndex() {
      return injectedMethodIndex;
    }

    @Override
    public String getName() {
      // Both names are in the format "test[foo]" so strip the method name and concat them.
      return injectedMethod.getName() + super.getName().substring(getMethod().getName().length());
    }

    @Override
    public int hashCode() {
      return Objects.hash(super.hashCode(), injectedMethod);
    }

    @Override
    public boolean equals(Object o) {
      return super.equals(o)
          && o instanceof RobolectricInjectedFrameworkMethod robolectricInjectedFrameworkMethod
          && injectedMethod.equals(robolectricInjectedFrameworkMethod.getInjectedMethod());
    }
  }
}
