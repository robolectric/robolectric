package org.robolectric;

import org.junit.Assert;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Parameterized;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A Parameterized test runner for Robolectric. Copied from the {@link Parameterized} class, then modified the custom
 * test runner to extend the {@link RobolectricTestRunner}. The {@link RobolectricTestRunner#getHelperTestRunner(Class)}
 * is overridden in order to create instances of the test class with the appropriate parameters. Merged in the ability
 * to name your tests through the {@link Parameters#name()} property.
 *
 * @author John Ferlisi
 * @see Parameterized
 */
public final class ParameterizedRobolectricTestRunner extends Suite {

  /**
   * Annotation for a method which provides parameters to be injected into the test class constructor by
   * <code>Parameterized</code>
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public @interface Parameters {

    /**
     * <p> Optional pattern to derive the test's name from the parameters. Use numbers in braces to refer to the
     * parameters or the additional data as follows: </p>
     * <p/>
     * <pre>
     * {index} - the current parameter index
     * {0} - the first parameter value
     * {1} - the second parameter value
     * etc...
     * </pre>
     * <p> Default value is "{index}" for compatibility with previous JUnit versions. </p>
     *
     * @return {@link MessageFormat} pattern string, except the index placeholder.
     * @see MessageFormat
     */
    String name() default "{index}";
  }

  private static class TestClassRunnerForParameters extends RobolectricTestRunner {

    private final String name;
    private final Object[] parameters;

    TestClassRunnerForParameters(Class<?> type, Object[] parameters, String name) throws InitializationError {
      super(type);
      this.parameters = parameters;
      this.name = name;
    }

    private Object createTestInstance(Class bootstrappedClass) throws Exception {
      Constructor<?>[] constructors = bootstrappedClass.getConstructors();
      Assert.assertEquals(1, constructors.length);
      return constructors[0].newInstance(computeParams());
    }

    private Object[] computeParams() throws Exception {
      try {
        return parameters;
      } catch (ClassCastException e) {
        throw new Exception(String.format("%s.%s() must return a Collection of arrays.",
                                          getTestClass().getName(),
                                          name));
      }
    }

    @Override
    protected String getName() {
      return name;
    }

    @Override
    protected String testName(final FrameworkMethod method) {
      return method.getName() + getName();
    }

    @Override
    protected void validateConstructor(List<Throwable> errors) {
      validateOnlyOneConstructor(errors);
    }

    @Override
    protected Statement classBlock(RunNotifier notifier) {
      return childrenInvoker(notifier);
    }

    @Override
    public String toString() {
      return "TestClassRunnerForParameters " + name;
    }

    @Override
    protected HelperTestRunner getHelperTestRunner(Class bootstrappedTestClass) {
      try {
        return new HelperTestRunner(bootstrappedTestClass) {
          @Override
          protected void validateConstructor(List<Throwable> errors) {
            TestClassRunnerForParameters.this.validateOnlyOneConstructor(errors);
          }

          @Override
          protected Object createTest() throws Exception {
            return TestClassRunnerForParameters.this.createTestInstance(getTestClass().getJavaClass());
          }

          @Override
          public String toString() {
            return "HelperTestRunner for " + TestClassRunnerForParameters.this.toString();
          }
        };
      } catch (InitializationError initializationError) {
        throw new RuntimeException(initializationError);
      }
    }
  }

  private final ArrayList<Runner> runners = new ArrayList<Runner>();

  /**
   * Only called reflectively. Do not use programmatically.
   */
  public ParameterizedRobolectricTestRunner(Class<?> klass) throws Throwable {
    super(klass, Collections.<Runner>emptyList());
    Parameters parameters = getParametersMethod().getAnnotation(Parameters.class);
    List<Object[]> parametersList = getParametersList();
    for (int i = 0; i < parametersList.size(); i++) {
      Object[] parameterArray = parametersList.get(i);
      runners.add(new TestClassRunnerForParameters(getTestClass().getJavaClass(),
                                                   parameterArray,
                                                   nameFor(parameters.name(), i, parameterArray)));
    }
  }

  @Override
  protected List<Runner> getChildren() {
    return runners;
  }

  @SuppressWarnings("unchecked")
  private List<Object[]> getParametersList() throws Throwable {
    return (List<Object[]>) getParametersMethod().invokeExplosively(null);
  }

  private FrameworkMethod getParametersMethod() throws Exception {
    TestClass testClass = getTestClass();
    List<FrameworkMethod> methods = testClass.getAnnotatedMethods(Parameters.class);
    for (FrameworkMethod each : methods) {
      int modifiers = each.getMethod().getModifiers();
      if (Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers)) {
        return each;
      }
    }

    throw new Exception("No public static parameters method on class " + testClass.getName());
  }

  private static String nameFor(String namePattern, int index, Object[] parameters) {
    String finalPattern = namePattern.replaceAll("\\{index\\}", Integer.toString(index));
    String name = MessageFormat.format(finalPattern, parameters);
    return "[" + name + "]";
  }

}
