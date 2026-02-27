package org.robolectric;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.groupingBy;

import com.google.common.collect.ImmutableList;
import com.google.testing.junit.testparameterinjector.TestParameterInjector;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.IntStream;
import javax.annotation.Nonnull;
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
    if (isKotlinClass(getTestClass().getJavaClass())) {
      return mapChildrenInSandbox(
          super.getChildren(),
          (sandbox, methods) -> {
            Class<?> sandboxTestClass = sandbox.bootstrappedClass(getTestClass().getJavaClass());
            DelegateTestRunner testParameterInjector =
                createSandboxedDelegateTestParameterInjector(sandboxTestClass);
            List<FrameworkMethod> bootstrappedMethods =
                methods.stream()
                    .map(m -> new FrameworkMethod(getBootstrappedMethod(sandbox, m)))
                    .collect(toImmutableList());
            Map<Method, List<FrameworkMethod>> injectedMethodsMap =
                testParameterInjector.computeTestMethods(bootstrappedMethods).stream()
                    .collect(groupingBy(FrameworkMethod::getMethod));
            return applyInjectedMethods(
                methods,
                robolectricFrameworkMethod ->
                    injectedMethodsMap.get(
                        getBootstrappedMethod(sandbox, robolectricFrameworkMethod)));
          });
    } else {
      // This is a Java test class. Because Java does not have default parameter values,
      // TestParameterInjector does not need to construct 'dummy' class instances, this
      // means we can safely use the non-sandboxed version here.
      //
      // Note that the Kotlin version above works fine for the most part, but some (Java) tests are
      // broken by it, usually due to niche static state issues (see cl/868663089, cl/862287907,
      // cl/869244208, cl/869178240, cl/869179101 for some examples).
      try {
        DelegateTestRunner testParameterInjector =
            new DelegateTestParameterInjector(getTestClass().getJavaClass());
        Map<Method, List<FrameworkMethod>> injectedMethodsMap =
            testParameterInjector.computeTestMethods().stream()
                .collect(groupingBy(FrameworkMethod::getMethod));
        return applyInjectedMethods(
            super.getChildren(), m -> injectedMethodsMap.get(m.getMethod()));
      } catch (InitializationError e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  protected HelperTestRunner getHelperTestRunner(Class<?> sandboxedTestClass)
      throws InitializationError {
    return new DelegateHelperTestRunner(sandboxedTestClass);
  }

  private ImmutableList<FrameworkMethod> applyInjectedMethods(
      Collection<FrameworkMethod> methods,
      Function<FrameworkMethod, List<FrameworkMethod>> injectedMethodsGetter) {
    return methods.stream()
        .map(RobolectricFrameworkMethod.class::cast)
        .map(Objects::requireNonNull)
        .flatMap(
            robolectricFrameworkMethod -> {
              List<FrameworkMethod> injectedMethods =
                  injectedMethodsGetter.apply(robolectricFrameworkMethod);
              if (injectedMethods == null) {
                // This should not happen if computeTestMethods preserves methods
                return IntStream.empty().mapToObj(unused -> null); // Empty stream
              }

              // We rely on the order of injected methods being deterministic
              AtomicInteger index = new AtomicInteger(0);
              return injectedMethods.stream()
                  .map(
                      injected ->
                          new RobolectricInjectedFrameworkMethod(
                              robolectricFrameworkMethod, injected, index.getAndIncrement()));
            })
        .filter(Objects::nonNull)
        .collect(toImmutableList());
  }

  private static boolean isKotlinClass(Class<?> clazz) {
    return stream(clazz.getDeclaredAnnotations())
        .anyMatch(annotation -> annotation.annotationType().getName().equals("kotlin.Metadata"));
  }

  /** Interface to allow sandboxed injected runner to be accessed from the helper runner. */
  public interface DelegateTestRunner {
    List<FrameworkMethod> computeTestMethods();

    List<FrameworkMethod> computeTestMethods(List<FrameworkMethod> methods);

    Statement methodBlock(FrameworkMethod method);

    Statement methodInvoker(FrameworkMethod method, Object test);
  }

  /**
   * Proxy for TestParameterInjector to allow the classloader to load TestParameterInjector in the
   * SDK sandbox.
   */
  private static final class DelegateTestParameterInjector implements DelegateTestRunner {
    private final TestParameterInjector delegate;

    @SuppressWarnings("UnusedMethod") // Used via reflection.
    DelegateTestParameterInjector(Class<?> klass) throws InitializationError {
      delegate = new TestParameterInjector(klass);
    }

    @Override
    public List<FrameworkMethod> computeTestMethods() {
      return delegate.computeTestMethods();
    }

    @Override
    public List<FrameworkMethod> computeTestMethods(List<FrameworkMethod> methods) {
      return delegate.computeTestMethods(methods);
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
