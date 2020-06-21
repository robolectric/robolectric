package org.robolectric.internal;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import javax.annotation.Nonnull;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.internal.runners.statements.FailOnTimeout;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;
import org.robolectric.internal.bytecode.Sandbox;
import org.robolectric.util.PerfStatsCollector;
import org.robolectric.util.PerfStatsCollector.Event;
import org.robolectric.util.Util;

/**
 * SandboxTestRunner is a JUnit 4 test runner that allows the test class to be swapped out with a similar one from
 * another classloader.
 */
@SuppressWarnings("NewApi")
public abstract class SandboxTestRunner extends BlockJUnit4ClassRunner {

  private final HashSet<Class<?>> loadedTestClasses = new HashSet<>();

  public SandboxTestRunner(Class<?> klass) throws InitializationError {
    super(klass);
  }

  @Override
  protected Statement classBlock(RunNotifier notifier) {
    final Statement statement = childrenInvoker(notifier);
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        try {
          statement.evaluate();
          for (Class<?> testClass : loadedTestClasses) {
            invokeAfterClass(testClass);
          }
        } finally {
          afterClass();
          loadedTestClasses.clear();
        }
      }
    };
  }

  private void invokeBeforeClass(final Class clazz) throws Throwable {
    if (!loadedTestClasses.contains(clazz)) {
      loadedTestClasses.add(clazz);

      final TestClass testClass = new TestClass(clazz);
      final List<FrameworkMethod> befores = testClass.getAnnotatedMethods(BeforeClass.class);
      for (FrameworkMethod before : befores) {
        before.invokeExplosively(null);
      }
    }
  }

  private static void invokeAfterClass(final Class<?> clazz) throws Throwable {
    final TestClass testClass = new TestClass(clazz);
    final List<FrameworkMethod> afters = testClass.getAnnotatedMethods(AfterClass.class);
    for (FrameworkMethod after : afters) {
      after.invokeExplosively(null);
    }
  }

  protected void afterClass() {
  }

  @Nonnull
  abstract protected Sandbox getSandbox(FrameworkMethod method);

  abstract protected void configureSandbox(Sandbox sandbox, FrameworkMethod method);

  @Override protected Statement methodBlock(final FrameworkMethod method) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        Event initialization = PerfStatsCollector.getInstance()
            .startEvent("initialization");

        Sandbox sandbox = getSandbox(method);

        // Configure sandbox *BEFORE* setting the ClassLoader. This is necessary because
        // creating the ShadowMap loads all ShadowProviders via ServiceLoader and this is
        // not available once we install the Robolectric class loader.
        configureSandbox(sandbox, method);

        sandbox.runOnMainThread(() -> {
          ClassLoader priorContextClassLoader = Thread.currentThread().getContextClassLoader();
          Thread.currentThread().setContextClassLoader(sandbox.getRobolectricClassLoader());

          Class bootstrappedTestClass =
              sandbox.bootstrappedClass(getTestClass().getJavaClass());
          HelperTestRunner helperTestRunner = getHelperTestRunner(bootstrappedTestClass);
          helperTestRunner.frameworkMethod = method;

          final Method bootstrappedMethod;
          try {
            //noinspection unchecked
            bootstrappedMethod = bootstrappedTestClass.getMethod(method.getMethod().getName());
          } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
          }

          try {
            // Only invoke @BeforeClass once per class
            invokeBeforeClass(bootstrappedTestClass);

            beforeTest(sandbox, method, bootstrappedMethod);

            initialization.finished();

            Statement statement =
                helperTestRunner.methodBlock(new FrameworkMethod(bootstrappedMethod));

            // todo: this try/finally probably isn't right -- should mimic RunAfters? [xw]
            try {
              statement.evaluate();
            } finally {
              afterTest(sandbox, method, bootstrappedMethod);
            }
          } catch (Throwable throwable) {
            throw Util.sneakyThrow(throwable);
          } finally {
            Thread.currentThread().setContextClassLoader(priorContextClassLoader);
            try {
              finallyAfterTest(method);
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        });
      }
    };
  }

  protected void beforeTest(Sandbox sandbox, FrameworkMethod method, Method bootstrappedMethod) throws Throwable {
  }

  protected void afterTest(Sandbox sandbox, FrameworkMethod method, Method bootstrappedMethod) {
  }

  protected void finallyAfterTest(FrameworkMethod method) {
  }

  protected HelperTestRunner getHelperTestRunner(Class bootstrappedTestClass) {
    try {
      return new HelperTestRunner(bootstrappedTestClass);
    } catch (InitializationError initializationError) {
      throw new RuntimeException(initializationError);
    }
  }

  protected static class HelperTestRunner extends BlockJUnit4ClassRunner {
    public FrameworkMethod frameworkMethod;

    public HelperTestRunner(Class<?> klass) throws InitializationError {
      super(klass);
    }

    // for visibility from SandboxTestRunner.methodBlock()
    @Override
    protected Statement methodBlock(FrameworkMethod method) {
      return super.methodBlock(method);
    }

    /**
     * For tests with a timeout, we need to wrap the test method execution (but not `@Before`s or
     * `@After`s) in a {@link TimeLimitedStatement}. JUnit's built-in {@link FailOnTimeout}
     * statement causes the test method (but not `@Before`s or `@After`s) to be run on a short-lived
     * thread. This is inadequate for our purposes; we want to guarantee that every entry point to
     * test code is run from the same thread.
     */
    @Override
    protected Statement methodInvoker(FrameworkMethod method, Object test) {
      Statement delegate = super.methodInvoker(method, test);
      long timeout = getTimeout(method.getAnnotation(Test.class));

      if (timeout == 0) {
        return delegate;
      } else {
        return new TimeLimitedStatement(timeout, delegate);
      }
    }

    /**
     * Disables JUnit's normal timeout mode strategy.
     *
     * @see #methodInvoker(FrameworkMethod, Object)
     * @see TimeLimitedStatement
     */
    @Override
    protected Statement withPotentialTimeout(FrameworkMethod method, Object test, Statement next) {
      return next;
    }

    private long getTimeout(Test annotation) {
      if (annotation == null) {
        return 0;
      }
      return annotation.timeout();
    }

  }

  /**
   * Disables JUnit's normal timeout mode strategy.
   *
   * @see #methodInvoker(FrameworkMethod, Object)
   * @see TimeLimitedStatement
   */
  protected Statement withPotentialTimeout(FrameworkMethod method, Object test, Statement next) {
    return next;
  }
}
