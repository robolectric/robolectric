package org.robolectric;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import com.google.common.collect.ImmutableList;
import com.google.testing.junit.testparameterinjector.TestParameter;
import com.google.testing.junit.testparameterinjector.TestParameterInjector;
import com.google.testing.junit.testparameterinjector.TestParameters;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;

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
  private final DelegateTestParameterInjector testParameterInjector;

  public RobolectricTestParameterInjector(Class<?> testClass) throws InitializationError {
    super(testClass);
    testParameterInjector = new DelegateTestParameterInjector(testClass);
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
  protected InstrumentationConfiguration createClassLoaderConfig(FrameworkMethod method) {
    return new InstrumentationConfiguration.Builder(super.createClassLoaderConfig(method))
        .doNotAcquireClass(DelegateTestRunner.class)
        .build();
  }

  @Override
  protected ImmutableList<FrameworkMethod> getChildren() {
    // Instantiate test parameter injector to collect the parameterized test method list, and expand
    // it out for each sdk that the Robolectric runner is running. Unfortunately instantiating the
    // test parameter injector here means that we cannot inject Android objects, but this is a
    // similar limitation to the regular Robolectric test runner which doesn't support static
    // allocation or references to Android objects.
    Map<Method, List<FrameworkMethod>> injectedMethodsMap =
        testParameterInjector.computeTestMethods().stream()
            .collect(groupingBy(FrameworkMethod::getMethod));
    return ImmutableList.copyOf(
        super.getChildren().stream()
            .map(RobolectricFrameworkMethod.class::cast)
            .map(Objects::requireNonNull)
            .flatMap(
                robolectricFrameworkMethod -> {
                  List<FrameworkMethod> injectedMethods =
                      Objects.requireNonNull(
                          injectedMethodsMap.get(robolectricFrameworkMethod.getMethod()));
                  return IntStream.range(0, injectedMethods.size())
                      .mapToObj(
                          index ->
                              new RobolectricInjectedFrameworkMethod(
                                  robolectricFrameworkMethod, injectedMethods.get(index), index));
                })
            .collect(toList()));
  }

  @Override
  protected HelperTestRunner getHelperTestRunner(Class<?> sandboxedTestClass)
      throws InitializationError {
    return new DelegateHelperTestRunner(sandboxedTestClass);
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
    private final DelegateTestRunner testParameterInjector;
    private final Map<Method, List<FrameworkMethod>> injectedMethods;

    public DelegateHelperTestRunner(Class<?> bootstrappedTestClass) throws InitializationError {
      super(bootstrappedTestClass);
      testParameterInjector = createSandboxedDelegateTestParameterInjector(bootstrappedTestClass);
      injectedMethods =
          testParameterInjector.computeTestMethods().stream()
              .collect(groupingBy(FrameworkMethod::getMethod));
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
      return testParameterInjector.methodBlock(
          Objects.requireNonNull(injectedMethods.get(method.getMethod()))
              .get(robolectricInjectedFrameworkMethod.getInjectedMethodIndex()));
    }

    @Override
    protected Statement methodInvoker(FrameworkMethod method, Object test) {
      return testParameterInjector.methodInvoker(method, test);
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
          && o instanceof RobolectricInjectedFrameworkMethod
          && injectedMethod.equals(((RobolectricInjectedFrameworkMethod) o).getInjectedMethod());
    }
  }
}
