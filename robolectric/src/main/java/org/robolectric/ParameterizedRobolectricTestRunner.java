package org.robolectric;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import org.junit.Assert;
import org.junit.runner.Runner;
import org.junit.runners.Parameterized;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkField;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.TestClass;
import org.robolectric.internal.SandboxTestRunner;
import org.robolectric.util.ReflectionHelpers;

/**
 * A Parameterized test runner for Robolectric. Copied from the {@link Parameterized} class, then
 * modified the custom test runner to extend the {@link RobolectricTestRunner}. The {@link
 * org.robolectric.RobolectricTestRunner#getHelperTestRunner(Class)} is overridden in order to
 * create instances of the test class with the appropriate parameters. Merged in the ability to name
 * your tests through the {@link Parameters#name()} property. Merged in support for {@link
 * Parameter} annotation alternative to providing a constructor.
 *
 * <p>This class takes care of the fact that the test runner and the test class are actually loaded
 * from different class loaders and therefore parameter objects created by one cannot be assigned to
 * instances of the other.
 */
public final class ParameterizedRobolectricTestRunner extends Suite {

  /**
   * Annotation for a method which provides parameters to be injected into the test class
   * constructor by <code>Parameterized</code>
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public @interface Parameters {

    /**
     * Optional pattern to derive the test's name from the parameters. Use numbers in braces to
     * refer to the parameters or the additional data as follows:
     *
     * <pre>
     * {index} - the current parameter index
     * {0} - the first parameter value
     * {1} - the second parameter value
     * etc...
     * </pre>
     *
     * <p>Default value is "{index}" for compatibility with previous JUnit versions.
     *
     * @return {@link MessageFormat} pattern string, except the index placeholder.
     * @see MessageFormat
     */
    String name() default "{index}";
  }

  /**
   * Annotation for fields of the test class which will be initialized by the method annotated by
   * <code>Parameters</code><br>
   * By using directly this annotation, the test class constructor isn't needed.<br>
   * Index range must start at 0. Default value is 0.
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.FIELD)
  public @interface Parameter {
    /**
     * Method that returns the index of the parameter in the array returned by the method annotated
     * by <code>Parameters</code>.<br>
     * Index range must start at 0. Default value is 0.
     *
     * @return the index of the parameter.
     */
    int value() default 0;
  }

  private static class TestClassRunnerForParameters extends RobolectricTestRunner {

    private final int parametersIndex;
    private final String name;

    TestClassRunnerForParameters(Class<?> type, int parametersIndex, String name)
        throws InitializationError {
      super(type);
      this.parametersIndex = parametersIndex;
      this.name = name;
    }

    private Object createTestInstance(Class bootstrappedClass) throws Exception {
      Constructor<?>[] constructors = bootstrappedClass.getConstructors();
      Assert.assertEquals(1, constructors.length);
      if (!fieldsAreAnnotated()) {
        return constructors[0].newInstance(computeParams(bootstrappedClass.getClassLoader()));
      } else {
        Object instance = constructors[0].newInstance();
        injectParametersIntoFields(instance, bootstrappedClass.getClassLoader());
        return instance;
      }
    }

    private Object[] computeParams(ClassLoader classLoader) throws Exception {
      // Robolectric uses a different class loader when running the tests, so the parameters objects
      // created by the test runner are not compatible with the parameters required by the test.
      // Instead, we compute the parameters within the test's class loader.
      try {
        List<Object[]> parametersList = getParametersList(getTestClass(), classLoader);
        if (parametersIndex >= parametersList.size()) {
          throw new Exception(
              "Re-computing the parameter list returned a different number of "
                  + "parameters values. Is the data() method of your test non-deterministic?");
        }
        return parametersList.get(parametersIndex);
      } catch (ClassCastException e) {
        throw new Exception(
            String.format(
                "%s.%s() must return a Collection of arrays.", getTestClass().getName(), name));
      } catch (Exception exception) {
        throw exception;
      } catch (Throwable throwable) {
        throw new Exception(throwable);
      }
    }

    @SuppressWarnings("unchecked")
    private void injectParametersIntoFields(Object testClassInstance, ClassLoader classLoader)
        throws Exception {
      // Robolectric uses a different class loader when running the tests, so referencing Parameter
      // directly causes type mismatches. Instead, we find its class within the test's class loader.
      Class<?> parameterClass = getClassInClassLoader(Parameter.class, classLoader);
      Object[] parameters = computeParams(classLoader);
      HashSet<Integer> parameterFieldsFound = new HashSet<>();
      for (Field field : testClassInstance.getClass().getFields()) {
        Annotation parameter = field.getAnnotation((Class<Annotation>) parameterClass);
        if (parameter != null) {
          int index = ReflectionHelpers.callInstanceMethod(parameter, "value");
          parameterFieldsFound.add(index);
          try {
            field.set(testClassInstance, parameters[index]);
          } catch (IllegalArgumentException iare) {
            throw new Exception(
                getTestClass().getName()
                    + ": Trying to set "
                    + field.getName()
                    + " with the value "
                    + parameters[index]
                    + " that is not the right type ("
                    + parameters[index].getClass().getSimpleName()
                    + " instead of "
                    + field.getType().getSimpleName()
                    + ").",
                iare);
          }
        }
      }
      if (parameterFieldsFound.size() != parameters.length) {
        throw new IllegalStateException(
            String.format(
                Locale.US,
                "Provided %d parameters, but only found fields for parameters: %s",
                parameters.length,
                parameterFieldsFound.toString()));
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
      if (fieldsAreAnnotated()) {
        validateZeroArgConstructor(errors);
      }
    }

    @Override
    public String toString() {
      return "TestClassRunnerForParameters " + name;
    }

    @Override
    protected void validateFields(List<Throwable> errors) {
      super.validateFields(errors);
      // Ensure that indexes for parameters are correctly defined
      if (fieldsAreAnnotated()) {
        List<FrameworkField> annotatedFieldsByParameter = getAnnotatedFieldsByParameter();
        int[] usedIndices = new int[annotatedFieldsByParameter.size()];
        for (FrameworkField each : annotatedFieldsByParameter) {
          int index = each.getField().getAnnotation(Parameter.class).value();
          if (index < 0 || index > annotatedFieldsByParameter.size() - 1) {
            errors.add(
                new Exception(
                    "Invalid @Parameter value: "
                        + index
                        + ". @Parameter fields counted: "
                        + annotatedFieldsByParameter.size()
                        + ". Please use an index between 0 and "
                        + (annotatedFieldsByParameter.size() - 1)
                        + "."));
          } else {
            usedIndices[index]++;
          }
        }
        for (int index = 0; index < usedIndices.length; index++) {
          int numberOfUse = usedIndices[index];
          if (numberOfUse == 0) {
            errors.add(new Exception("@Parameter(" + index + ") is never used."));
          } else if (numberOfUse > 1) {
            errors.add(
                new Exception(
                    "@Parameter(" + index + ") is used more than once (" + numberOfUse + ")."));
          }
        }
      }
    }

    @Override
    protected SandboxTestRunner.HelperTestRunner getHelperTestRunner(Class bootstrappedTestClass) {
      try {
        return new HelperTestRunner(bootstrappedTestClass) {
          @Override
          protected void validateConstructor(List<Throwable> errors) {
            TestClassRunnerForParameters.this.validateOnlyOneConstructor(errors);
          }

          @Override
          protected Object createTest() throws Exception {
            return TestClassRunnerForParameters.this.createTestInstance(
                getTestClass().getJavaClass());
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

    private List<FrameworkField> getAnnotatedFieldsByParameter() {
      return getTestClass().getAnnotatedFields(Parameter.class);
    }

    private boolean fieldsAreAnnotated() {
      return !getAnnotatedFieldsByParameter().isEmpty();
    }
  }

  private final ArrayList<Runner> runners = new ArrayList<>();

  /*
   * Only called reflectively. Do not use programmatically.
   */
  public ParameterizedRobolectricTestRunner(Class<?> klass) throws Throwable {
    super(klass, Collections.<Runner>emptyList());
    TestClass testClass = getTestClass();
    ClassLoader classLoader = getClass().getClassLoader();
    Parameters parameters =
        getParametersMethod(testClass, classLoader).getAnnotation(Parameters.class);
    List<Object[]> parametersList = getParametersList(testClass, classLoader);
    for (int i = 0; i < parametersList.size(); i++) {
      Object[] parameterArray = parametersList.get(i);
      runners.add(
          new TestClassRunnerForParameters(
              testClass.getJavaClass(), i, nameFor(parameters.name(), i, parameterArray)));
    }
  }

  @Override
  protected List<Runner> getChildren() {
    return runners;
  }

  @SuppressWarnings("unchecked")
  private static List<Object[]> getParametersList(TestClass testClass, ClassLoader classLoader)
      throws Throwable {
    return (List<Object[]>) getParametersMethod(testClass, classLoader).invokeExplosively(null);
  }

  private static FrameworkMethod getParametersMethod(TestClass testClass, ClassLoader classLoader)
      throws Exception {
    List<FrameworkMethod> methods = testClass.getAnnotatedMethods(Parameters.class);
    for (FrameworkMethod each : methods) {
      int modifiers = each.getMethod().getModifiers();
      if (Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers)) {
        return getFrameworkMethodInClassLoader(each, classLoader);
      }
    }

    throw new Exception("No public static parameters method on class " + testClass.getName());
  }

  private static String nameFor(String namePattern, int index, Object[] parameters) {
    String finalPattern = namePattern.replaceAll("\\{index\\}", Integer.toString(index));
    String name = MessageFormat.format(finalPattern, parameters);
    return "[" + name + "]";
  }

  /**
   * Returns the {@link FrameworkMethod} object for the given method in the provided class loader.
   */
  private static FrameworkMethod getFrameworkMethodInClassLoader(
      FrameworkMethod method, ClassLoader classLoader)
      throws ClassNotFoundException, NoSuchMethodException {
    Method methodInClassLoader = getMethodInClassLoader(method.getMethod(), classLoader);
    if (methodInClassLoader.equals(method.getMethod())) {
      // The method was already loaded in the right class loader, return it as is.
      return method;
    }
    return new FrameworkMethod(methodInClassLoader);
  }

  /** Returns the {@link Method} object for the given method in the provided class loader. */
  private static Method getMethodInClassLoader(Method method, ClassLoader classLoader)
      throws ClassNotFoundException, NoSuchMethodException {
    Class<?> declaringClass = method.getDeclaringClass();

    if (declaringClass.getClassLoader() == classLoader) {
      // The method was already loaded in the right class loader, return it as is.
      return method;
    }

    // Find the class in the class loader corresponding to the declaring class of the method.
    Class<?> declaringClassInClassLoader = getClassInClassLoader(declaringClass, classLoader);

    // Find the method with the same signature in the class loader.
    return declaringClassInClassLoader.getMethod(method.getName(), method.getParameterTypes());
  }

  /** Returns the {@link Class} object for the given class in the provided class loader. */
  private static Class<?> getClassInClassLoader(Class<?> klass, ClassLoader classLoader)
      throws ClassNotFoundException {
    if (klass.getClassLoader() == classLoader) {
      // The method was already loaded in the right class loader, return it as is.
      return klass;
    }

    // Find the class in the class loader corresponding to the declaring class of the method.
    return classLoader.loadClass(klass.getName());
  }
}
