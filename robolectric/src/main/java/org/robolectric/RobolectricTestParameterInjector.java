package org.robolectric;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.groupingBy;

import com.google.common.collect.ImmutableList;
import com.google.testing.junit.testparameterinjector.TestParameter;
import com.google.testing.junit.testparameterinjector.TestParameterInjector;
import com.google.testing.junit.testparameterinjector.TestParameters;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.IntStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.junit.AssumptionViolatedException;
import org.junit.Test;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkField;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;
import org.robolectric.internal.bytecode.Sandbox;

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
  @Nullable
  private static final Object DEFAULT_TEST_PARAMETER_VALUES_PROVIDER =
      defaultValuesProvider(TestParameter.class);

  @Nullable
  private static final Object DEFAULT_TEST_PARAMETERS_VALUES_PROVIDER =
      defaultValuesProvider(TestParameters.class);

  private final Map<ClassLoader, DelegateTestRunner> delegatesByClassLoader =
      Collections.synchronizedMap(new WeakHashMap<>());

  public RobolectricTestParameterInjector(Class<?> testClass) throws InitializationError {
    super(testClass);
  }

  @Override
  public void run(RunNotifier notifier) {
    try {
      super.run(notifier);
    } finally {
      delegatesByClassLoader.clear();
    }
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
      return mapChildrenInSandbox(super.getChildren(), this::discoverInSandbox);
    } else if (hasCustomValuesProviders()) {
      return discoverJavaCustomProviders(super.getChildren());
    } else {
      // This is a Java test class. Because Java does not have default parameter values,
      // TestParameterInjector does not need to construct 'dummy' class instances, this
      // means we can safely use the non-sandboxed version here.
      //
      // Note that the Kotlin version above works fine for the most part, but some (Java) tests are
      // broken by it, usually due to niche static state issues (see cl/868663089, cl/862287907,
      // cl/869244208, cl/869178240, cl/869179101 for some examples).
      return discoverOnHost(super.getChildren());
    }
  }

  private ImmutableList<FrameworkMethod> discoverInSandbox(
      Sandbox sandbox, List<FrameworkMethod> methods) {
    Class<?> sandboxTestClass = sandbox.bootstrappedClass(getTestClass().getJavaClass());
    DelegateTestRunner testParameterInjector =
        createSandboxedDelegateTestParameterInjector(sandboxTestClass);
    delegatesByClassLoader.put(sandbox.getRobolectricClassLoader(), testParameterInjector);
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
            injectedMethodsMap.get(getBootstrappedMethod(sandbox, robolectricFrameworkMethod)));
  }

  private ImmutableList<FrameworkMethod> discoverJavaCustomProviders(
      List<FrameworkMethod> children) {
    // Java tests with custom valuesProviders do not instantiate the test class during discovery,
    // so run discovery in the configured sandbox without beforeTest/afterTest lifecycle hooks.
    Map<Sandbox, List<FrameworkMethod>> methodsBySandbox = new LinkedHashMap<>();
    List<FrameworkMethod> hostMethods = new ArrayList<>();
    for (FrameworkMethod method : children) {
      if (isIgnored(method)) {
        hostMethods.add(method);
        continue;
      }
      try {
        Sandbox sandbox = getSandbox(method);
        methodsBySandbox.computeIfAbsent(sandbox, unused -> new ArrayList<>()).add(method);
      } catch (IllegalArgumentException | AssumptionViolatedException e) {
        hostMethods.add(method);
      }
    }

    ImmutableList.Builder<FrameworkMethod> result = ImmutableList.builder();
    for (Map.Entry<Sandbox, List<FrameworkMethod>> entry : methodsBySandbox.entrySet()) {
      List<FrameworkMethod> methods = entry.getValue();
      Sandbox sandbox = entry.getKey();
      if (sandbox.isShutdown()) {
        sandbox = getSandbox(methods.get(0));
      }
      configureSandbox(sandbox, methods.get(0));
      Sandbox activeSandbox = sandbox;
      activeSandbox.runOnMainThreadWithClassLoader(
          () -> result.addAll(discoverInSandbox(activeSandbox, methods)));
    }
    if (!hostMethods.isEmpty()) {
      result.addAll(discoverOnHost(hostMethods));
    }
    return result.build();
  }

  private ImmutableList<FrameworkMethod> discoverOnHost(List<FrameworkMethod> methods) {
    if (methods.isEmpty()) {
      return ImmutableList.of();
    }
    try {
      DelegateTestRunner testParameterInjector =
          new DelegateTestParameterInjector(getTestClass().getJavaClass());
      List<FrameworkMethod> uniqueMethods =
          methods.stream()
              .map(FrameworkMethod::getMethod)
              .distinct()
              .map(FrameworkMethod::new)
              .collect(toImmutableList());
      Map<Method, List<FrameworkMethod>> injectedMethodsMap =
          testParameterInjector.computeTestMethods(uniqueMethods).stream()
              .collect(groupingBy(FrameworkMethod::getMethod));
      return applyInjectedMethods(methods, m -> injectedMethodsMap.get(m.getMethod()));
    } catch (InitializationError e) {
      throw new RuntimeException(e);
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
        .flatMap(robolectricFrameworkMethod -> {
          List<FrameworkMethod> injectedMethods =
              injectedMethodsGetter.apply(robolectricFrameworkMethod);
          if (injectedMethods == null) {
            // This should not happen if computeTestMethods preserves methods
            return IntStream.empty().mapToObj(unused -> null); // Empty stream
          }

          // We rely on the order of injected methods being deterministic
          AtomicInteger index = new AtomicInteger(0);
          return injectedMethods.stream()
              .map(injected ->
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

  @Nullable
  private static Object defaultValuesProvider(Class<? extends Annotation> annotationType) {
    try {
      return annotationType.getMethod("valuesProvider").getDefaultValue();
    } catch (NoSuchMethodException e) {
      return null;
    }
  }

  private boolean hasCustomValuesProviders() {
    for (FrameworkField field : getTestClass().getAnnotatedFields(TestParameter.class)) {
      if (hasCustomValuesProvider(field.getField())) {
        return true;
      }
    }
    for (Constructor<?> constructor : getTestClass().getJavaClass().getDeclaredConstructors()) {
      if (hasCustomValuesProvider(constructor)) {
        return true;
      }
    }
    for (FrameworkMethod method : getTestClass().getAnnotatedMethods(Test.class)) {
      if (hasCustomValuesProvider(method.getMethod())) {
        return true;
      }
    }
    return false;
  }

  private static boolean hasCustomValuesProvider(Executable executable) {
    return hasCustomValuesProvider((AnnotatedElement) executable)
        || stream(executable.getParameters())
            .anyMatch(RobolectricTestParameterInjector::hasCustomValuesProvider);
  }

  private static boolean hasCustomValuesProvider(AnnotatedElement element) {
    TestParameter parameter = element.getAnnotation(TestParameter.class);
    if (parameter != null
        && DEFAULT_TEST_PARAMETER_VALUES_PROVIDER != null
        && parameter.valuesProvider() != DEFAULT_TEST_PARAMETER_VALUES_PROVIDER) {
      return true;
    }
    for (TestParameters parameters : element.getAnnotationsByType(TestParameters.class)) {
      if (DEFAULT_TEST_PARAMETERS_VALUES_PROVIDER != null
          && parameters.valuesProvider() != DEFAULT_TEST_PARAMETERS_VALUES_PROVIDER) {
        return true;
      }
    }
    return false;
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
  private final class DelegateHelperTestRunner extends HelperTestRunner {
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
      DelegateTestRunner discoveryDelegate =
          delegatesByClassLoader.get(getTestClass().getJavaClass().getClassLoader());
      if (discoveryDelegate != null) {
        return discoveryDelegate.methodBlock(
            robolectricInjectedFrameworkMethod.getInjectedMethod());
      }
      // Map the method to one that has been class loaded in the SDK sandbox.
      return getTestParameterInjector()
          .methodBlock(
              Objects.requireNonNull(injectedMethods.get(method.getMethod()))
                  .get(robolectricInjectedFrameworkMethod.getInjectedMethodIndex()));
    }

    @Override
    protected Statement methodInvoker(FrameworkMethod method, Object test) {
      DelegateTestRunner discoveryDelegate =
          delegatesByClassLoader.get(getTestClass().getJavaClass().getClassLoader());
      return (discoveryDelegate != null ? discoveryDelegate : getTestParameterInjector())
          .methodInvoker(method, test);
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
